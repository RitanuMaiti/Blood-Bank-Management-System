package bloodbank.service;

import bloodbank.dao.BloodUnitDAO;
import bloodbank.model.BloodUnit;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class InventoryService {

    private final BloodUnitDAO bloodUnitDAO;

    public InventoryService() {
        this.bloodUnitDAO = new BloodUnitDAO();
    }

    public InventoryService(BloodUnitDAO dao) {
        this.bloodUnitDAO = dao;
    }

    public List<BloodUnit> getAllUnits() throws SQLException {
        return bloodUnitDAO.getAllUnits();
    }

    public Optional<BloodUnit> getUnitById(int unitId) throws SQLException {
        if (unitId <= 0) return Optional.empty();
        return bloodUnitDAO.getUnitById(unitId);
    }

    public void addUnit(BloodUnit unit) throws SQLException {
        validateUnit(unit);
        bloodUnitDAO.addUnit(unit);
    }

    public void updateUnit(BloodUnit unit) throws SQLException {
        if (unit.getUnitId() <= 0) {
            throw new IllegalArgumentException("Select a valid blood unit to update.");
        }
        validateUnit(unit);
        bloodUnitDAO.updateUnit(unit);
    }

    public void deleteUnit(int unitId) throws SQLException {
        if (unitId <= 0) {
            throw new IllegalArgumentException("Select a valid blood unit to delete.");
        }
        bloodUnitDAO.deleteUnit(unitId);
    }

    public List<BloodUnit> searchUnits(String searchText) throws SQLException {
        return bloodUnitDAO.searchUnits(searchText);
    }

    private void validateUnit(BloodUnit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("Blood unit cannot be null.");
        }
        if (unit.getDonationId() <= 0) {
            throw new IllegalArgumentException("Valid donation ID is required.");
        }
        if (unit.getBloodType() == null || unit.getBloodType().isBlank()) {
            throw new IllegalArgumentException("Blood group is required.");
        }
        if (unit.getComponentType() == null || unit.getComponentType().isBlank()) {
            throw new IllegalArgumentException("Component type is required.");
        }
        if (unit.getVolumeMl() <= 0) {
            throw new IllegalArgumentException("Volume must be greater than zero.");
        }
        if (unit.getCollectionDate() == null) {
            throw new IllegalArgumentException("Collection date is required.");
        }
        if (unit.getCollectionDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Collection date cannot be in the future.");
        }
        if (unit.getExpiryDate() == null) {
            throw new IllegalArgumentException("Expiry date is required.");
        }
        if (unit.getExpiryDate().isBefore(unit.getCollectionDate())) {
            throw new IllegalArgumentException("Expiry date cannot precede collection date.");
        }
        if (unit.getStatus() == null || unit.getStatus().isBlank()) {
            unit.setStatus("Available");
        }
        if (!List.of("Available", "Reserved", "Dispatched", "Discarded").contains(unit.getStatus())) {
            throw new IllegalArgumentException("Invalid unit status: " + unit.getStatus());
        }
    }
}