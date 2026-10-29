-- =======================================================================
-- 03_logic.sql: PL/SQL Triggers, Stored Procedures, Functions & Views
-- =======================================================================
SET SERVEROUTPUT ON;

-- 1. LOW STOCK ALERT VIEW
-- Excludes expired, reserved, or already dispatched units
CREATE OR REPLACE VIEW v_low_inventory_alert AS
SELECT Blood_Type, Component_Type, COUNT(Unit_ID) AS Available_Units
FROM BLOOD_INVENTORY
WHERE Status = 'Available'
  AND Expiry_Date >= TRUNC(SYSDATE)
GROUP BY Blood_Type, Component_Type
HAVING COUNT(Unit_ID) < 5;
/

-- 2. HELPER FUNCTION: CHECK BLOOD COMPATIBILITY
-- Implements component-aware transfusion medicine compatibility rules:
--   - Whole Blood: exact ABO/Rh match (conservative; no low-titer O protocol assumed)
--   - Red-cell components: ABO/Rh compatible
--   - Plasma (Fresh Frozen Plasma): ABO plasma compatibility (AB is universal plasma donor, O is universal plasma recipient)
--   - Platelets: Identical ABO/Rh group preferred
CREATE OR REPLACE FUNCTION is_blood_compatible (
    p_donor_type     IN VARCHAR2,
    p_recipient_type IN VARCHAR2,
    p_component      IN VARCHAR2
) RETURN NUMBER IS
BEGIN
    -- Platelets: Exact ABO match required under standard conservative protocol
    IF p_component = 'Platelets' THEN
        IF p_donor_type = p_recipient_type THEN
            RETURN 1;
        ELSE
            RETURN 0;
        END IF;
    END IF;

    -- Whole blood contains both red cells and donor plasma: require exact ABO/Rh match conservatively.
    IF UPPER(TRIM(p_component)) = 'WHOLE BLOOD' THEN
        IF p_donor_type = p_recipient_type THEN RETURN 1; ELSE RETURN 0; END IF;
    END IF;

    -- Plasma / Fresh Frozen Plasma: Acellular compatibility (anti-A/anti-B in donor plasma)
    IF p_component IN ('Fresh Frozen Plasma', 'Plasma') THEN
        CASE
            WHEN p_recipient_type IN ('AB+', 'AB-') THEN
                IF p_donor_type IN ('AB+', 'AB-') THEN RETURN 1; ELSE RETURN 0; END IF;
            WHEN p_recipient_type IN ('A+', 'A-') THEN
                IF p_donor_type IN ('A+', 'A-', 'AB+', 'AB-') THEN RETURN 1; ELSE RETURN 0; END IF;
            WHEN p_recipient_type IN ('B+', 'B-') THEN
                IF p_donor_type IN ('B+', 'B-', 'AB+', 'AB-') THEN RETURN 1; ELSE RETURN 0; END IF;
            WHEN p_recipient_type IN ('O+', 'O-') THEN
                RETURN 1; -- O recipient can safely receive any plasma
            ELSE
                RETURN 0;
        END CASE;
    END IF;

    -- Red-cell components (Red Blood Cells, Packed Red Cells):
    -- O- is the universal red-cell donor; AB+ is the universal red-cell recipient
    CASE p_recipient_type
        WHEN 'O-' THEN
            IF p_donor_type = 'O-' THEN RETURN 1; ELSE RETURN 0; END IF;
        WHEN 'O+' THEN
            IF p_donor_type IN ('O-', 'O+') THEN RETURN 1; ELSE RETURN 0; END IF;
        WHEN 'A-' THEN
            IF p_donor_type IN ('O-', 'A-') THEN RETURN 1; ELSE RETURN 0; END IF;
        WHEN 'A+' THEN
            IF p_donor_type IN ('O-', 'O+', 'A-', 'A+') THEN RETURN 1; ELSE RETURN 0; END IF;
        WHEN 'B-' THEN
            IF p_donor_type IN ('O-', 'B-') THEN RETURN 1; ELSE RETURN 0; END IF;
        WHEN 'B+' THEN
            IF p_donor_type IN ('O-', 'O+', 'B-', 'B+') THEN RETURN 1; ELSE RETURN 0; END IF;
        WHEN 'AB-' THEN
            IF p_donor_type IN ('O-', 'A-', 'B-', 'AB-') THEN RETURN 1; ELSE RETURN 0; END IF;
        WHEN 'AB+' THEN
            RETURN 1; -- AB+ can receive all cellular blood types
        ELSE
            RETURN 0;
    END CASE;
END is_blood_compatible;
/

