package bloodbank.controller;

import bloodbank.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DashboardController {

    @FXML private Label totalDonorsLabel;
    @FXML private Label availableUnitsLabel;
    @FXML private Label pendingRequestsLabel;

    @FXML
    public void initialize() {
        loadDashboardStats();
    }

    private void loadDashboardStats() {
        try (Connection c = DBConnection.getConnection()) {
            // Count total registered donors
            try (PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM DONOR");
                 ResultSet r = p.executeQuery()) {
                if (r.next() && totalDonorsLabel != null) {
                    totalDonorsLabel.setText(String.valueOf(r.getInt(1)));
                }
            }

            // Count available non-expired blood units
            try (PreparedStatement p = c.prepareStatement(
                    "SELECT COUNT(*) FROM BLOOD_INVENTORY WHERE Status = 'Available' AND Expiry_Date >= CURRENT_DATE");
                 ResultSet r = p.executeQuery()) {
                if (r.next() && availableUnitsLabel != null) {
                    availableUnitsLabel.setText(String.valueOf(r.getInt(1)));
                }
            }

            // Count active requests
            try (PreparedStatement p = c.prepareStatement(
                    "SELECT COUNT(*) FROM EMERGENCY_REQUEST WHERE Request_Status IN ('Pending', 'Partially_Fulfilled')");
                 ResultSet r = p.executeQuery()) {
                if (r.next() && pendingRequestsLabel != null) {
                    pendingRequestsLabel.setText(String.valueOf(r.getInt(1)));
                }
            }
        } catch (Exception ignored) {
            // Graceful fallback when database is empty or connection is not yet configured
        }
    }

    private void navigate(ActionEvent event, String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/bloodbank/" + fxmlFile)
            );

            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            Scene scene = new Scene(root, 1200, 750);

            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            System.err.println("Failed to load screen: " + fxmlFile);
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Could not open " + fxmlFile);
            alert.setContentText(
                    e.getClass().getSimpleName() + ": " + e.getMessage()
            );
            alert.showAndWait();
        }
    }

    public void showDashboard(ActionEvent event) {
        loadDashboardStats();
    }

    public void showDonors(ActionEvent event) {
        navigate(
                event,
                "donors.fxml",
                "Donor Management - Blood Bank System"
        );
    }

    public void showDonations(ActionEvent event) {
        navigate(
                event,
                "donations.fxml",
                "Donation Records - Blood Bank System"
        );
    }

    public void showInventory(ActionEvent event) {
        navigate(
                event,
                "inventory.fxml",
                "Blood Inventory - Blood Bank System"
        );
    }

    public void showHospitals(ActionEvent event) {
        navigate(
                event,
                "hospitals.fxml",
                "Hospital Management - Blood Bank System"
        );
    }

    public void showRequests(ActionEvent event) {
        navigate(
                event,
                "requests.fxml",
                "Blood Requests - Blood Bank System"
        );
    }

    public void showEmergencyMatching(ActionEvent event) {
        navigate(
                event,
                "requests.fxml",
                "Emergency Matching & Allocation - Blood Bank System"
        );
    }
}