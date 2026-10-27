package bloodbank.service;

import bloodbank.dao.DonationDAO;
import bloodbank.model.Donation;

import java.sql.SQLException;
import java.util.List;

public class DonationService {

    private final DonationDAO donationDAO;

    public DonationService() {
        donationDAO =
                new DonationDAO();
    }

    public List<Donation> getAllDonations()
            throws SQLException {

        return donationDAO.getAllDonations();
    }

    public void addDonation(Donation donation)
            throws SQLException {

        donationDAO.addDonation(donation);
    }

    public void updateDonation(Donation donation)
            throws SQLException {

        donationDAO.updateDonation(donation);
    }

    public void deleteDonation(int donationId)
            throws SQLException {

        donationDAO.deleteDonation(donationId);
    }

    public List<Donation> searchDonations(
            String searchText
    ) throws SQLException {

        return donationDAO.searchDonations(
                searchText
        );
    }
}