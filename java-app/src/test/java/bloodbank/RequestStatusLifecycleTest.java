package bloodbank;

import bloodbank.dao.HospitalDAO;
import bloodbank.dao.RequestDAO;
import bloodbank.model.BloodRequest;
import bloodbank.model.Hospital;
import bloodbank.service.RequestService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class RequestStatusLifecycleTest {

    private RequestService requestService;
    private RequestDAO requestDAO;
    private int testHospitalId;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseHelper.initTestDatabase();
        requestDAO = new RequestDAO();
        requestService = new RequestService(requestDAO);

        HospitalDAO hospitalDAO = new HospitalDAO();
        Hospital h = new Hospital("Emergency Test Center", "LIC-TEST-1", "99 Emergency St", "Vellore", "Dr. John", "9876543299", "em@hosp.com");
        hospitalDAO.addHospital(h);
        testHospitalId = hospitalDAO.getAllHospitals().get(0).getHospitalId();
    }

    @AfterAll
    static void tearDown() {
        TestDatabaseHelper.teardown();
    }

    private int createRequest(String initialStatus) throws SQLException {
        BloodRequest r = new BloodRequest(
                0, testHospitalId, "O+", "Red Blood Cells", 2, "Urgent", LocalDateTime.now(), initialStatus
        );
        int reqId = requestDAO.addAndReturnId(r);
        if (!"Pending".equalsIgnoreCase(initialStatus)) {
            requestDAO.updateStatus(reqId, initialStatus);
        }
        return reqId;
    }

    @Test
    @DisplayName("Valid transitions from Pending: to Partially_Fulfilled, Fulfilled, and Cancelled")
    void testValidTransitionsFromPending() throws SQLException {
        int req1 = createRequest("Pending");
        assertDoesNotThrow(() -> requestService.updateStatus(req1, "Partially_Fulfilled"));
        assertEquals("Partially_Fulfilled", requestService.getById(req1).orElseThrow().getRequestStatus());

        int req2 = createRequest("Pending");
        assertDoesNotThrow(() -> requestService.updateStatus(req2, "Fulfilled"));
        assertEquals("Fulfilled", requestService.getById(req2).orElseThrow().getRequestStatus());

        int req3 = createRequest("Pending");
        assertDoesNotThrow(() -> requestService.updateStatus(req3, "Cancelled"));
        assertEquals("Cancelled", requestService.getById(req3).orElseThrow().getRequestStatus());
    }

    @Test
    @DisplayName("Valid transitions from Partially_Fulfilled: to Fulfilled and Cancelled")
    void testValidTransitionsFromPartiallyFulfilled() throws SQLException {
        int req1 = createRequest("Partially_Fulfilled");
        assertDoesNotThrow(() -> requestService.updateStatus(req1, "Fulfilled"));
        assertEquals("Fulfilled", requestService.getById(req1).orElseThrow().getRequestStatus());

        int req2 = createRequest("Partially_Fulfilled");
        assertDoesNotThrow(() -> requestService.updateStatus(req2, "Cancelled"));
        assertEquals("Cancelled", requestService.getById(req2).orElseThrow().getRequestStatus());
    }

    @Test
    @DisplayName("Invalid transition: Partially_Fulfilled cannot be reverted to Pending")
    void testPartiallyFulfilledCannotRevertToPending() throws SQLException {
        int req = createRequest("Partially_Fulfilled");
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                requestService.updateStatus(req, "Pending"));
        assertTrue(ex.getMessage().contains("cannot be reverted to Pending"));
    }

    @Test
    @DisplayName("Invalid transition: Fulfilled is a terminal state and cannot transition")
    void testFulfilledCannotTransition() throws SQLException {
        int req = createRequest("Fulfilled");

        assertThrows(IllegalStateException.class, () ->
                requestService.updateStatus(req, "Pending"));
        assertThrows(IllegalStateException.class, () ->
                requestService.updateStatus(req, "Partially_Fulfilled"));
        assertThrows(IllegalStateException.class, () ->
                requestService.updateStatus(req, "Cancelled"));
    }

    @Test
    @DisplayName("Invalid transition: Cancelled is a terminal state and cannot be reopened")
    void testCancelledCannotTransition() throws SQLException {
        int req = createRequest("Cancelled");

        assertThrows(IllegalStateException.class, () ->
                requestService.updateStatus(req, "Pending"));
        assertThrows(IllegalStateException.class, () ->
                requestService.updateStatus(req, "Partially_Fulfilled"));
        assertThrows(IllegalStateException.class, () ->
                requestService.updateStatus(req, "Fulfilled"));
    }

    @Test
    @DisplayName("Stale status update must not overwrite a concurrent transition")
    void testConcurrentStatusChangeRejected() throws SQLException {
        int req = createRequest("Pending");
        requestDAO.updateStatus(req, "Cancelled");

        assertFalse(requestDAO.updateStatusIfCurrent(req, "Pending", "Fulfilled"));
        assertEquals("Cancelled", requestService.getById(req).orElseThrow().getRequestStatus());
    }

    @Test
    @DisplayName("Invalid status string must be rejected")
    void testInvalidStatusStringRejected() throws SQLException {
        int req = createRequest("Pending");
        assertThrows(IllegalArgumentException.class, () ->
                requestService.updateStatus(req, "UNKNOWN_STATUS"));
        assertThrows(IllegalArgumentException.class, () ->
                requestService.updateStatus(req, ""));
        assertThrows(IllegalArgumentException.class, () ->
                requestService.updateStatus(req, null));
    }
}
