package bloodbank.dao;

import bloodbank.DBConnection;
import bloodbank.model.Donor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DonorDAO {

    private Donor map(ResultSet r) throws SQLException {
        Donor d = new Donor();
        d.setDonorId(r.getInt("DONOR_ID"));
        d.setFirstName(r.getString("FIRST_NAME"));
        d.setLastName(r.getString("LAST_NAME"));
        d.setBloodType(r.getString("BLOOD_TYPE"));

        Date dob = r.getDate("DOB");
        d.setDob(dob == null ? null : dob.toLocalDate());

        d.setGender(r.getString("GENDER"));
        d.setContactNumber(r.getString("CONTACT_NUMBER"));
        d.setEmail(r.getString("EMAIL"));
        d.setAddress(r.getString("ADDRESS"));

        Date last = r.getDate("LAST_DONATION_DATE");
        d.setLastDonationDate(last == null ? null : last.toLocalDate());

        d.setEligibilityStatus(r.getString("ELIGIBILITY_STATUS"));
        return d;
    }

    public List<Donor> getAllDonors() throws SQLException {
        String sql = "SELECT * FROM DONOR ORDER BY DONOR_ID DESC";
        List<Donor> list = new ArrayList<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) list.add(map(r));
        }
        return list;
    }

    public void addDonor(Donor d) throws SQLException {
        String sql = """
            INSERT INTO DONOR
            (FIRST_NAME, LAST_NAME, BLOOD_TYPE, DOB, GENDER,
             CONTACT_NUMBER, EMAIL, ADDRESS, LAST_DONATION_DATE,
             ELIGIBILITY_STATUS)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, d.getFirstName());
            p.setString(2, d.getLastName());
            p.setString(3, d.getBloodType());
            p.setDate(4, Date.valueOf(d.getDob()));
            p.setString(5, d.getGender());
            p.setString(6, d.getContactNumber());

            if (d.getEmail() == null || d.getEmail().isBlank())
                p.setNull(7, Types.VARCHAR);
            else
                p.setString(7, d.getEmail());

            p.setString(8, d.getAddress());

            if (d.getLastDonationDate() == null)
                p.setNull(9, Types.DATE);
            else
                p.setDate(9, Date.valueOf(d.getLastDonationDate()));

            p.setString(10, d.getEligibilityStatus());
            p.executeUpdate();
        }
    }

    public void updateDonor(Donor d) throws SQLException {
        String sql = """
            UPDATE DONOR SET FIRST_NAME=?, LAST_NAME=?, BLOOD_TYPE=?,
            DOB=?, GENDER=?, CONTACT_NUMBER=?, EMAIL=?, ADDRESS=?,
            LAST_DONATION_DATE=?, ELIGIBILITY_STATUS=?
            WHERE DONOR_ID=?
            """;

        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, d.getFirstName());
            p.setString(2, d.getLastName());
            p.setString(3, d.getBloodType());
            p.setDate(4, Date.valueOf(d.getDob()));
            p.setString(5, d.getGender());
            p.setString(6, d.getContactNumber());

            if (d.getEmail() == null || d.getEmail().isBlank())
                p.setNull(7, Types.VARCHAR);
            else
                p.setString(7, d.getEmail());

            p.setString(8, d.getAddress());

            if (d.getLastDonationDate() == null)
                p.setNull(9, Types.DATE);
            else
                p.setDate(9, Date.valueOf(d.getLastDonationDate()));

            p.setString(10, d.getEligibilityStatus());
            p.setInt(11, d.getDonorId());

            if (p.executeUpdate() == 0)
                throw new SQLException("Donor not found.");
        }
    }

    public void deleteDonor(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(
                 "DELETE FROM DONOR WHERE DONOR_ID=?")) {
            p.setInt(1, id);
            if (p.executeUpdate() == 0)
                throw new SQLException("Donor not found.");
        }
    }

    public List<Donor> searchDonors(String term) throws SQLException {
        String sql = """
            SELECT * FROM DONOR
            WHERE LOWER(FIRST_NAME) LIKE ?
               OR LOWER(LAST_NAME) LIKE ?
               OR LOWER(BLOOD_TYPE) LIKE ?
               OR CONTACT_NUMBER LIKE ?
            ORDER BY DONOR_ID DESC
            """;

        List<Donor> list = new ArrayList<>();
        String value = "%" + term.toLowerCase() + "%";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, value);
            p.setString(2, value);
            p.setString(3, value);
            p.setString(4, "%" + term + "%");

            try (ResultSet r = p.executeQuery()) {
                while (r.next()) list.add(map(r));
            }
        }
        return list;
    }
}
