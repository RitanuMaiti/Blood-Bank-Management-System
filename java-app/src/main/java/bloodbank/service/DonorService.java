package bloodbank.service;

import bloodbank.dao.DonorDAO;
import bloodbank.model.Donor;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class DonorService {

    private final DonorDAO dao = new DonorDAO();

    public List<Donor> getAllDonors() throws SQLException {
        return dao.getAllDonors();
    }

    public java.util.Optional<Donor> getDonorById(int id) throws SQLException {
        if (id <= 0) return java.util.Optional.empty();
        return dao.getDonorById(id);
    }

    public List<Donor> searchDonors(String term) throws SQLException {
        return dao.searchDonors(term == null ? "" : term.trim());
    }

    public void addDonor(Donor d) throws SQLException {
        validate(d);
        dao.addDonor(d);
    }

    public void updateDonor(Donor d) throws SQLException {
        if (d.getDonorId() <= 0)
            throw new IllegalArgumentException("Select a donor first.");
        validate(d);
        dao.updateDonor(d);
    }

    public void deleteDonor(int id) throws SQLException {
        if (id <= 0)
            throw new IllegalArgumentException("Select a donor first.");
        dao.deleteDonor(id);
    }

    private void validate(Donor d) {
        if (d.getFirstName() == null || d.getFirstName().isBlank()
                || d.getLastName() == null || d.getLastName().isBlank())
            throw new IllegalArgumentException("Enter the donor's full name.");

        if (d.getBloodType() == null || d.getBloodType().isBlank())
            throw new IllegalArgumentException("Select a blood group.");

        if (d.getDob() == null || !d.getDob().isBefore(LocalDate.now().minusYears(18)))
            throw new IllegalArgumentException("Donor must be at least 18 years old.");

        if (d.getContactNumber() == null || d.getContactNumber().isBlank())
            throw new IllegalArgumentException("Enter a contact number.");

        if (d.getAddress() == null || d.getAddress().isBlank())
            throw new IllegalArgumentException("Enter an address.");

        if (d.getEligibilityStatus() == null)
            d.setEligibilityStatus("Eligible");
    }
}
