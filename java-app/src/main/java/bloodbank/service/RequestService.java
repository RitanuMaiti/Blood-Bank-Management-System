package bloodbank.service;

import bloodbank.dao.RequestDAO;
import bloodbank.model.BloodRequest;
import java.sql.SQLException;
import java.util.*;

public class RequestService {
    private final RequestDAO dao = new RequestDAO();

    public List<BloodRequest> getAll() throws SQLException {
        return dao.getAll();
    }

    public void create(BloodRequest r) throws SQLException {
        if (r.getHospitalId() <= 0) throw new IllegalArgumentException("Enter a valid Hospital ID.");
        if (r.getUnitsRequested() <= 0) throw new IllegalArgumentException("Units requested must be greater than zero.");
        if (r.getRequiredBloodType() == null || r.getRequiredComponent() == null || r.getUrgencyLevel() == null)
            throw new IllegalArgumentException("Complete all request fields.");
        if (!List.of("Routine", "Urgent", "Critical").contains(r.getUrgencyLevel()))
            throw new IllegalArgumentException("Invalid urgency level.");
        dao.add(r);
    }

    public void updateStatus(int id, String status) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("Select a request.");
        if (!List.of("Pending", "Partially_Fulfilled", "Fulfilled", "Cancelled").contains(status))
            throw new IllegalArgumentException("Invalid status.");
        dao.updateStatus(id, status);
    }

    public void delete(int id) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("Select a request.");
        dao.delete(id);
    }
}
