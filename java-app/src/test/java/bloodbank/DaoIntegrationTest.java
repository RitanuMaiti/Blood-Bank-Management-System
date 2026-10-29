package bloodbank;

import bloodbank.dao.*;
import bloodbank.model.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DaoIntegrationTest {

    private HospitalDAO hospitalDAO;
    private DonorDAO donorDAO;
    private DonationDAO donationDAO;
    private BloodUnitDAO bloodUnitDAO;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseHelper.initTestDatabase();
        hospitalDAO = new HospitalDAO();
        donorDAO = new DonorDAO();
        donationDAO = new DonationDAO();
        bloodUnitDAO = new BloodUnitDAO();
    }

    @AfterAll
    static void tearDown() {
        TestDatabaseHelper.teardown();
    }

    @Test
    @DisplayName("Hospital DAO: add, getById, update, search, delete")
    void testHospitalCrud() throws SQLException {
        Hospital h = new Hospital("St. Jude Hospital", "LIC-CRUD-1", "100 Medical Way", "Chennai", "Dr. Paul", "9876543201", "stjude@hosp.com");
        hospitalDAO.addHospital(h);

        List<Hospital> all = hospitalDAO.getAllHospitals();
        assertFalse(all.isEmpty());
        int hId = all.get(0).getHospitalId();

        Hospital fetched = hospitalDAO.getHospitalById(hId).orElseThrow();
        assertEquals("St. Jude Hospital", fetched.getHospitalName());
        assertEquals("100 Medical Way", fetched.getAddress());
        assertEquals("Chennai", fetched.getCity());

        // Update
        fetched.setHospitalName("St. Jude Memorial Hospital");
        fetched.setCity("Vellore");
        hospitalDAO.updateHospital(fetched);

        Hospital updated = hospitalDAO.getHospitalById(hId).orElseThrow();
        assertEquals("St. Jude Memorial Hospital", updated.getHospitalName());
        assertEquals("Vellore", updated.getCity());

        // Search
        List<Hospital> searchResults = hospitalDAO.searchHospitals("Memorial");
        assertEquals(1, searchResults.size());

        // Delete
        hospitalDAO.deleteHospital(hId);
        assertTrue(hospitalDAO.getHospitalById(hId).isEmpty());
    }

    @Test
    @DisplayName("Donor DAO: add, getById, update, search, delete")
    void testDonorCrud() throws SQLException {
        Donor d = new Donor("Vikram", "Malhotra", "B+", LocalDate.of(1993, 7, 21), "M", "9811223344", "vikram@test.com", "55 Park St", null, "Eligible");
        donorDAO.addDonor(d);

        List<Donor> all = donorDAO.getAllDonors();
        assertFalse(all.isEmpty());
        int dId = all.get(0).getDonorId();

        Donor fetched = donorDAO.getDonorById(dId).orElseThrow();
        assertEquals("Vikram", fetched.getFirstName());
        assertEquals("B+", fetched.getBloodType());

        // Update
        fetched.setEligibilityStatus("Cooldown");
        fetched.setLastDonationDate(LocalDate.now());
        donorDAO.updateDonor(fetched);

        Donor updated = donorDAO.getDonorById(dId).orElseThrow();
        assertEquals("Cooldown", updated.getEligibilityStatus());
        assertEquals(LocalDate.now(), updated.getLastDonationDate());

        // Search
        List<Donor> search = donorDAO.searchDonors("Malhotra");
        assertEquals(1, search.size());

        // Delete
        donorDAO.deleteDonor(dId);
        assertTrue(donorDAO.getDonorById(dId).isEmpty());
    }

    @Test
    @DisplayName("Foreign Key Integrity: cannot insert donation for non-existent donor")
    void testForeignKeyIntegrityDonation() {
        Donation d = new Donation(999999, LocalDate.now(), 14.0, "120/80", 72, "Passed");
        assertThrows(SQLException.class, () -> donationDAO.addDonation(d));
    }

    @Test
    @DisplayName("Foreign Key Integrity: cannot insert inventory unit for non-existent donation")
    void testForeignKeyIntegrityInventory() {
        BloodUnit u = new BloodUnit(888888, "O+", "Whole Blood", 450, LocalDate.now(), LocalDate.now().plusDays(35), "Available");
        assertThrows(SQLException.class, () -> bloodUnitDAO.addUnit(u));
    }
}
