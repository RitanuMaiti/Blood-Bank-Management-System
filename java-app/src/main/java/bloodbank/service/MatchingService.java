package bloodbank.service;

import bloodbank.DBConnection;
import java.sql.*;
import java.util.*;

public class MatchingService {

    public static class Match {
        private final int unitId;
        private final String bloodType;
        private final String component;
        private final int volumeMl;
        private final java.sql.Date expiryDate;

        public Match(int id, String blood, String component, int volume, java.sql.Date expiry) {
            this.unitId = id;
            this.bloodType = blood;
            this.component = component;
            this.volumeMl = volume;
            this.expiryDate = expiry;
        }

        public int getUnitId() { return unitId; }
        public String getBloodType() { return bloodType; }
        public String getComponent() { return component; }
        public int getVolumeMl() { return volumeMl; }
        public java.sql.Date getExpiryDate() { return expiryDate; }
    }

    private boolean compatible(String donor, String recipient, String component) {
        // Conservative policy: use exact group matching for plasma and platelets.
        if (component.equals("Platelets") || component.equals("Fresh Frozen Plasma"))
            return donor.equals(recipient);

        switch (recipient) {
            case "O-": return donor.equals("O-");
            case "O+": return donor.equals("O-") || donor.equals("O+");
            case "A-": return donor.equals("O-") || donor.equals("A-");
            case "A+": return Arrays.asList("O-", "O+", "A-", "A+").contains(donor);
            case "B-": return donor.equals("O-") || donor.equals("B-");
            case "B+": return Arrays.asList("O-", "O+", "B-", "B+").contains(donor);
            case "AB-": return Arrays.asList("O-", "A-", "B-", "AB-").contains(donor);
            case "AB+": return true;
            default: return false;
        }
    }

    public List<Match> findMatches(int requestId) throws SQLException {
        String query = "SELECT REQUIRED_BLOOD_TYPE, REQUIRED_COMPONENT FROM EMERGENCY_REQUEST WHERE REQUEST_ID=? AND REQUEST_STATUS IN ('Pending','Partially_Fulfilled')";
        String units = "SELECT UNIT_ID,BLOOD_TYPE,COMPONENT_TYPE,VOLUME_ML,EXPIRY_DATE FROM BLOOD_INVENTORY WHERE STATUS='Available' AND EXPIRY_DATE >= TRUNC(SYSDATE) ORDER BY EXPIRY_DATE";

        String recipient;
        String component;

        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(query)) {
            p.setInt(1, requestId);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new SQLException("Open request not found.");
                recipient = r.getString(1);
                component = r.getString(2);
            }
        }

        List<Match> matches = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(units);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                String donor = r.getString("BLOOD_TYPE");
                String type = r.getString("COMPONENT_TYPE");
                if (component.equals(type) && compatible(donor, recipient, component)) {
                    matches.add(new Match(r.getInt("UNIT_ID"), donor, type,
                        r.getInt("VOLUME_ML"), r.getDate("EXPIRY_DATE")));
                }
            }
        }
        return matches;
    }

    public int allocateAvailableUnits(int requestId) throws SQLException {
        String requestSql = "SELECT REQUIRED_BLOOD_TYPE,REQUIRED_COMPONENT,UNITS_REQUESTED,REQUEST_STATUS FROM EMERGENCY_REQUEST WHERE REQUEST_ID=? FOR UPDATE";
        String candidateSql = "SELECT UNIT_ID,BLOOD_TYPE,COMPONENT_TYPE FROM BLOOD_INVENTORY WHERE STATUS='Available' AND EXPIRY_DATE >= TRUNC(SYSDATE) ORDER BY EXPIRY_DATE,UNIT_ID FOR UPDATE";
        String countSql = "SELECT COUNT(*) FROM ALLOCATION_DISPATCH WHERE REQUEST_ID=?";
        String reserveSql = "UPDATE BLOOD_INVENTORY SET STATUS='Dispatched' WHERE UNIT_ID=? AND STATUS='Available'";
        String dispatchSql = "INSERT INTO ALLOCATION_DISPATCH (REQUEST_ID,UNIT_ID,DISPATCH_TIMESTAMP,DELIVERY_STATUS) VALUES (?, ?, SYSTIMESTAMP, 'In Transit')";

        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);

            try {
                String recipient, component, status;
                int requested;

                try (PreparedStatement p = c.prepareStatement(requestSql)) {
                    p.setInt(1, requestId);
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next()) throw new SQLException("Request not found.");
                        recipient = r.getString(1);
                        component = r.getString(2);
                        requested = r.getInt(3);
                        status = r.getString(4);
                    }
                }

                if (status.equals("Cancelled") || status.equals("Fulfilled"))
                    throw new SQLException("Request is already closed.");

                int already;
                try (PreparedStatement p = c.prepareStatement(countSql)) {
                    p.setInt(1, requestId);
                    try (ResultSet r = p.executeQuery()) {
                        r.next();
                        already = r.getInt(1);
                    }
                }

                int remaining = requested - already;
                if (remaining <= 0) throw new SQLException("Request already fulfilled.");

                int allocated = 0;

                try (PreparedStatement p = c.prepareStatement(candidateSql);
                     ResultSet r = p.executeQuery();
                     PreparedStatement reserve = c.prepareStatement(reserveSql);
                     PreparedStatement dispatch = c.prepareStatement(dispatchSql)) {

                    while (r.next() && allocated < remaining) {
                        String donor = r.getString("BLOOD_TYPE");
                        String unitComponent = r.getString("COMPONENT_TYPE");

                        if (!component.equals(unitComponent) || !compatible(donor, recipient, component))
                            continue;

                        int unitId = r.getInt("UNIT_ID");
                        reserve.setInt(1, unitId);

                        if (reserve.executeUpdate() == 1) {
                            dispatch.setInt(1, requestId);
                            dispatch.setInt(2, unitId);
                            dispatch.executeUpdate();
                            allocated++;
                        }
                    }
                }

                int total = already + allocated;
                String newStatus = total >= requested ? "Fulfilled"
                    : total > 0 ? "Partially_Fulfilled" : "Pending";

                try (PreparedStatement p = c.prepareStatement(
                    "UPDATE EMERGENCY_REQUEST SET REQUEST_STATUS=? WHERE REQUEST_ID=?")) {
                    p.setString(1, newStatus);
                    p.setInt(2, requestId);
                    p.executeUpdate();
                }

                c.commit();
                return allocated;

            } catch (Exception ex) {
                c.rollback();
                if (ex instanceof SQLException) throw (SQLException) ex;
                throw new SQLException("Allocation failed.", ex);
            } finally {
                c.setAutoCommit(true);
            }
        }
    }
}
