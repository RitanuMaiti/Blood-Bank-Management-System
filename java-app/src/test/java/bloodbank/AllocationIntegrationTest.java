package bloodbank;

import bloodbank.dao.*;
import bloodbank.model.*;
import bloodbank.service.MatchingService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class AllocationIntegrationTest {

    private MatchingService matchingService;
    private HospitalDAO hospitalDAO;
    private DonorDAO donorDAO;
    private DonationDAO donationDAO;
    private BloodUnitDAO bloodUnitDAO;
    private RequestDAO requestDAO;

    private int hospitalId;
    private int donorId;
    private int donationId;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseHelper.initTestDatabase();
        matchingService = new MatchingService();
        hospitalDAO = new HospitalDAO();
        donorDAO = new DonorDAO();
        donationDAO = new DonationDAO();
        bloodUnitDAO = new BloodUnitDAO();
        requestDAO = new RequestDAO();

        // Seed base parent records
        Hospital h = new Hospital("Central Hospital", "LIC-ALLOC-1", "500 Main St", "Vellore", "Dr. Rao", "9876543200", "central@hosp.com");
        hospitalDAO.addHospital(h);
        hospitalId = hospitalDAO.getAllHospitals().get(0).getHospitalId();

        Donor d = new Donor("Test", "Donor", "O+", LocalDate.of(1990, 1, 1), "M", "9876500000", "donor@test.com", "100 Test St", null, "Eligible");
        donorDAO.addDonor(d);
        donorId = donorDAO.getAllDonors().get(0).getDonorId();

        Donation don = new Donation(donorId, LocalDate.now(), 14.0, "120/80", 72, "Passed");
        donationDAO.addDonation(don);
        donationId = donationDAO.getAllDonations().get(0).getDonationId();
    }

    @AfterAll
    static void tearDown() {
        TestDatabaseHelper.teardown();
    }

    private int addUnit(String bloodType, String component, LocalDate expiry, String status) throws SQLException {
        BloodUnit u = new BloodUnit(
                donationId, bloodType, component, 450, LocalDate.now().minusDays(5), expiry, status
        );
        bloodUnitDAO.addUnit(u);
        return bloodUnitDAO.getAllUnits().get(bloodUnitDAO.getAllUnits().size() - 1).getUnitId();
    }

    private int createRequest(String bloodType, String component, int units) throws SQLException {
        BloodRequest r = new BloodRequest(
                0, hospitalId, bloodType, component, units, "Urgent", LocalDateTime.now(), "Pending"
        );
        return requestDAO.addAndReturnId(r);
    }

    @Test
    @DisplayName("Complete allocation: 1 unit requested, 1 matching available -> Fulfilled")
    void testCompleteAllocation() throws SQLException {
        int unitId = addUnit("O+", "Red Blood Cells", LocalDate.now().plusDays(20), "Available");
        int reqId = createRequest("O+", "Red Blood Cells", 1);

        MatchingService.AllocationResult result = matchingService.allocate(reqId);

        assertEquals(1, result.getUnitsAllocated());
        assertEquals("Fulfilled", result.getNewStatus());
        assertEquals(1, result.getTotalAllocatedForRequest());
        assertTrue(result.getAllocatedUnitIds().contains(unitId));

        // Verify inventory unit is marked 'Dispatched'
        BloodUnit unit = bloodUnitDAO.getUnitById(unitId).orElseThrow();
        assertEquals("Dispatched", unit.getStatus());

        // Verify request is marked 'Fulfilled' in database
        BloodRequest req = requestDAO.getById(reqId).orElseThrow();
        assertEquals("Fulfilled", req.getRequestStatus());
    }

    @Test
    @DisplayName("Partial allocation: 3 units requested, 1 available -> Partially_Fulfilled")
    void testPartialAllocation() throws SQLException {
        int unitId = addUnit("O+", "Red Blood Cells", LocalDate.now().plusDays(20), "Available");
        int reqId = createRequest("O+", "Red Blood Cells", 3);

        MatchingService.AllocationResult result = matchingService.allocate(reqId);

        assertEquals(1, result.getUnitsAllocated());
        assertEquals("Partially_Fulfilled", result.getNewStatus());
        assertEquals(1, result.getTotalAllocatedForRequest());

        BloodRequest req = requestDAO.getById(reqId).orElseThrow();
        assertEquals("Partially_Fulfilled", req.getRequestStatus());

        // Now add 2 more units and allocate again
        int u2 = addUnit("O+", "Red Blood Cells", LocalDate.now().plusDays(25), "Available");
        int u3 = addUnit("O-", "Red Blood Cells", LocalDate.now().plusDays(30), "Available"); // O- compatible with O+

        MatchingService.AllocationResult result2 = matchingService.allocate(reqId);

        assertEquals(2, result2.getUnitsAllocated());
        assertEquals("Fulfilled", result2.getNewStatus());
        assertEquals(3, result2.getTotalAllocatedForRequest());

        BloodRequest reqFinal = requestDAO.getById(reqId).orElseThrow();
        assertEquals("Fulfilled", reqFinal.getRequestStatus());
    }

    @Test
    @DisplayName("Expired units must be excluded from allocation")
    void testExpiredUnitsExcluded() throws SQLException {
        // Expired yesterday
        int expiredUnitId = addUnit("O+", "Red Blood Cells", LocalDate.now().minusDays(1), "Available");
        int reqId = createRequest("O+", "Red Blood Cells", 1);

        MatchingService.AllocationResult result = matchingService.allocate(reqId);

        assertEquals(0, result.getUnitsAllocated());
        assertEquals("Pending", result.getNewStatus());

        // Expired unit remains untouched
        BloodUnit unit = bloodUnitDAO.getUnitById(expiredUnitId).orElseThrow();
        assertEquals("Available", unit.getStatus());
    }

    @Test
    @DisplayName("Incompatible blood units must be excluded from allocation")
    void testIncompatibleUnitsExcluded() throws SQLException {
        // B+ cannot be given to O- recipient for cellular blood
        addUnit("B+", "Red Blood Cells", LocalDate.now().plusDays(20), "Available");
        int reqId = createRequest("O-", "Red Blood Cells", 1);

        MatchingService.AllocationResult result = matchingService.allocate(reqId);

        assertEquals(0, result.getUnitsAllocated());
        assertEquals("Pending", result.getNewStatus());
    }

    @Test
    @DisplayName("Compatible blood group allocation (O- cellular given to A+ recipient)")
    void testCompatibleDonorAllocation() throws SQLException {
        int unitId = addUnit("O-", "Red Blood Cells", LocalDate.now().plusDays(15), "Available");
        int reqId = createRequest("A+", "Red Blood Cells", 1);

        MatchingService.AllocationResult result = matchingService.allocate(reqId);

        assertEquals(1, result.getUnitsAllocated());
        assertEquals("Fulfilled", result.getNewStatus());
        assertTrue(result.getAllocatedUnitIds().contains(unitId));
    }

    @Test
    @DisplayName("Prevent duplicate allocation: dispatched unit cannot be allocated to another request")
    void testPreventDuplicateAllocation() throws SQLException {
        int unitId = addUnit("O+", "Red Blood Cells", LocalDate.now().plusDays(20), "Available");
        int req1 = createRequest("O+", "Red Blood Cells", 1);
        int req2 = createRequest("O+", "Red Blood Cells", 1);

        // Allocate to first request
        matchingService.allocate(req1);

        // Attempt allocation to second request
        MatchingService.AllocationResult result2 = matchingService.allocate(req2);
        assertEquals(0, result2.getUnitsAllocated());
        assertEquals("Pending", result2.getNewStatus());
    }

    @Test
    @DisplayName("Cancelled request cannot receive allocations")
    void testCancelledRequestRejected() throws SQLException {
        addUnit("O+", "Red Blood Cells", LocalDate.now().plusDays(20), "Available");
        int reqId = createRequest("O+", "Red Blood Cells", 1);
        requestDAO.updateStatus(reqId, "Cancelled");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                matchingService.allocate(reqId));
        assertTrue(ex.getMessage().contains("Cancelled"));
    }

    @Test
    @DisplayName("Already fulfilled request cannot receive duplicate allocations")
    void testFulfilledRequestRejected() throws SQLException {
        addUnit("O+", "Red Blood Cells", LocalDate.now().plusDays(20), "Available");
        int reqId = createRequest("O+", "Red Blood Cells", 1);
        matchingService.allocate(reqId);

        // Try allocating again
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                matchingService.allocate(reqId));
        assertTrue(ex.getMessage().contains("Fulfilled"));
    }
}
