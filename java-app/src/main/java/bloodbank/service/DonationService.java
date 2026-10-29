package bloodbank.service;

import bloodbank.dao.DonationDAO;
import bloodbank.dao.DonorDAO;
import bloodbank.model.Donation;
import bloodbank.model.Donor;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

public class DonationService {

    /**
     * Standard mandatory inter-donation interval for whole blood donation.
     * Note: In clinical practice, cooldown intervals may vary by component type
     * (e.g., 56-90 days for whole blood, 14 days for plasma, 7 days for platelets)
     * and jurisdictional regulations. For this system, 90 days is the default baseline.
     */
    public static final int DEFAULT_COOLDOWN_DAYS = 90;

    private final DonationDAO donationDAO;
    private final DonorDAO donorDAO;
    private int cooldownDays = DEFAULT_COOLDOWN_DAYS;

    public DonationService() {
        this.donationDAO = new DonationDAO();
        this.donorDAO = new DonorDAO();
    }

    public DonationService(DonationDAO donationDAO, DonorDAO donorDAO) {
        this.donationDAO = donationDAO;
        this.donorDAO = donorDAO;
    }

    public int getCooldownDays() {
        return cooldownDays;
    }

    public void setCooldownDays(int days) {
        if (days < 0) {
            throw new IllegalArgumentException("Cooldown days cannot be negative.");
        }
        this.cooldownDays = days;
    }

    public List<Donation> getAllDonations() throws SQLException {
        return donationDAO.getAllDonations();
    }

    public Optional<Donation> getDonationById(int donationId) throws SQLException {
        if (donationId <= 0) return Optional.empty();
        return donationDAO.getDonationById(donationId);
    }

    public void addDonation(Donation donation) throws SQLException {
        validateDonation(donation);

        // Fetch donor to verify existence and eligibility rules
        Optional<Donor> donorOpt = donorDAO.getDonorById(donation.getDonorId());
        if (donorOpt.isEmpty()) {
            throw new IllegalArgumentException("Donor ID " + donation.getDonorId() + " does not exist.");
        }

        Donor donor = donorOpt.get();

        // Check deferral status
        if ("Permanent_Deferral".equalsIgnoreCase(donor.getEligibilityStatus())) {
            throw new IllegalStateException("Donor is marked with Permanent Deferral and cannot donate.");
        }

        // Check 90-day cooldown period
        LocalDate lastDonation = donor.getLastDonationDate();
        if (lastDonation != null) {
            if (donation.getDonationDate().isBefore(lastDonation)) {
                throw new IllegalArgumentException(
                        "Donation date cannot precede the donor's previous donation date (" + lastDonation + ")."
                );
            }

            long daysBetween = ChronoUnit.DAYS.between(lastDonation, donation.getDonationDate());
            if (daysBetween < cooldownDays) {
                throw new IllegalStateException(
                        "Donor is currently in the " + cooldownDays + "-day cooldown period. Only "
                                + daysBetween + " days have elapsed since last donation on " + lastDonation + "."
                );
            }
        }

        // Persist donation record
        donationDAO.addDonation(donation);

        // Synchronize donor record (updates last donation date and eligibility)
        donor.setLastDonationDate(donation.getDonationDate());
        donor.setEligibilityStatus("Cooldown");
        try {
            donorDAO.updateDonor(donor);
        } catch (SQLException ex) {
            // Log/rethrow if donor synchronization fails
            throw new SQLException("Donation was recorded but failed to synchronize donor status: " + ex.getMessage(), ex);
        }
    }

    public void updateDonation(Donation donation) throws SQLException {
        if (donation.getDonationId() <= 0) {
            throw new IllegalArgumentException("Select a valid donation record to update.");
        }
        validateDonation(donation);
        donationDAO.updateDonation(donation);
    }

    public void deleteDonation(int donationId) throws SQLException {
        if (donationId <= 0) {
            throw new IllegalArgumentException("Select a valid donation record to delete.");
        }
        donationDAO.deleteDonation(donationId);
    }

    public List<Donation> searchDonations(String searchText) throws SQLException {
        return donationDAO.searchDonations(searchText);
    }

    private void validateDonation(Donation donation) {
        if (donation == null) {
            throw new IllegalArgumentException("Donation record cannot be null.");
        }
        if (donation.getDonorId() <= 0) {
            throw new IllegalArgumentException("Valid donor ID is required.");
        }
        if (donation.getDonationDate() == null) {
            throw new IllegalArgumentException("Donation date is required.");
        }
        if (donation.getDonationDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Donation date cannot be in the future.");
        }
        if (donation.getHemoglobinLevel() < 12.5) {
            throw new IllegalArgumentException("Hemoglobin level must be at least 12.5 g/dL.");
        }
        if (donation.getPulseRate() <= 0) {
            throw new IllegalArgumentException("Pulse rate must be greater than zero.");
        }
        if (donation.getBloodPressure() == null || donation.getBloodPressure().isBlank()) {
            throw new IllegalArgumentException("Blood pressure reading is required.");
        }
        if (donation.getMedicalScreeningResult() == null || donation.getMedicalScreeningResult().isBlank()) {
            donation.setMedicalScreeningResult("Passed");
        }
    }
}