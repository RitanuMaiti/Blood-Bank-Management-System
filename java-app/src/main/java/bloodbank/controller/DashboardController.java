package bloodbank.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DashboardController {

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

    javafx.scene.control.Alert alert =
            new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR
            );

    alert.setTitle("Navigation Error");
    alert.setHeaderText("Could not open " + fxmlFile);
    alert.setContentText(
            e.getClass().getSimpleName() + ": " + e.getMessage()
    );
    alert.showAndWait();
}
    }

    public void showDashboard(ActionEvent event) {
        // Already on dashboard
    }

    public void showDonors(ActionEvent event) {
        navigate(
                event,
                "donors.fxml",
                "Donor Management - Blood Bank System"
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
                "Emergency Donor Matching - Blood Bank System"
        );
    }
}