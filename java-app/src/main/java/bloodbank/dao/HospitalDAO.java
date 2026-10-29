package bloodbank.dao;

import bloodbank.DBConnection;
import bloodbank.model.Hospital;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

public class HospitalDAO {

    public List<Hospital> getAllHospitals()
            throws SQLException {

        List<Hospital> hospitals =
                new ArrayList<>();

        String sql =
                "SELECT Hospital_ID, Hospital_Name, " +
                "License_Number, Address, City, " +
                "Contact_Person, Phone_Number, Email " +
                "FROM HOSPITAL " +
                "ORDER BY Hospital_ID";

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            while (resultSet.next()) {

                hospitals.add(
                        mapResultSetToHospital(
                                resultSet
                        )
                );
            }
        }

        return hospitals;
    }

    public java.util.Optional<Hospital> getHospitalById(int hospitalId) throws SQLException {
        String sql =
                "SELECT Hospital_ID, Hospital_Name, " +
                "License_Number, Address, City, " +
                "Contact_Person, Phone_Number, Email " +
                "FROM HOSPITAL " +
                "WHERE Hospital_ID = ?";

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, hospitalId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return java.util.Optional.of(mapResultSetToHospital(resultSet));
                }
            }
        }
        return java.util.Optional.empty();
    }



    public void addHospital(Hospital hospital)
            throws SQLException {

        String sql =
                "INSERT INTO HOSPITAL " +
                "(Hospital_Name, License_Number, Address, " +
                "City, Contact_Person, Phone_Number, Email) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    hospital.getHospitalName()
            );

            statement.setString(
                    2,
                    hospital.getLicenseNumber()
            );

            statement.setString(
                    3,
                    hospital.getAddress()
            );

            statement.setString(
                    4,
                    hospital.getCity()
            );

            statement.setString(
                    5,
                    hospital.getContactPerson()
            );

            statement.setString(
                    6,
                    hospital.getPhoneNumber()
            );

            statement.setString(
                    7,
                    hospital.getEmail()
            );

            statement.executeUpdate();
        }
    }


    public void updateHospital(Hospital hospital)
            throws SQLException {

        String sql =
                "UPDATE HOSPITAL SET " +
                "Hospital_Name = ?, " +
                "License_Number = ?, " +
                "Address = ?, " +
                "City = ?, " +
                "Contact_Person = ?, " +
                "Phone_Number = ?, " +
                "Email = ? " +
                "WHERE Hospital_ID = ?";

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    hospital.getHospitalName()
            );

            statement.setString(
                    2,
                    hospital.getLicenseNumber()
            );

            statement.setString(
                    3,
                    hospital.getAddress()
            );

            statement.setString(
                    4,
                    hospital.getCity()
            );

            statement.setString(
                    5,
                    hospital.getContactPerson()
            );

            statement.setString(
                    6,
                    hospital.getPhoneNumber()
            );

            statement.setString(
                    7,
                    hospital.getEmail()
            );

            statement.setInt(
                    8,
                    hospital.getHospitalId()
            );

            statement.executeUpdate();
        }
    }


    public void deleteHospital(int hospitalId)
            throws SQLException {

        String sql =
                "DELETE FROM HOSPITAL " +
                "WHERE Hospital_ID = ?";

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    hospitalId
            );

            statement.executeUpdate();
        }
    }


    public List<Hospital> searchHospitals(
            String searchText
    ) throws SQLException {

        List<Hospital> hospitals =
                new ArrayList<>();

        String sql =
                "SELECT Hospital_ID, Hospital_Name, " +
                "License_Number, Address, City, " +
                "Contact_Person, Phone_Number, Email " +
                "FROM HOSPITAL " +
                "WHERE LOWER(Hospital_Name) LIKE ? " +
                "OR LOWER(License_Number) LIKE ? " +
                "OR LOWER(Address) LIKE ? " +
                "OR LOWER(City) LIKE ? " +
                "OR LOWER(Contact_Person) LIKE ? " +
                "OR LOWER(Phone_Number) LIKE ? " +
                "OR LOWER(Email) LIKE ? " +
                "ORDER BY Hospital_ID";

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            String pattern =
                    "%" +
                    searchText.toLowerCase() +
                    "%";

            for (int i = 1; i <= 7; i++) {

                statement.setString(
                        i,
                        pattern
                );
            }

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                while (resultSet.next()) {

                    hospitals.add(
                            mapResultSetToHospital(
                                    resultSet
                            )
                    );
                }
            }
        }

        return hospitals;
    }


    private Hospital mapResultSetToHospital(
            ResultSet resultSet
    ) throws SQLException {

        return new Hospital(

                resultSet.getInt(
                        "Hospital_ID"
                ),

                resultSet.getString(
                        "Hospital_Name"
                ),

                resultSet.getString(
                        "License_Number"
                ),

                resultSet.getString(
                        "Address"
                ),

                resultSet.getString(
                        "City"
                ),

                resultSet.getString(
                        "Contact_Person"
                ),

                resultSet.getString(
                        "Phone_Number"
                ),

                resultSet.getString(
                        "Email"
                )
        );
    }
}