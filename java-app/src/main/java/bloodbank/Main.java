package bloodbank;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/bloodbank/dashboard.fxml")
        );

        Parent root = loader.load();

        Scene scene = new Scene(root, 1200, 750);

        stage.setTitle("Blood Bank Management System");
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}