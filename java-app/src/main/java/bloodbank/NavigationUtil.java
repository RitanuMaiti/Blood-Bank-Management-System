package bloodbank;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NavigationUtil {

    public static void goToDashboard(ActionEvent event) {
        navigate(
                event,
                "/bloodbank/dashboard.fxml",
                "Blood Bank Management System"
        );
    }

    public static void goToDonors(ActionEvent event) {
        navigate(
                event,
                "/bloodbank/donors.fxml",
                "Donor Management - Blood Bank System"
        );
    }

    public static void goToInventory(ActionEvent event) {
        navigate(
                event,
                "/bloodbank/inventory.fxml",
                "Blood Inventory - Blood Bank System"
        );
    }

    public static void goToHospitals(ActionEvent event) {
        navigate(
                event,
                "/bloodbank/hospitals.fxml",
                "Hospital Management - Blood Bank System"
        );
    }

    public static void goToRequests(ActionEvent event) {
        navigate(
                event,
                "/bloodbank/requests.fxml",
                "Blood Requests - Blood Bank System"
        );
    }
    public static void goToDonations(ActionEvent event) {
    navigate(event, "/bloodbank/donations.fxml", "Donation Records");
}

    private static void navigate(
            ActionEvent event,
            String fxmlPath,
            String title
    ) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            NavigationUtil.class.getResource(fxmlPath)
                    );

            Parent root = loader.load();

            Stage stage =
                    (Stage) ((Node) event.getSource())
                            .getScene()
                            .getWindow();

            Scene scene =
                    new Scene(root, 1200, 750);

            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}