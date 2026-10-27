package bloodbank.service;

import bloodbank.dao.HospitalDAO;
import bloodbank.model.Hospital;

import java.sql.SQLException;
import java.util.List;

public class HospitalService {

    private final HospitalDAO hospitalDAO;

    public HospitalService() {
        hospitalDAO = new HospitalDAO();
    }

    public List<Hospital> getAllHospitals()
            throws SQLException {
        return hospitalDAO.getAllHospitals();
    }

    public void addHospital(Hospital hospital)
            throws SQLException {
        hospitalDAO.addHospital(hospital);
    }

    public void updateHospital(Hospital hospital)
            throws SQLException {
        hospitalDAO.updateHospital(hospital);
    }

    public void deleteHospital(int hospitalId)
            throws SQLException {
        hospitalDAO.deleteHospital(hospitalId);
    }

    public List<Hospital> searchHospitals(
            String searchText
    ) throws SQLException {
        return hospitalDAO.searchHospitals(searchText);
    }
}