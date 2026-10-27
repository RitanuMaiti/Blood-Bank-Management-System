package bloodbank.model;

import java.time.LocalDate;

public class BloodUnit {

    private int unitId;
    private int donationId;
    private String bloodType;
    private String componentType;
    private int volumeMl;
    private LocalDate collectionDate;
    private LocalDate expiryDate;
    private String status;

    public BloodUnit() {
    }

    public BloodUnit(
            int unitId,
            int donationId,
            String bloodType,
            String componentType,
            int volumeMl,
            LocalDate collectionDate,
            LocalDate expiryDate,
            String status
    ) {
        this.unitId = unitId;
        this.donationId = donationId;
        this.bloodType = bloodType;
        this.componentType = componentType;
        this.volumeMl = volumeMl;
        this.collectionDate = collectionDate;
        this.expiryDate = expiryDate;
        this.status = status;
    }

    public BloodUnit(
            int donationId,
            String bloodType,
            String componentType,
            int volumeMl,
            LocalDate collectionDate,
            LocalDate expiryDate,
            String status
    ) {
        this.donationId = donationId;
        this.bloodType = bloodType;
        this.componentType = componentType;
        this.volumeMl = volumeMl;
        this.collectionDate = collectionDate;
        this.expiryDate = expiryDate;
        this.status = status;
    }

    public int getUnitId() {
        return unitId;
    }

    public void setUnitId(int unitId) {
        this.unitId = unitId;
    }

    public int getDonationId() {
        return donationId;
    }

    public void setDonationId(int donationId) {
        this.donationId = donationId;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public int getVolumeMl() {
        return volumeMl;
    }

    public void setVolumeMl(int volumeMl) {
        this.volumeMl = volumeMl;
    }

    public LocalDate getCollectionDate() {
        return collectionDate;
    }

    public void setCollectionDate(LocalDate collectionDate) {
        this.collectionDate = collectionDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}