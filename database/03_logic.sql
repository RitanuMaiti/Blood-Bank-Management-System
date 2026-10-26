-- ==========================================
-- 03_logic.sql: PL/SQL Triggers, Procedures & Views
-- ==========================================
SET SERVEROUTPUT ON;

-- 1. LOW STOCK ALERT VIEW
CREATE OR REPLACE VIEW v_low_inventory_alert AS
SELECT Blood_Type, Component_Type, COUNT(Unit_ID) as Available_Units
FROM BLOOD_INVENTORY
WHERE Status = 'Available' AND Expiry_Date >= SYSDATE
GROUP BY Blood_Type, Component_Type
HAVING COUNT(Unit_ID) < 5;
/

-- 2. 90-DAY COOLDOWN TRIGGER
CREATE OR REPLACE TRIGGER trg_check_cooldown
BEFORE INSERT ON DONATION_RECORD
FOR EACH ROW
DECLARE
    v_last_donation DATE;
    v_days_since NUMBER;
BEGIN
    SELECT Last_Donation_Date INTO v_last_donation
    FROM DONOR
    WHERE Donor_ID = :NEW.Donor_ID;

    IF v_last_donation IS NOT NULL THEN
        v_days_since := :NEW.Donation_Date - v_last_donation;
        IF v_days_since < 90 THEN
            RAISE_APPLICATION_ERROR(-20001, 'REJECTED: Donor is in the 90-day cooldown period. Only ' || ROUND(v_days_since) || ' days have passed.');
        END IF;
    END IF;
    
    UPDATE DONOR 
    SET Last_Donation_Date = :NEW.Donation_Date,
        Eligibility_Status = 'Cooldown'
    WHERE Donor_ID = :NEW.Donor_ID;
END;
/

-- 3. BLOOD ALLOCATION MATCHING ENGINE (STORED PROCEDURE)
CREATE OR REPLACE PROCEDURE allocate_blood (
    p_request_id IN NUMBER
) AS
    v_blood_type VARCHAR2(3);
    v_comp_type VARCHAR2(50);
    v_units_req NUMBER;
    v_allocated NUMBER := 0;
    v_unit_id NUMBER;
    
    CURSOR c_available_units(p_blood VARCHAR2, p_comp VARCHAR2) IS
        SELECT Unit_ID 
        FROM BLOOD_INVENTORY 
        WHERE Blood_Type = p_blood 
          AND Component_Type = p_comp 
          AND Status = 'Available'
          AND Expiry_Date >= SYSDATE
        ORDER BY Collection_Date ASC; 
BEGIN
    -- Retrieve request requirements (removed unused Hospital_ID variable)
    SELECT Required_Blood_Type, Required_Component, Units_Requested
    INTO v_blood_type, v_comp_type, v_units_req
    FROM EMERGENCY_REQUEST
    WHERE Request_ID = p_request_id AND Request_Status = 'Pending';
    
    OPEN c_available_units(v_blood_type, v_comp_type);
    LOOP
        FETCH c_available_units INTO v_unit_id;
        EXIT WHEN c_available_units%NOTFOUND OR v_allocated >= v_units_req;
        
        INSERT INTO ALLOCATION_DISPATCH (Request_ID, Unit_ID, Delivery_Status)
        VALUES (p_request_id, v_unit_id, 'In Transit');
        
        UPDATE BLOOD_INVENTORY
        SET Status = 'Dispatched'
        WHERE Unit_ID = v_unit_id;
        
        v_allocated := v_allocated + 1;
    END LOOP;
    CLOSE c_available_units;
    
    IF v_allocated = v_units_req THEN
        UPDATE EMERGENCY_REQUEST SET Request_Status = 'Fulfilled' WHERE Request_ID = p_request_id;
        DBMS_OUTPUT.PUT_LINE('SUCCESS: All units allocated.');
    ELSIF v_allocated > 0 THEN
        DBMS_OUTPUT.PUT_LINE('PARTIAL: Only ' || TO_CHAR(v_allocated) || ' units available.');
    ELSE
        DBMS_OUTPUT.PUT_LINE('FAILED: No matching inventory available.');
    END IF;
    
    COMMIT;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE('ERROR: Request ID not found or already processed.');
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
/