-- 3. 90-DAY DONOR COOLDOWN TRIGGER
-- Enforces mandatory inter-donation interval (configurable project rule: 90 days for whole blood)
-- Keeps DONOR.Last_Donation_Date and Eligibility_Status synchronized atomically.
CREATE OR REPLACE TRIGGER trg_check_cooldown
BEFORE INSERT ON DONATION_RECORD
FOR EACH ROW
DECLARE
    v_last_donation DATE;
    v_eligibility_status DONOR.Eligibility_Status%TYPE;
    v_days_since    NUMBER;
BEGIN
    -- Fallback for donation date
    IF :NEW.Donation_Date IS NULL THEN
        :NEW.Donation_Date := TRUNC(SYSDATE);
    END IF;

    -- Prevent future donation records
    IF :NEW.Donation_Date > TRUNC(SYSDATE) THEN
        RAISE_APPLICATION_ERROR(-20002, 'REJECTED: Donation date cannot be in the future.');
    END IF;

    -- Fetch current donor history
    SELECT Last_Donation_Date, Eligibility_Status
      INTO v_last_donation, v_eligibility_status
      FROM DONOR
     WHERE Donor_ID = :NEW.Donor_ID
       FOR UPDATE;

    IF v_eligibility_status = 'Permanent_Deferral' THEN
        RAISE_APPLICATION_ERROR(-20004, 'REJECTED: Permanently deferred donors cannot donate.');
    END IF;

    IF v_last_donation IS NOT NULL THEN
        IF :NEW.Donation_Date < v_last_donation THEN
            RAISE_APPLICATION_ERROR(-20003, 'REJECTED: Donation date cannot precede the donor''s previous recorded donation (' || TO_CHAR(v_last_donation, 'YYYY-MM-DD') || ').');
        END IF;

        v_days_since := :NEW.Donation_Date - v_last_donation;
        IF v_days_since < 90 THEN
            RAISE_APPLICATION_ERROR(-20001, 'REJECTED: Donor is in the 90-day cooldown period. Only ' || ROUND(v_days_since) || ' days have elapsed since last donation.');
        END IF;
    END IF;

    -- Atomic synchronization of donor record
    UPDATE DONOR
    SET Last_Donation_Date = :NEW.Donation_Date,
        Eligibility_Status = 'Cooldown'
    WHERE Donor_ID = :NEW.Donor_ID;
END;
/

-- 4. BLOOD ALLOCATION MATCHING ENGINE (STORED PROCEDURE)
-- Features:
--   * Component-aware compatibility matching
--   * Supports both Pending and Partially_Fulfilled requests
--   * Considers already-dispatched units to prevent over-allocation
--   * Prevents allocating expired units (Expiry_Date >= TRUNC(SYSDATE))
--   * Updates request status to 'Partially_Fulfilled' or 'Fulfilled' consistently
--   * Uses FOR UPDATE to lock inventory candidates and prevent concurrent race conditions
CREATE OR REPLACE PROCEDURE allocate_blood (
    p_request_id        IN NUMBER,
    p_units_allocated   OUT NUMBER,
    p_final_status      OUT VARCHAR2
) AS
    v_blood_type        VARCHAR2(3);
    v_comp_type         VARCHAR2(50);
    v_units_req         NUMBER;
    v_current_status    VARCHAR2(20);
    v_already_allocated NUMBER := 0;
    v_remaining         NUMBER := 0;
    v_allocated_now     NUMBER := 0;
    v_unit_id           NUMBER;
    v_comp_key          VARCHAR2(50);

    -- Select unreserved, unexpired units of the requested component ordered by oldest collection (FIFO)
    CURSOR c_candidate_units(p_comp_key VARCHAR2) IS
        SELECT Unit_ID, Blood_Type
        FROM BLOOD_INVENTORY
        WHERE CASE
                  WHEN UPPER(Component_Type) LIKE '%PLASMA%' THEN 'PLASMA'
                  WHEN UPPER(Component_Type) LIKE '%PLATELET%' THEN 'PLATELETS'
                  WHEN UPPER(Component_Type) LIKE '%RED%' OR UPPER(Component_Type) LIKE '%PACKED%' THEN 'RED BLOOD CELLS'
                  WHEN UPPER(Component_Type) LIKE '%WHOLE%' THEN 'WHOLE BLOOD'
                  ELSE UPPER(TRIM(Component_Type))
              END = p_comp_key
          AND Status = 'Available'
          AND Expiry_Date >= TRUNC(SYSDATE)
        ORDER BY Expiry_Date ASC, Collection_Date ASC, Unit_ID ASC
        FOR UPDATE;
