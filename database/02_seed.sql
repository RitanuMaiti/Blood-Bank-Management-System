-- =======================================================================
-- 02_seed.sql: Seed Data for Blood Bank Management System
-- =======================================================================
-- Intended for a clean installation following 01_schema.sql.
-- Inserts parent records first, respects foreign-key constraints,
-- satisfies the 90-day donor cooldown rule, and correctly marks expired
-- units as 'Discarded'.
-- =======================================================================

-- 1. Insert Hospitals (using Address column)
INSERT INTO HOSPITAL (Hospital_ID, Hospital_Name, License_Number, Address, City, Contact_Person, Phone_Number, Email) VALUES
(1, 'City General Hospital', 'LIC-1001', '123 Health Ave', 'Vellore', 'Dr. Ramesh', '9876543210', 'emergency@citygen.com');

INSERT INTO HOSPITAL (Hospital_ID, Hospital_Name, License_Number, Address, City, Contact_Person, Phone_Number, Email) VALUES
(2, 'Apollo Care Center', 'LIC-1002', '45 Apollo Street', 'Chennai', 'Dr. Smitha', '9876543211', 'bloodreq@apollo.com');

INSERT INTO HOSPITAL (Hospital_ID, Hospital_Name, License_Number, Address, City, Contact_Person, Phone_Number, Email) VALUES
(3, 'Fortis Super Specialty Hospital', 'LIC-1003', '78 Parkway Blvd', 'Bengaluru', 'Dr. Arvind', '9876543212', 'transfusion@fortis.com');

-- 2. Insert Donors
-- Note: Last_Donation_Date represents the donor's previous donation PRIOR to current records.
-- Amit Patel previously donated on 2025-09-10 (122 days before his 2026-01-10 donation), satisfying the 90-day cooldown.
INSERT INTO DONOR (Donor_ID, First_Name, Last_Name, Blood_Type, DOB, Gender, Contact_Number, Email, Address, Last_Donation_Date, Eligibility_Status) VALUES
(1, 'Amit', 'Patel', 'O+', TO_DATE('1995-05-14', 'YYYY-MM-DD'), 'M', '9123456780', 'amit.p@email.com', 'Gandhi Road, Vellore', TO_DATE('2025-09-10', 'YYYY-MM-DD'), 'Eligible');

-- Priya Singh previously donated on 2026-05-10 (>90 days before her 2026-10-01 donation).
INSERT INTO DONOR (Donor_ID, First_Name, Last_Name, Blood_Type, DOB, Gender, Contact_Number, Email, Address, Last_Donation_Date, Eligibility_Status) VALUES
(2, 'Priya', 'Singh', 'A-', TO_DATE('1998-11-22', 'YYYY-MM-DD'), 'F', '9123456781', 'priya.s@email.com', 'Anna Nagar, Chennai', TO_DATE('2026-05-10', 'YYYY-MM-DD'), 'Eligible');

-- Rahul Sharma is a first-time donor (no prior donation).
INSERT INTO DONOR (Donor_ID, First_Name, Last_Name, Blood_Type, DOB, Gender, Contact_Number, Email, Address, Last_Donation_Date, Eligibility_Status) VALUES
(3, 'Rahul', 'Sharma', 'B+', TO_DATE('2001-03-30', 'YYYY-MM-DD'), 'M', '9123456782', 'rahul.s@email.com', 'Katpadi, Vellore', NULL, 'Eligible');

-- Sunita Rao is an AB+ donor.
INSERT INTO DONOR (Donor_ID, First_Name, Last_Name, Blood_Type, DOB, Gender, Contact_Number, Email, Address, Last_Donation_Date, Eligibility_Status) VALUES
(4, 'Sunita', 'Rao', 'AB+', TO_DATE('1992-08-18', 'YYYY-MM-DD'), 'F', '9123456783', 'sunita.r@email.com', 'Indira Nagar, Bengaluru', TO_DATE('2025-11-01', 'YYYY-MM-DD'), 'Eligible');

-- 3. Insert Donation Records
-- Donor 1 donates on 2026-01-10 (122 days after 2025-09-10 -> PASSES cooldown check)
INSERT INTO DONATION_RECORD (Donation_ID, Donor_ID, Donation_Date, Hemoglobin_Level, Blood_Pressure, Pulse_Rate, Medical_Screening_Result) VALUES
(1, 1, TO_DATE('2026-01-10', 'YYYY-MM-DD'), 14.2, '120/80', 72, 'Passed');

-- Donor 2 donates on 2026-10-01 (144 days after 2026-05-10 -> PASSES cooldown check)
INSERT INTO DONATION_RECORD (Donation_ID, Donor_ID, Donation_Date, Hemoglobin_Level, Blood_Pressure, Pulse_Rate, Medical_Screening_Result) VALUES
(2, 2, TO_DATE('2026-10-01', 'YYYY-MM-DD'), 13.5, '110/75', 68, 'Passed');

-- Donor 3 donates on 2026-10-01 (first donation -> PASSES cooldown check)
-- Donor 1 donates again on 2026-10-05 (well over 90 days after 2026-01-10).
-- Donor 4 donates on 2026-10-05 (well over 90 days after 2025-11-01).
INSERT INTO DONATION_RECORD (Donation_ID, Donor_ID, Donation_Date, Hemoglobin_Level, Blood_Pressure, Pulse_Rate, Medical_Screening_Result) VALUES
(3, 3, TO_DATE('2026-10-01', 'YYYY-MM-DD'), 15.0, '125/82', 74, 'Passed');

