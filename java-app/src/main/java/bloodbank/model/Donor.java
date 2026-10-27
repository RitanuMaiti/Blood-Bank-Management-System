package bloodbank.model;

import java.time.LocalDate;

public class Donor {

    private int donorId;
    private String firstName;
    private String lastName;
    private String bloodType;
    private LocalDate dob;
    private String gender;
    private String contactNumber;
    private String email;
    private String address;
    private LocalDate lastDonationDate;
    private String eligibilityStatus;

    public Donor() {
    }

    public Donor(
            int donorId,
            String firstName,
            String lastName,
            String bloodType,
            LocalDate dob,
            String gender,
            String contactNumber,
            String email,
            String address,
            LocalDate lastDonationDate,
            String eligibilityStatus
    ) {
        this.donorId = donorId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.bloodType = bloodType;
        this.dob = dob;
        this.gender = gender;
        this.contactNumber = contactNumber;
        this.email = email;
        this.address = address;
        this.lastDonationDate = lastDonationDate;
        this.eligibilityStatus = eligibilityStatus;
    }

    public Donor(
            String firstName,
            String lastName,
            String bloodType,
            LocalDate dob,
            String gender,
            String contactNumber,
            String email,
            String address,
            LocalDate lastDonationDate,
            String eligibilityStatus
    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.bloodType = bloodType;
        this.dob = dob;
        this.gender = gender;
        this.contactNumber = contactNumber;
        this.email = email;
        this.address = address;
        this.lastDonationDate = lastDonationDate;
        this.eligibilityStatus = eligibilityStatus;
    }

    public int getDonorId() {
        return donorId;
    }

    public void setDonorId(int donorId) {
        this.donorId = donorId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getLastDonationDate() {
        return lastDonationDate;
    }

    public void setLastDonationDate(LocalDate lastDonationDate) {
        this.lastDonationDate = lastDonationDate;
    }

    public String getEligibilityStatus() {
        return eligibilityStatus;
    }

    public void setEligibilityStatus(String eligibilityStatus) {
        this.eligibilityStatus = eligibilityStatus;
    }
}