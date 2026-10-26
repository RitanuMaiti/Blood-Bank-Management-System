-- 1. Insert Hospitals
INSERT INTO HOSPITAL (Hospital_Name, License_Number, Street, City, Contact_Person, Phone_Number, Email) VALUES 
('City General Hospital', 'LIC-1001', '123 Health Ave', 'Vellore', 'Dr. Ramesh', '9876543210', 'emergency@citygen.com');
INSERT INTO HOSPITAL (Hospital_Name, License_Number, Street, City, Contact_Person, Phone_Number, Email) VALUES 
('Apollo Care Center', 'LIC-1002', '45 Apollo Street', 'Chennai', 'Dr. Smitha', '9876543211', 'bloodreq@apollo.com');

-- 2. Insert Donors
INSERT INTO DONOR (First_Name, Last_Name, Blood_Type, DOB, Gender, Contact_Number, Email, Address, Last_Donation_Date, Eligibility_Status) VALUES 
('Amit', 'Patel', 'O+', TO_DATE('1995-05-14', 'YYYY-MM-DD'), 'M', '9123456780', 'amit.p@email.com', 'Gandhi Road, Vellore', TO_DATE('2026-01-10', 'YYYY-MM-DD'), 'Eligible');
INSERT INTO DONOR (First_Name, Last_Name, Blood_Type, DOB, Gender, Contact_Number, Email, Address, Last_Donation_Date, Eligibility_Status) VALUES 
('Priya', 'Singh', 'A-', TO_DATE('1998-11-22', 'YYYY-MM-DD'), 'F', '9123456781', 'priya.s@email.com', 'Anna Nagar, Chennai', TO_DATE('2026-09-15', 'YYYY-MM-DD'), 'Cooldown');
INSERT INTO DONOR (First_Name, Last_Name, Blood_Type, DOB, Gender, Contact_Number, Email, Address, Last_Donation_Date, Eligibility_Status) VALUES 
('Rahul', 'Sharma', 'B+', TO_DATE('2001-03-30', 'YYYY-MM-DD'), 'M', '9123456782', 'rahul.s@email.com', 'Katpadi, Vellore', NULL, 'Eligible');

-- 3. Insert Donation Records
INSERT INTO DONATION_RECORD (Donor_ID, Donation_Date, Hemoglobin_Level, Blood_Pressure, Pulse_Rate, Medical_Screening_Result) VALUES 
(1, TO_DATE('2026-01-10', 'YYYY-MM-DD'), 14.2, '120/80', 72, 'Passed');
INSERT INTO DONATION_RECORD (Donor_ID, Donation_Date, Hemoglobin_Level, Blood_Pressure, Pulse_Rate, Medical_Screening_Result) VALUES 
(2, TO_DATE('2026-09-15', 'YYYY-MM-DD'), 13.5, '110/75', 68, 'Passed');

-- 4. Insert Blood Inventory
INSERT INTO BLOOD_INVENTORY (Donation_ID, Blood_Type, Component_Type, Volume_ML, Collection_Date, Expiry_Date, Status) VALUES 
(1, 'O+', 'Whole Blood', 450, TO_DATE('2026-01-10', 'YYYY-MM-DD'), TO_DATE('2026-02-14', 'YYYY-MM-DD'), 'Available');
INSERT INTO BLOOD_INVENTORY (Donation_ID, Blood_Type, Component_Type, Volume_ML, Collection_Date, Expiry_Date, Status) VALUES 
(1, 'O+', 'Plasma', 200, TO_DATE('2026-01-10', 'YYYY-MM-DD'), TO_DATE('2027-01-10', 'YYYY-MM-DD'), 'Available');
INSERT INTO BLOOD_INVENTORY (Donation_ID, Blood_Type, Component_Type, Volume_ML, Collection_Date, Expiry_Date, Status) VALUES 
(2, 'A-', 'Packed Red Cells', 300, TO_DATE('2026-09-15', 'YYYY-MM-DD'), TO_DATE('2026-10-27', 'YYYY-MM-DD'), 'Available');

-- 5. Insert Emergency Requests
INSERT INTO EMERGENCY_REQUEST (Hospital_ID, Required_Blood_Type, Required_Component, Units_Requested, Urgency_Level, Request_Status) VALUES 
(1, 'O+', 'Whole Blood', 1, 'Critical', 'Pending');
INSERT INTO EMERGENCY_REQUEST (Hospital_ID, Required_Blood_Type, Required_Component, Units_Requested, Urgency_Level, Request_Status) VALUES 
(2, 'AB-', 'Plasma', 2, 'Urgent', 'Pending');

COMMIT;
