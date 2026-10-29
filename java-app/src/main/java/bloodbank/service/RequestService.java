package bloodbank.service;

import bloodbank.dao.RequestDAO;
import bloodbank.model.BloodRequest;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class RequestService {

    public static final List<String> VALID_STATUSES = List.of(
            "Pending", "Partially_Fulfilled", "Fulfilled", "Cancelled"
    );

    public static final List<String> VALID_URGENCIES = List.of(
            "Routine", "Urgent", "Critical"
    );

    private final RequestDAO dao;

    public RequestService() {
        this.dao = new RequestDAO();
    }

    public RequestService(RequestDAO dao) {
        this.dao = dao;
    }

    public List<BloodRequest> getAll() throws SQLException {
        return dao.getAll();
    }

    public Optional<BloodRequest> getById(int id) throws SQLException {
        if (id <= 0) return Optional.empty();
        return dao.getById(id);
    }

    public void create(BloodRequest r) throws SQLException {
        if (r.getHospitalId() <= 0) {
            throw new IllegalArgumentException("Enter a valid Hospital ID.");
        }
        if (r.getUnitsRequested() <= 0) {
            throw new IllegalArgumentException("Units requested must be greater than zero.");
        }
        if (r.getRequiredBloodType() == null || r.getRequiredBloodType().isBlank()) {
            throw new IllegalArgumentException("Select a required blood group.");
        }
        if (r.getRequiredComponent() == null || r.getRequiredComponent().isBlank()) {
            throw new IllegalArgumentException("Select a required component.");
        }
        if (r.getUrgencyLevel() == null || !VALID_URGENCIES.contains(r.getUrgencyLevel())) {
            throw new IllegalArgumentException("Invalid urgency level: " + r.getUrgencyLevel());
        }
        if (r.getRequestStatus() == null || r.getRequestStatus().isBlank()) {
            r.setRequestStatus("Pending");
        }
        dao.add(r);
    }

    /**
     * Validates and executes state transition for blood requests.
     * Allowed transitions:
     *   - Pending -> Partially_Fulfilled, Fulfilled, Cancelled
     *   - Partially_Fulfilled -> Fulfilled, Cancelled
     *   - Fulfilled -> (Terminal state, cannot transition)
     *   - Cancelled -> (Terminal state, cannot transition)
     */
    public void updateStatus(int id, String newStatus) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Select a valid request.");
        }
        if (newStatus == null || !VALID_STATUSES.contains(newStatus)) {
            throw new IllegalArgumentException("Invalid request status: " + newStatus);
        }

        Optional<BloodRequest> reqOpt = dao.getById(id);
        if (reqOpt.isEmpty()) {
            throw new SQLException("Request #" + id + " not found.");
        }

        String currentStatus = reqOpt.get().getRequestStatus();
        validateTransition(currentStatus, newStatus);

        if (!dao.updateStatusIfCurrent(id, currentStatus, newStatus)) {
            throw new SQLException("Request status changed concurrently. Refresh the request and try again.");
        }
    }

    public void validateTransition(String currentStatus, String newStatus) {
        if (currentStatus.equals(newStatus)) {
            return; // No-op transition
        }

        if ("Fulfilled".equalsIgnoreCase(currentStatus)) {
            throw new IllegalStateException("Request is already Fulfilled and cannot be modified.");
        }

        if ("Cancelled".equalsIgnoreCase(currentStatus)) {
            throw new IllegalStateException("Request is Cancelled and cannot be reopened.");
        }

        if ("Partially_Fulfilled".equalsIgnoreCase(currentStatus) && "Pending".equalsIgnoreCase(newStatus)) {
            throw new IllegalStateException("Partially fulfilled request cannot be reverted to Pending.");
        }
    }

    public void delete(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Select a valid request to delete.");
        }
        dao.delete(id);
    }
}