BEGIN
    p_units_allocated := 0;
    p_final_status := 'Pending';

    -- Retrieve open request requirements with row lock
    SELECT Required_Blood_Type, Required_Component, Units_Requested, Request_Status
    INTO v_blood_type, v_comp_type, v_units_req, v_current_status
    FROM EMERGENCY_REQUEST
    WHERE Request_ID = p_request_id
    FOR UPDATE;

    IF v_current_status IN ('Cancelled', 'Fulfilled') THEN
        p_final_status := v_current_status;
        DBMS_OUTPUT.PUT_LINE('NOTICE: Request #' || p_request_id || ' is already ' || v_current_status || '. No units allocated.');
        RETURN;
    END IF;

    -- Determine how many units have already been dispatched for this request
    SELECT COUNT(*) INTO v_already_allocated
    FROM ALLOCATION_DISPATCH
    WHERE Request_ID = p_request_id;

    v_remaining := v_units_req - v_already_allocated;
    IF v_remaining <= 0 THEN
        UPDATE EMERGENCY_REQUEST SET Request_Status = 'Fulfilled' WHERE Request_ID = p_request_id;
        p_final_status := 'Fulfilled';
        p_units_allocated := 0;
        COMMIT;
        RETURN;
    END IF;

    -- Normalize aliases exactly as the Java MatchingService does.
    IF UPPER(v_comp_type) LIKE '%PLASMA%' THEN
        v_comp_key := 'PLASMA';
    ELSIF UPPER(v_comp_type) LIKE '%PLATELET%' THEN
        v_comp_key := 'PLATELETS';
    ELSIF UPPER(v_comp_type) LIKE '%RED%' OR UPPER(v_comp_type) LIKE '%PACKED%' THEN
        v_comp_key := 'RED BLOOD CELLS';
    ELSIF UPPER(v_comp_type) LIKE '%WHOLE%' THEN
        v_comp_key := 'WHOLE BLOOD';
    ELSE
        v_comp_key := UPPER(TRIM(v_comp_type));
    END IF;

    -- Iterate through candidate inventory
    FOR rec IN c_candidate_units(v_comp_key) LOOP
        EXIT WHEN v_allocated_now >= v_remaining;

        -- Check biological compatibility
        IF is_blood_compatible(rec.Blood_Type, v_blood_type, v_comp_type) = 1 THEN
            -- Dispatch unit
            INSERT INTO ALLOCATION_DISPATCH (Request_ID, Unit_ID, Dispatch_Timestamp, Delivery_Status)
            VALUES (p_request_id, rec.Unit_ID, CURRENT_TIMESTAMP, 'In Transit');

            UPDATE BLOOD_INVENTORY
            SET Status = 'Dispatched'
            WHERE Unit_ID = rec.Unit_ID;

            v_allocated_now := v_allocated_now + 1;
        END IF;
    END LOOP;

    -- Update request status based on total fulfillment
    IF (v_already_allocated + v_allocated_now) >= v_units_req THEN
        p_final_status := 'Fulfilled';
        UPDATE EMERGENCY_REQUEST
        SET Request_Status = 'Fulfilled'
        WHERE Request_ID = p_request_id;
        DBMS_OUTPUT.PUT_LINE('SUCCESS: Request fully fulfilled. Allocated ' || v_allocated_now || ' unit(s).');
    ELSIF (v_already_allocated + v_allocated_now) > 0 THEN
        p_final_status := 'Partially_Fulfilled';
        UPDATE EMERGENCY_REQUEST
        SET Request_Status = 'Partially_Fulfilled'
        WHERE Request_ID = p_request_id;
        DBMS_OUTPUT.PUT_LINE('PARTIAL: Request partially fulfilled. Allocated ' || v_allocated_now || ' unit(s).');
    ELSE
        p_final_status := 'Pending';
        DBMS_OUTPUT.PUT_LINE('FAILED: No compatible available inventory found.');
    END IF;

    p_units_allocated := v_allocated_now;
    COMMIT;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        p_final_status := 'NotFound';
        p_units_allocated := 0;
        DBMS_OUTPUT.PUT_LINE('ERROR: Request ID #' || p_request_id || ' not found.');
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END allocate_blood;
/

-- Convenience wrapper with a unique name
CREATE OR REPLACE PROCEDURE allocate_blood_simple (
    p_request_id IN NUMBER
) AS
    v_allocated NUMBER;
    v_status    VARCHAR2(20);
BEGIN
    allocate_blood(p_request_id, v_allocated, v_status);

    DBMS_OUTPUT.PUT_LINE('Units allocated: ' || v_allocated);
    DBMS_OUTPUT.PUT_LINE('Final status: ' || v_status);
END allocate_blood_simple;
/
