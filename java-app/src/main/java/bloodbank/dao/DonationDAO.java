package bloodbank.dao;

import bloodbank.DBConnection;
import bloodbank.model.Donation;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DonationDAO {

    public List<Donation> getAllDonations()
            throws SQLException {

        List<Donation> donations = new ArrayList<>();

        String sql =
                "SELECT Donation_ID, Donor_ID, Donation_Date, " +
                "Hemoglobin_Level, Blood_Pressure, Pulse_Rate, " +
                "Medical_Screening_Result " +
                "FROM DONATION_RECORD " +
                "ORDER BY Donation_ID";

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            while (resultSet.next()) {
                donations.add(
                        mapResultSetToDonation(resultSet)
                );
            }
        }

        return donations;
    }

    public java.util.Optional<Donation> getDonationById(int donationId) throws SQLException {
        String sql =
                "SELECT Donation_ID, Donor_ID, Donation_Date, " +
                "Hemoglobin_Level, Blood_Pressure, Pulse_Rate, " +
                "Medical_Screening_Result " +
                "FROM DONATION_RECORD " +
                "WHERE Donation_ID = ?";

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, donationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return java.util.Optional.of(mapResultSetToDonation(resultSet));
                }
            }
        }
        return java.util.Optional.empty();
    }

    public void addDonation(Donation donation)
            throws SQLException {

        String sql =
                "INSERT INTO DONATION_RECORD " +
                "(Donor_ID, Donation_Date, Hemoglobin_Level, " +
                "Blood_Pressure, Pulse_Rate, " +
                "Medical_Screening_Result) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)
        ) {

            statement.setInt(
                    1,
                    donation.getDonorId()
            );

            statement.setDate(
                    2,
                    Date.valueOf(donation.getDonationDate())
            );

            statement.setDouble(
                    3,
                    donation.getHemoglobinLevel()
            );

            statement.setString(
                    4,
                    donation.getBloodPressure()
            );

            statement.setInt(
                    5,
                    donation.getPulseRate()
            );

            statement.setString(
                    6,
                    donation.getMedicalScreeningResult()
            );

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    donation.setDonationId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void updateDonation(Donation donation)
            throws SQLException {

        String sql =
                "UPDATE DONATION_RECORD SET " +
                "Donor_ID = ?, " +
                "Donation_Date = ?, " +
                "Hemoglobin_Level = ?, " +
                "Blood_Pressure = ?, " +
                "Pulse_Rate = ?, " +
                "Medical_Screening_Result = ? " +
                "WHERE Donation_ID = ?";

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    donation.getDonorId()
            );

            statement.setDate(
                    2,
                    Date.valueOf(donation.getDonationDate())
            );

            statement.setDouble(
                    3,
                    donation.getHemoglobinLevel()
            );

            statement.setString(
                    4,
                    donation.getBloodPressure()
            );

            statement.setInt(
                    5,
                    donation.getPulseRate()
            );

            statement.setString(
                    6,
                    donation.getMedicalScreeningResult()
            );

            statement.setInt(
                    7,
                    donation.getDonationId()
            );

            statement.executeUpdate();
        }
    }

    public void deleteDonation(int donationId)
            throws SQLException {

        String sql =
                "DELETE FROM DONATION_RECORD " +
                "WHERE Donation_ID = ?";

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, donationId);

            statement.executeUpdate();
        }
    }

    public List<Donation> searchDonations(
            String searchText
    ) throws SQLException {

        List<Donation> donations = new ArrayList<>();

        String sql =
                "SELECT Donation_ID, Donor_ID, Donation_Date, " +
                "Hemoglobin_Level, Blood_Pressure, Pulse_Rate, " +
                "Medical_Screening_Result " +
                "FROM DONATION_RECORD " +
                "WHERE TO_CHAR(Donation_ID) LIKE ? " +
                "OR TO_CHAR(Donor_ID) LIKE ? " +
                "OR LOWER(Blood_Pressure) LIKE ? " +
                "OR LOWER(Medical_Screening_Result) LIKE ? " +
                "ORDER BY Donation_ID";

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            String pattern =
                    "%" + searchText.toLowerCase() + "%";

            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            statement.setString(4, pattern);

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                while (resultSet.next()) {
                    donations.add(
                            mapResultSetToDonation(resultSet)
                    );
                }
            }
        }

        return donations;
    }

    private Donation mapResultSetToDonation(
            ResultSet resultSet
    ) throws SQLException {

        Date donationDate =
                resultSet.getDate("Donation_Date");

        return new Donation(
                resultSet.getInt("Donation_ID"),
                resultSet.getInt("Donor_ID"),
                donationDate != null
                        ? donationDate.toLocalDate()
                        : null,
                resultSet.getDouble("Hemoglobin_Level"),
                resultSet.getString("Blood_Pressure"),
                resultSet.getInt("Pulse_Rate"),
                resultSet.getString(
                        "Medical_Screening_Result"
                )
        );
    }
}