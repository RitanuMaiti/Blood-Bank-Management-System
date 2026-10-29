package bloodbank.service;

import bloodbank.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public static class AllocationResult {
        private final int unitsAllocated;
        private final int totalAllocatedForRequest;
        private final int unitsRequested;
        private final String newStatus;
        private final List<Integer> allocatedUnitIds;

        public AllocationResult(int unitsAllocated, int totalAllocatedForRequest, int unitsRequested, String newStatus, List<Integer> allocatedUnitIds) {
            this.unitsAllocated = unitsAllocated;
            this.totalAllocatedForRequest = totalAllocatedForRequest;
            this.unitsRequested = unitsRequested;
            this.newStatus = newStatus;
            this.allocatedUnitIds = allocatedUnitIds;
        }

        public int getUnitsAllocated() { return unitsAllocated; }
        public int getTotalAllocatedForRequest() { return totalAllocatedForRequest; }
        public int getUnitsRequested() { return unitsRequested; }
        public String getNewStatus() { return newStatus; }
        public List<Integer> getAllocatedUnitIds() { return allocatedUnitIds; }
    }

    /**
     * Component-aware biological compatibility evaluation.
     * Note: This academic project implements established transfusion medicine baselines.
     * Clinical applications must be validated against local clinical guidelines.
     */
    public static boolean isCompatible(String donor, String recipient, String component) {
        if (donor == null || recipient == null || component == null) {
            return false;
        }

        String normComponent = normalizeComponent(component);

        // 1. Platelets: Exact ABO/Rh match preferred
        if ("Platelets".equalsIgnoreCase(normComponent)) {
            return donor.equalsIgnoreCase(recipient);
        }

        // 2. Whole blood contains both red cells and donor plasma, so use a conservative exact ABO/Rh match.
        // Do not treat O- whole blood as universally compatible unless a validated low-titer O protocol exists.
        if ("Whole Blood".equalsIgnoreCase(normComponent)) {
            return donor.equalsIgnoreCase(recipient);
        }

        // 3. Plasma / Fresh Frozen Plasma: Acellular compatibility (anti-A/anti-B in donor plasma)
        // AB is the universal plasma donor (no anti-A or anti-B antibodies)
        // O is the universal plasma recipient
        if ("Fresh Frozen Plasma".equalsIgnoreCase(normComponent)) {
            String rec = recipient.toUpperCase();
            String don = donor.toUpperCase();
            return switch (rec) {
                case "AB+", "AB-" -> don.equals("AB+") || don.equals("AB-");
                case "A+", "A-" -> don.equals("A+") || don.equals("A-") || don.equals("AB+") || don.equals("AB-");
                case "B+", "B-" -> don.equals("B+") || don.equals("B-") || don.equals("AB+") || don.equals("AB-");
                case "O+", "O-" -> true; // O can safely receive plasma from any group
                default -> false;
            };
        }

        // 4. Red-cell components (Red Blood Cells, Packed Red Cells):
        // O- is the universal red-cell donor; AB+ is the universal red-cell recipient.
        return switch (recipient.toUpperCase()) {
            case "O-" -> donor.equalsIgnoreCase("O-");
            case "O+" -> donor.equalsIgnoreCase("O-") || donor.equalsIgnoreCase("O+");
            case "A-" -> donor.equalsIgnoreCase("O-") || donor.equalsIgnoreCase("A-");
            case "A+" -> Arrays.asList("O-", "O+", "A-", "A+").contains(donor.toUpperCase());
            case "B-" -> donor.equalsIgnoreCase("O-") || donor.equalsIgnoreCase("B-");
            case "B+" -> Arrays.asList("O-", "O+", "B-", "B+").contains(donor.toUpperCase());
            case "AB-" -> Arrays.asList("O-", "A-", "B-", "AB-").contains(donor.toUpperCase());
            case "AB+" -> Arrays.asList("O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+").contains(donor.toUpperCase());
            default -> false;
        };
    }

    public static String normalizeComponent(String comp) {
        if (comp == null) return "";
        String lower = comp.trim().toLowerCase();
        if (lower.contains("platelet")) return "Platelets";
        if (lower.contains("plasma")) return "Fresh Frozen Plasma";
        if (lower.contains("red") || lower.contains("packed")) return "Red Blood Cells";
        if (lower.contains("whole")) return "Whole Blood";
        return comp.trim();
    }

    public static boolean componentsMatch(String comp1, String comp2) {
        return normalizeComponent(comp1).equalsIgnoreCase(normalizeComponent(comp2));
    }

    /**
     * Finds candidate blood units that are available, non-expired, and compatible with the request.
     */
    public List<Match> findMatches(int requestId) throws SQLException {
        String query = "SELECT REQUIRED_BLOOD_TYPE, REQUIRED_COMPONENT FROM EMERGENCY_REQUEST WHERE REQUEST_ID=? AND REQUEST_STATUS IN ('Pending','Partially_Fulfilled')";
        String units = "SELECT UNIT_ID,BLOOD_TYPE,COMPONENT_TYPE,VOLUME_ML,EXPIRY_DATE FROM BLOOD_INVENTORY WHERE STATUS='Available' AND EXPIRY_DATE >= CURRENT_DATE ORDER BY EXPIRY_DATE, UNIT_ID";

        String recipient;
        String component;

        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(query)) {
            p.setInt(1, requestId);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new SQLException("Active request #" + requestId + " not found or already closed.");
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
                if (componentsMatch(component, type) && isCompatible(donor, recipient, component)) {
                    matches.add(new Match(r.getInt("UNIT_ID"), donor, type,
                        r.getInt("VOLUME_ML"), r.getDate("EXPIRY_DATE")));
                }
            }
        }
        return matches;
    }

    /**
     * Atomically allocates available compatible units to the emergency request.
     * Prevents over-allocation, handles partial fulfillment, excludes expired units,
     * locks candidate rows to prevent concurrency race conditions, and rolls back on failure.
     */
    public AllocationResult allocate(int requestId) throws SQLException {
        String requestSql = "SELECT REQUIRED_BLOOD_TYPE,REQUIRED_COMPONENT,UNITS_REQUESTED,REQUEST_STATUS FROM EMERGENCY_REQUEST WHERE REQUEST_ID=? FOR UPDATE";
        String candidateSql = "SELECT UNIT_ID,BLOOD_TYPE,COMPONENT_TYPE FROM BLOOD_INVENTORY WHERE STATUS='Available' AND EXPIRY_DATE >= CURRENT_DATE ORDER BY EXPIRY_DATE,UNIT_ID FOR UPDATE";
        String countSql = "SELECT COUNT(*) FROM ALLOCATION_DISPATCH WHERE REQUEST_ID=?";
        String reserveSql = "UPDATE BLOOD_INVENTORY SET STATUS='Dispatched' WHERE UNIT_ID=? AND STATUS='Available'";
        String dispatchSql = "INSERT INTO ALLOCATION_DISPATCH (REQUEST_ID,UNIT_ID,DISPATCH_TIMESTAMP,DELIVERY_STATUS) VALUES (?, ?, CURRENT_TIMESTAMP, 'In Transit')";
        String updateStatusSql = "UPDATE EMERGENCY_REQUEST SET REQUEST_STATUS=? WHERE REQUEST_ID=?";

        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);

            try {
                String recipient, component, status;
                int requested;

                try (PreparedStatement p = c.prepareStatement(requestSql)) {
                    p.setInt(1, requestId);
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next()) {
                            throw new SQLException("Request #" + requestId + " not found.");
                        }
                        recipient = r.getString(1);
                        component = r.getString(2);
                        requested = r.getInt(3);
                        status = r.getString(4);
                    }
                }

                if ("Cancelled".equalsIgnoreCase(status)) {
                    throw new IllegalStateException("Cannot allocate to Cancelled request #" + requestId + ".");
                }
                if ("Fulfilled".equalsIgnoreCase(status)) {
                    throw new IllegalStateException("Request #" + requestId + " is already Fulfilled.");
                }

                int alreadyDispatched = 0;
                try (PreparedStatement p = c.prepareStatement(countSql)) {
                    p.setInt(1, requestId);
                    try (ResultSet r = p.executeQuery()) {
                        if (r.next()) {
                            alreadyDispatched = r.getInt(1);
                        }
                    }
                }

                int remainingNeeded = requested - alreadyDispatched;
                if (remainingNeeded <= 0) {
                    try (PreparedStatement p = c.prepareStatement(updateStatusSql)) {
                        p.setString(1, "Fulfilled");
                        p.setInt(2, requestId);
                        p.executeUpdate();
                    }
                    c.commit();
                    return new AllocationResult(0, alreadyDispatched, requested, "Fulfilled", List.of());
                }

                List<Integer> allocatedIds = new ArrayList<>();
                int allocatedNow = 0;

                try (PreparedStatement p = c.prepareStatement(candidateSql);
                     ResultSet r = p.executeQuery();
                     PreparedStatement reserve = c.prepareStatement(reserveSql);
                     PreparedStatement dispatch = c.prepareStatement(dispatchSql)) {

                    while (r.next() && allocatedNow < remainingNeeded) {
                        String donor = r.getString("BLOOD_TYPE");
                        String unitComponent = r.getString("COMPONENT_TYPE");

                        if (!componentsMatch(component, unitComponent) || !isCompatible(donor, recipient, component)) {
                            continue;
                        }

                        int unitId = r.getInt("UNIT_ID");
                        reserve.setInt(1, unitId);

                        if (reserve.executeUpdate() == 1) {
                            dispatch.setInt(1, requestId);
                            dispatch.setInt(2, unitId);
                            dispatch.executeUpdate();
                            allocatedIds.add(unitId);
                            allocatedNow++;
                        }
                    }
                }

                int totalAllocated = alreadyDispatched + allocatedNow;
                String newStatus = totalAllocated >= requested ? "Fulfilled"
                        : totalAllocated > 0 ? "Partially_Fulfilled" : "Pending";

                try (PreparedStatement p = c.prepareStatement(updateStatusSql)) {
                    p.setString(1, newStatus);
                    p.setInt(2, requestId);
                    p.executeUpdate();
                }

                c.commit();
                return new AllocationResult(allocatedNow, totalAllocated, requested, newStatus, allocatedIds);

            } catch (Exception ex) {
                c.rollback();
                if (ex instanceof SQLException) throw (SQLException) ex;
                if (ex instanceof RuntimeException) throw (RuntimeException) ex;
                throw new SQLException("Allocation transaction failed: " + ex.getMessage(), ex);
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    /**
     * Backward-compatible allocation method returning the number of newly allocated units.
     */
    public int allocateAvailableUnits(int requestId) throws SQLException {
        AllocationResult result = allocate(requestId);
        return result.getUnitsAllocated();
    }
}
