package bloodbank.controller;

import bloodbank.NavigationUtil;
import bloodbank.model.Hospital;
import bloodbank.service.HospitalService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;

public class HospitalController {

    @FXML private TextField hospitalNameField;
    @FXML private TextField licenseNumberField;
    @FXML private TextField addressField;
    @FXML private TextField cityField;
    @FXML private TextField contactPersonField;
    @FXML private TextField phoneNumberField;
    @FXML private TextField emailField;
    @FXML private TextField searchField;

    @FXML private TableView<Hospital> hospitalTable;
    @FXML private TableColumn<Hospital, Number> idColumn;
    @FXML private TableColumn<Hospital, String> nameColumn;
    @FXML private TableColumn<Hospital, String> licenseColumn;
    @FXML private TableColumn<Hospital, String> addressColumn;
    @FXML private TableColumn<Hospital, String> cityColumn;
    @FXML private TableColumn<Hospital, String> contactColumn;
    @FXML private TableColumn<Hospital, String> phoneColumn;
    @FXML private TableColumn<Hospital, String> emailColumn;

    private final HospitalService hospitalService = new HospitalService();
    private final ObservableList<Hospital> hospitals =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(
                new PropertyValueFactory<>("hospitalId"));
        nameColumn.setCellValueFactory(
                new PropertyValueFactory<>("hospitalName"));
        licenseColumn.setCellValueFactory(
                new PropertyValueFactory<>("licenseNumber"));
        addressColumn.setCellValueFactory(
                new PropertyValueFactory<>("address"));
        cityColumn.setCellValueFactory(
                new PropertyValueFactory<>("city"));
        contactColumn.setCellValueFactory(
                new PropertyValueFactory<>("contactPerson"));
        phoneColumn.setCellValueFactory(
                new PropertyValueFactory<>("phoneNumber"));
        emailColumn.setCellValueFactory(
                new PropertyValueFactory<>("email"));

        hospitalTable.setItems(hospitals);

        hospitalTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldHospital, newHospital) -> {
                    if (newHospital != null) {
                        populateForm(newHospital);
                    }
                });

        loadHospitals();
    }

    @FXML
    public void addHospital() {
        try {
            Hospital hospital = readForm();
            hospitalService.addHospital(hospital);

            showMessage(Alert.AlertType.INFORMATION,
                    "Success", "Hospital added successfully.");

            clearForm();
            loadHospitals();

        } catch (IllegalArgumentException e) {
            showMessage(Alert.AlertType.WARNING,
                    "Invalid Input", e.getMessage());
        } catch (SQLException e) {
            showMessage(Alert.AlertType.ERROR,
                    "Database Error",
                    "Could not add hospital.\n" + e.getMessage());
        }
    }

    @FXML
    public void updateHospital() {
        Hospital selected =
                hospitalTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showMessage(Alert.AlertType.WARNING,
                    "No Selection", "Select a hospital to update.");
            return;
        }

        try {
            Hospital hospital = readForm();
            hospital.setHospitalId(selected.getHospitalId());

            hospitalService.updateHospital(hospital);

            showMessage(Alert.AlertType.INFORMATION,
                    "Success", "Hospital updated successfully.");

            clearForm();
            loadHospitals();

        } catch (IllegalArgumentException e) {
            showMessage(Alert.AlertType.WARNING,
                    "Invalid Input", e.getMessage());
        } catch (SQLException e) {
            showMessage(Alert.AlertType.ERROR,
                    "Database Error",
                    "Could not update hospital.\n" + e.getMessage());
        }
    }

    @FXML
    public void deleteHospital() {
        Hospital selected =
                hospitalTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showMessage(Alert.AlertType.WARNING,
                    "No Selection", "Select a hospital to delete.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText("Delete this hospital?");
        confirmation.setContentText(selected.getHospitalName());

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL)
                != ButtonType.OK) {
            return;
        }

        try {
            hospitalService.deleteHospital(selected.getHospitalId());

            showMessage(Alert.AlertType.INFORMATION,
                    "Success", "Hospital deleted successfully.");

            clearForm();
            loadHospitals();

        } catch (SQLException e) {
            showMessage(Alert.AlertType.ERROR,
                    "Database Error",
                    "Could not delete hospital.\n" + e.getMessage());
        }
    }

    @FXML
    public void loadHospitals() {
        try {
            hospitals.setAll(hospitalService.getAllHospitals());
        } catch (SQLException e) {
            showMessage(Alert.AlertType.ERROR,
                    "Database Unavailable",
                    "Could not load hospitals.\n" + e.getMessage());
        }
    }

    @FXML
    public void searchHospitals() {
        String query = searchField.getText();

        try {
            if (query == null || query.isBlank()) {
                loadHospitals();
            } else {
                hospitals.setAll(
                        hospitalService.searchHospitals(query.trim()));
            }
        } catch (SQLException e) {
            showMessage(Alert.AlertType.ERROR,
                    "Search Failed",
                    "Could not search hospitals.\n" + e.getMessage());
        }
    }

    @FXML
    public void clearForm() {
        hospitalNameField.clear();
        licenseNumberField.clear();
        addressField.clear();
        cityField.clear();
        contactPersonField.clear();
        phoneNumberField.clear();
        emailField.clear();
        hospitalTable.getSelectionModel().clearSelection();
    }

    @FXML
    public void goToDashboard(ActionEvent event) {
        NavigationUtil.goToDashboard(event);
    }

    private Hospital readForm() {
        return new Hospital(
                required(hospitalNameField.getText(), "Hospital name"),
                required(licenseNumberField.getText(), "License number"),
                required(addressField.getText(), "Address"),
                required(cityField.getText(), "City"),
                required(contactPersonField.getText(), "Contact person"),
                required(phoneNumberField.getText(), "Phone number"),
                optional(emailField.getText())
        );
    }

    private String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required.");
        }
        return value.trim();
    }

    private String optional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void populateForm(Hospital hospital) {
        hospitalNameField.setText(hospital.getHospitalName());
        licenseNumberField.setText(hospital.getLicenseNumber());
        addressField.setText(hospital.getAddress());
        cityField.setText(hospital.getCity());
        contactPersonField.setText(hospital.getContactPerson());
        phoneNumberField.setText(hospital.getPhoneNumber());
        emailField.setText(hospital.getEmail());
    }

    private void showMessage(
            Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