INSERT INTO DONATION_RECORD (Donation_ID, Donor_ID, Donation_Date, Hemoglobin_Level, Blood_Pressure, Pulse_Rate, Medical_Screening_Result) VALUES
(4, 1, TO_DATE('2026-10-05', 'YYYY-MM-DD'), 14.0, '120/80', 70, 'Passed');

INSERT INTO DONATION_RECORD (Donation_ID, Donor_ID, Donation_Date, Hemoglobin_Level, Blood_Pressure, Pulse_Rate, Medical_Screening_Result) VALUES
(5, 4, TO_DATE('2026-10-05', 'YYYY-MM-DD'), 13.8, '118/76', 72, 'Passed');

-- 4. Insert Blood Inventory
-- Unit 1: Collected on 2026-01-10, expired on 2026-02-14 -> Past shelf life, correctly marked 'Discarded'
INSERT INTO BLOOD_INVENTORY (Unit_ID, Donation_ID, Blood_Type, Component_Type, Volume_ML, Collection_Date, Expiry_Date, Status) VALUES
(1, 1, 'O+', 'Whole Blood', 450, TO_DATE('2026-01-10', 'YYYY-MM-DD'), TO_DATE('2026-02-14', 'YYYY-MM-DD'), 'Discarded');

-- Unit 2: AB+ Fresh Frozen Plasma from its own donation (frozen shelf life up to 1 year) -> Available
INSERT INTO BLOOD_INVENTORY (Unit_ID, Donation_ID, Blood_Type, Component_Type, Volume_ML, Collection_Date, Expiry_Date, Status) VALUES
(2, 5, 'AB+', 'Fresh Frozen Plasma', 200, TO_DATE('2026-10-05', 'YYYY-MM-DD'), TO_DATE('2027-10-05', 'YYYY-MM-DD'), 'Available');

-- Unit 3: A- Red Blood Cells from Donor 2 -> Available
INSERT INTO BLOOD_INVENTORY (Unit_ID, Donation_ID, Blood_Type, Component_Type, Volume_ML, Collection_Date, Expiry_Date, Status) VALUES
(3, 2, 'A-', 'Red Blood Cells', 300, TO_DATE('2026-10-01', 'YYYY-MM-DD'), TO_DATE('2026-11-12', 'YYYY-MM-DD'), 'Available');

-- Unit 4: B+ Whole Blood from Donor 3 -> Available
INSERT INTO BLOOD_INVENTORY (Unit_ID, Donation_ID, Blood_Type, Component_Type, Volume_ML, Collection_Date, Expiry_Date, Status) VALUES
(4, 3, 'B+', 'Whole Blood', 450, TO_DATE('2026-10-01', 'YYYY-MM-DD'), TO_DATE('2026-11-12', 'YYYY-MM-DD'), 'Available');

-- Unit 5: O+ Whole Blood from Donor 1's second donation on 2026-10-05 -> Active Available unit
INSERT INTO BLOOD_INVENTORY (Unit_ID, Donation_ID, Blood_Type, Component_Type, Volume_ML, Collection_Date, Expiry_Date, Status) VALUES
(5, 4, 'O+', 'Whole Blood', 450, TO_DATE('2026-10-05', 'YYYY-MM-DD'), TO_DATE('2026-11-16', 'YYYY-MM-DD'), 'Available');

-- 5. Insert Emergency Requests
INSERT INTO EMERGENCY_REQUEST (Request_ID, Hospital_ID, Required_Blood_Type, Required_Component, Units_Requested, Urgency_Level, Request_Status) VALUES
(1, 1, 'O+', 'Whole Blood', 1, 'Critical', 'Pending');

INSERT INTO EMERGENCY_REQUEST (Request_ID, Hospital_ID, Required_Blood_Type, Required_Component, Units_Requested, Urgency_Level, Request_Status) VALUES
(2, 2, 'AB-', 'Fresh Frozen Plasma', 2, 'Urgent', 'Pending');

INSERT INTO EMERGENCY_REQUEST (Request_ID, Hospital_ID, Required_Blood_Type, Required_Component, Units_Requested, Urgency_Level, Request_Status) VALUES
(3, 3, 'A-', 'Red Blood Cells', 1, 'Routine', 'Pending');

COMMIT;

-- Explicit IDs above are for readable, repeatable demo relationships. Advance Oracle identity
-- high-water marks so subsequent application-created rows receive IDs greater than the seed IDs.
-- START WITH LIMIT VALUE is supported by the project's Oracle 19c/21c/23ai target versions.
ALTER TABLE DONOR MODIFY (Donor_ID GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE));
ALTER TABLE HOSPITAL MODIFY (Hospital_ID GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE));
ALTER TABLE DONATION_RECORD MODIFY (Donation_ID GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE));
ALTER TABLE BLOOD_INVENTORY MODIFY (Unit_ID GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE));
ALTER TABLE EMERGENCY_REQUEST MODIFY (Request_ID GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE));
