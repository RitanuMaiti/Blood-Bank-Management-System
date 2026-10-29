package bloodbank;

import bloodbank.dao.DonationDAO;
import bloodbank.dao.DonorDAO;
import bloodbank.model.Donation;
import bloodbank.model.Donor;
import bloodbank.service.DonationService;
import bloodbank.service.DonorService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class DonorEligibilityTest {

    private DonorService donorService;
    private DonationService donationService;
    private DonorDAO donorDAO;
    private DonationDAO donationDAO;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseHelper.initTestDatabase();
        donorDAO = new DonorDAO();
        donationDAO = new DonationDAO();
        donorService = new DonorService();
        donationService = new DonationService(donationDAO, donorDAO);
    }

    @AfterAll
    static void tearDown() {
        TestDatabaseHelper.teardown();
    }

    @Test
    @DisplayName("Underage donors (younger than 18) must be rejected")
    void testUnderageDonorRejected() {
        Donor underage = new Donor(
                "Junior", "Doe", "O+",
                LocalDate.now().minusYears(17), // 17 years old
                "M", "9998887770", "junior@example.com",
                "123 Street", null, "Eligible"
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                donorService.addDonor(underage));
        assertTrue(ex.getMessage().contains("18 years old"));
    }

    @Test
    @DisplayName("First-time donor with no previous donation is eligible")
    void testFirstTimeDonorDonation() throws SQLException {
        Donor donor = new Donor(
                "John", "Doe", "A+",
                LocalDate.of(1990, 1, 1),
                "M", "9123456799", "john.doe@example.com",
                "456 Avenue", null, "Eligible"
        );
        donorDAO.addDonor(donor);
        int donorId = donorDAO.getAllDonors().get(0).getDonorId();

        Donation donation = new Donation(
                donorId, LocalDate.now(), 14.0, "120/80", 72, "Passed"
        );

        assertDoesNotThrow(() -> donationService.addDonation(donation));

        // Verify donor is synchronized
        Donor updated = donorDAO.getDonorById(donorId).orElseThrow();
        assertEquals(LocalDate.now(), updated.getLastDonationDate());
        assertEquals("Cooldown", updated.getEligibilityStatus());
    }

    @Test
    @DisplayName("Donation within 90-day cooldown period must be rejected")
    void testCooldownViolationRejected() throws SQLException {
        Donor donor = new Donor(
                "Jane", "Smith", "O+",
                LocalDate.of(1992, 4, 15),
                "F", "9123456788", "jane@example.com",
                "789 Boulevard",
                LocalDate.now().minusDays(45), // Donated 45 days ago (< 90 days)
                "Cooldown"
        );
        donorDAO.addDonor(donor);
        int donorId = donorDAO.getAllDonors().get(0).getDonorId();

        Donation donation = new Donation(
                donorId, LocalDate.now(), 13.8, "115/75", 68, "Passed"
        );

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                donationService.addDonation(donation));
        assertTrue(ex.getMessage().contains("cooldown period"));
        assertTrue(ex.getMessage().contains("45 days"));
    }

    @Test
    @DisplayName("Donation exactly 90 days or more after last donation is allowed")
    void testEligibleAfter90Days() throws SQLException {
        Donor donor = new Donor(
                "Robert", "Brown", "B+",
                LocalDate.of(1988, 6, 20),
                "M", "9123456777", "robert@example.com",
                "101 Road",
                LocalDate.now().minusDays(90), // Donated exactly 90 days ago
                "Eligible"
        );
        donorDAO.addDonor(donor);
        int donorId = donorDAO.getAllDonors().get(0).getDonorId();

        Donation donation = new Donation(
                donorId, LocalDate.now(), 14.5, "122/80", 70, "Passed"
        );

        assertDoesNotThrow(() -> donationService.addDonation(donation));

        Donor updated = donorDAO.getDonorById(donorId).orElseThrow();
        assertEquals(LocalDate.now(), updated.getLastDonationDate());
        assertEquals("Cooldown", updated.getEligibilityStatus());
    }

    @Test
    @DisplayName("Donation date in the future must be rejected")
    void testFutureDonationDateRejected() throws SQLException {
        Donor donor = new Donor(
                "Alice", "Green", "AB+",
                LocalDate.of(1995, 3, 10),
                "F", "9123456766", "alice@example.com",
                "202 Park", null, "Eligible"
        );
        donorDAO.addDonor(donor);
        int donorId = donorDAO.getAllDonors().get(0).getDonorId();

        Donation futureDonation = new Donation(
                donorId, LocalDate.now().plusDays(2), 14.0, "120/80", 72, "Passed"
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                donationService.addDonation(futureDonation));
        assertTrue(ex.getMessage().contains("future"));
    }

    @Test
    @DisplayName("Donation date preceding donor's last recorded donation must be rejected")
    void testPrecedingDonationDateRejected() throws SQLException {
        Donor donor = new Donor(
                "David", "Miller", "O-",
                LocalDate.of(1985, 8, 12),
                "M", "9123456755", "david@example.com",
                "303 Lane",
                LocalDate.of(2026, 5, 1),
                "Cooldown"
        );
        donorDAO.addDonor(donor);
        int donorId = donorDAO.getAllDonors().get(0).getDonorId();

        Donation precedingDonation = new Donation(
                donorId, LocalDate.of(2026, 4, 1), 13.5, "120/80", 70, "Passed"
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                donationService.addDonation(precedingDonation));
        assertTrue(ex.getMessage().contains("cannot precede"));
    }

    @Test
    @DisplayName("Donor with permanent deferral cannot donate")
    void testPermanentDeferralRejected() throws SQLException {
        Donor donor = new Donor(
                "Mark", "Taylor", "A-",
                LocalDate.of(1991, 10, 5),
                "M", "9123456744", "mark@example.com",
                "404 Drive",
                LocalDate.now().minusDays(150),
                "Permanent_Deferral"
        );
        donorDAO.addDonor(donor);
        int donorId = donorDAO.getAllDonors().get(0).getDonorId();

        Donation donation = new Donation(
                donorId, LocalDate.now(), 14.0, "120/80", 72, "Passed"
        );

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                donationService.addDonation(donation));
        assertTrue(ex.getMessage().contains("Permanent Deferral"));
    }

    @Test
    @DisplayName("Hemoglobin below 12.5 must be rejected")
    void testLowHemoglobinRejected() throws SQLException {
        Donor donor = new Donor(
                "Sam", "Wilson", "O+",
                LocalDate.of(1994, 2, 25),
                "M", "9123456733", "sam@example.com",
                "505 Crescent", null, "Eligible"
        );
        donorDAO.addDonor(donor);
        int donorId = donorDAO.getAllDonors().get(0).getDonorId();

        Donation lowHb = new Donation(
                donorId, LocalDate.now(), 11.8, "118/76", 75, "Passed"
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                donationService.addDonation(lowHb));
        assertTrue(ex.getMessage().contains("12.5"));
    }
}
