package bloodbank.dao;

import bloodbank.DBConnection;
import bloodbank.model.BloodUnit;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

public class BloodUnitDAO {

    public List<BloodUnit> getAllUnits()
            throws SQLException {

        List<BloodUnit> units =
                new ArrayList<>();

        String sql =
                "SELECT Unit_ID, Donation_ID, Blood_Type, " +
                "Component_Type, Volume_ML, Collection_Date, " +
                "Expiry_Date, Status " +
                "FROM BLOOD_INVENTORY " +
                "ORDER BY Unit_ID";

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            while (resultSet.next()) {

                units.add(
                        mapResultSetToBloodUnit(
                                resultSet
                        )
                );
            }
        }

        return units;
    }

    public java.util.Optional<BloodUnit> getUnitById(int unitId) throws SQLException {
        String sql =
                "SELECT Unit_ID, Donation_ID, Blood_Type, " +
                "Component_Type, Volume_ML, Collection_Date, " +
                "Expiry_Date, Status " +
                "FROM BLOOD_INVENTORY " +
                "WHERE Unit_ID = ?";

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, unitId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return java.util.Optional.of(mapResultSetToBloodUnit(resultSet));
                }
            }
        }
        return java.util.Optional.empty();
    }



    public void addUnit(BloodUnit unit)
            throws SQLException {

        String sql =
                "INSERT INTO BLOOD_INVENTORY " +
                "(Donation_ID, Blood_Type, Component_Type, " +
                "Volume_ML, Collection_Date, Expiry_Date, Status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    unit.getDonationId()
            );

            statement.setString(
                    2,
                    unit.getBloodType()
            );

            statement.setString(
                    3,
                    unit.getComponentType()
            );

            statement.setInt(
                    4,
                    unit.getVolumeMl()
            );

            statement.setDate(
                    5,
                    Date.valueOf(
                            unit.getCollectionDate()
                    )
            );

            statement.setDate(
                    6,
                    Date.valueOf(
                            unit.getExpiryDate()
                    )
            );

            statement.setString(
                    7,
                    unit.getStatus()
            );

            statement.executeUpdate();
        }
    }


    public void updateUnit(BloodUnit unit)
            throws SQLException {

        String sql =
                "UPDATE BLOOD_INVENTORY SET " +
                "Donation_ID = ?, " +
                "Blood_Type = ?, " +
                "Component_Type = ?, " +
                "Volume_ML = ?, " +
                "Collection_Date = ?, " +
                "Expiry_Date = ?, " +
                "Status = ? " +
                "WHERE Unit_ID = ?";

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    unit.getDonationId()
            );

            statement.setString(
                    2,
                    unit.getBloodType()
            );

            statement.setString(
                    3,
                    unit.getComponentType()
            );

            statement.setInt(
                    4,
                    unit.getVolumeMl()
            );

            statement.setDate(
                    5,
                    Date.valueOf(
                            unit.getCollectionDate()
                    )
            );

            statement.setDate(
                    6,
                    Date.valueOf(
                            unit.getExpiryDate()
                    )
            );

            statement.setString(
                    7,
                    unit.getStatus()
            );

            statement.setInt(
                    8,
                    unit.getUnitId()
            );

            statement.executeUpdate();
        }
    }


    public void deleteUnit(int unitId)
            throws SQLException {

        String sql =
                "DELETE FROM BLOOD_INVENTORY " +
                "WHERE Unit_ID = ?";

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    unitId
            );

            statement.executeUpdate();
        }
    }


    public List<BloodUnit> searchUnits(
            String searchText
    ) throws SQLException {

        List<BloodUnit> units =
                new ArrayList<>();

        String sql =
                "SELECT Unit_ID, Donation_ID, Blood_Type, " +
                "Component_Type, Volume_ML, Collection_Date, " +
                "Expiry_Date, Status " +
                "FROM BLOOD_INVENTORY " +
                "WHERE LOWER(Blood_Type) LIKE ? " +
                "OR LOWER(Component_Type) LIKE ? " +
                "OR LOWER(Status) LIKE ? " +
                "OR TO_CHAR(Unit_ID) LIKE ? " +
                "OR TO_CHAR(Donation_ID) LIKE ? " +
                "ORDER BY Unit_ID";

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

            statement.setString(
                    1,
                    pattern
            );

            statement.setString(
                    2,
                    pattern
            );

            statement.setString(
                    3,
                    pattern
            );

            statement.setString(
                    4,
                    pattern
            );

            statement.setString(
                    5,
                    pattern
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                while (resultSet.next()) {

                    units.add(
                            mapResultSetToBloodUnit(
                                    resultSet
                            )
                    );
                }
            }
        }

        return units;
    }


    private BloodUnit mapResultSetToBloodUnit(
            ResultSet resultSet
    ) throws SQLException {

        Date collectionDate =
                resultSet.getDate(
                        "Collection_Date"
                );

        Date expiryDate =
                resultSet.getDate(
                        "Expiry_Date"
                );

        return new BloodUnit(

                resultSet.getInt(
                        "Unit_ID"
                ),

                resultSet.getInt(
                        "Donation_ID"
                ),

                resultSet.getString(
                        "Blood_Type"
                ),

                resultSet.getString(
                        "Component_Type"
                ),

                resultSet.getInt(
                        "Volume_ML"
                ),

                collectionDate != null
                        ? collectionDate.toLocalDate()
                        : null,

                expiryDate != null
                        ? expiryDate.toLocalDate()
                        : null,

                resultSet.getString(
                        "Status"
                )
        );
    }
}