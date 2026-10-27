package bloodbank.service;

import bloodbank.dao.BloodUnitDAO;
import bloodbank.model.BloodUnit;

import java.sql.SQLException;
import java.util.List;

public class InventoryService {

    private final BloodUnitDAO bloodUnitDAO;

    public InventoryService() {
        bloodUnitDAO =
                new BloodUnitDAO();
    }


    public List<BloodUnit> getAllUnits()
            throws SQLException {

        return bloodUnitDAO.getAllUnits();
    }


    public void addUnit(BloodUnit unit)
            throws SQLException {

        bloodUnitDAO.addUnit(unit);
    }


    public void updateUnit(BloodUnit unit)
            throws SQLException {

        bloodUnitDAO.updateUnit(unit);
    }


    public void deleteUnit(int unitId)
            throws SQLException {

        bloodUnitDAO.deleteUnit(unitId);
    }


    public List<BloodUnit> searchUnits(
            String searchText
    ) throws SQLException {

        return bloodUnitDAO.searchUnits(
                searchText
        );
    }
}