package bloodbank.dao;

import bloodbank.DBConnection;
import bloodbank.model.BloodRequest;
import java.sql.*;
import java.util.*;

public class RequestDAO {
    public List<BloodRequest> getAll() throws SQLException {
        String sql = "SELECT REQUEST_ID,HOSPITAL_ID,REQUIRED_BLOOD_TYPE,REQUIRED_COMPONENT,UNITS_REQUESTED,URGENCY_LEVEL,REQUEST_TIMESTAMP,REQUEST_STATUS FROM EMERGENCY_REQUEST ORDER BY CASE URGENCY_LEVEL WHEN 'Critical' THEN 1 WHEN 'Urgent' THEN 2 ELSE 3 END, REQUEST_TIMESTAMP DESC";
        List<BloodRequest> result = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                Timestamp ts = r.getTimestamp("REQUEST_TIMESTAMP");
                result.add(new BloodRequest(
                    r.getInt("REQUEST_ID"), r.getInt("HOSPITAL_ID"),
                    r.getString("REQUIRED_BLOOD_TYPE"), r.getString("REQUIRED_COMPONENT"),
                    r.getInt("UNITS_REQUESTED"), r.getString("URGENCY_LEVEL"),
                    ts == null ? null : ts.toLocalDateTime(), r.getString("REQUEST_STATUS")));
            }
        }
        return result;
    }

    public void add(BloodRequest r) throws SQLException {
        String sql = "INSERT INTO EMERGENCY_REQUEST (HOSPITAL_ID,REQUIRED_BLOOD_TYPE,REQUIRED_COMPONENT,UNITS_REQUESTED,URGENCY_LEVEL,REQUEST_TIMESTAMP,REQUEST_STATUS) VALUES (?,?,?,?,?,SYSTIMESTAMP,'Pending')";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, r.getHospitalId());
            p.setString(2, r.getRequiredBloodType());
            p.setString(3, r.getRequiredComponent());
            p.setInt(4, r.getUnitsRequested());
            p.setString(5, r.getUrgencyLevel());
            p.executeUpdate();
        }
    }

    public void updateStatus(int id, String status) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(
                 "UPDATE EMERGENCY_REQUEST SET REQUEST_STATUS=? WHERE REQUEST_ID=?")) {
            p.setString(1, status);
            p.setInt(2, id);
            if (p.executeUpdate() == 0) throw new SQLException("Request not found.");
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(
                 "DELETE FROM EMERGENCY_REQUEST WHERE REQUEST_ID=?")) {
            p.setInt(1, id);
            if (p.executeUpdate() == 0) throw new SQLException("Request not found.");
        }
    }
}
