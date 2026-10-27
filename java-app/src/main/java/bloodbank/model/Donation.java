package bloodbank.model;

import java.time.LocalDate;

public class Donation {

    private int donationId;
    private int donorId;
    private LocalDate donationDate;
    private double hemoglobinLevel;
    private String bloodPressure;
    private int pulseRate;
    private String medicalScreeningResult;

    public Donation() {
    }

    public Donation(
            int donationId,
            int donorId,
            LocalDate donationDate,
            double hemoglobinLevel,
            String bloodPressure,
            int pulseRate,
            String medicalScreeningResult
    ) {
        this.donationId = donationId;
        this.donorId = donorId;
        this.donationDate = donationDate;
        this.hemoglobinLevel = hemoglobinLevel;
        this.bloodPressure = bloodPressure;
        this.pulseRate = pulseRate;
        this.medicalScreeningResult = medicalScreeningResult;
    }

    public Donation(
            int donorId,
            LocalDate donationDate,
            double hemoglobinLevel,
            String bloodPressure,
            int pulseRate,
            String medicalScreeningResult
    ) {
        this.donorId = donorId;
        this.donationDate = donationDate;
        this.hemoglobinLevel = hemoglobinLevel;
        this.bloodPressure = bloodPressure;
        this.pulseRate = pulseRate;
        this.medicalScreeningResult = medicalScreeningResult;
    }

    public int getDonationId() {
        return donationId;
    }

    public void setDonationId(int donationId) {
        this.donationId = donationId;
    }

    public int getDonorId() {
        return donorId;
    }

    public void setDonorId(int donorId) {
        this.donorId = donorId;
    }

    public LocalDate getDonationDate() {
        return donationDate;
    }

    public void setDonationDate(LocalDate donationDate) {
        this.donationDate = donationDate;
    }

    public double getHemoglobinLevel() {
        return hemoglobinLevel;
    }

    public void setHemoglobinLevel(double hemoglobinLevel) {
        this.hemoglobinLevel = hemoglobinLevel;
    }

    public String getBloodPressure() {
        return bloodPressure;
    }

    public void setBloodPressure(String bloodPressure) {
        this.bloodPressure = bloodPressure;
    }

    public int getPulseRate() {
        return pulseRate;
    }

    public void setPulseRate(int pulseRate) {
        this.pulseRate = pulseRate;
    }

    public String getMedicalScreeningResult() {
        return medicalScreeningResult;
    }

    public void setMedicalScreeningResult(
            String medicalScreeningResult
    ) {
        this.medicalScreeningResult =
                medicalScreeningResult;
    }
}