package bloodbank.controller;

import bloodbank.NavigationUtil;
import bloodbank.model.Donation;
import bloodbank.service.DonationService;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.time.LocalDate;

public class DonationController {

    @FXML
    private TextField donorIdField;

    @FXML
    private DatePicker donationDatePicker;

    @FXML
    private TextField hemoglobinField;

    @FXML
    private TextField bloodPressureField;

    @FXML
    private TextField pulseRateField;

    @FXML
    private ComboBox<String> screeningResultBox;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<Donation> donationTable;

    @FXML
    private TableColumn<Donation, Number> idColumn;

    @FXML
    private TableColumn<Donation, Number> donorIdColumn;

    @FXML
    private TableColumn<Donation, LocalDate> donationDateColumn;

    @FXML
    private TableColumn<Donation, Number> hemoglobinColumn;

    @FXML
    private TableColumn<Donation, String> bloodPressureColumn;

    @FXML
    private TableColumn<Donation, Number> pulseRateColumn;

    @FXML
    private TableColumn<Donation, String> screeningColumn;


    private final DonationService donationService =
            new DonationService();


    private final ObservableList<Donation> donations =
            FXCollections.observableArrayList();


    @FXML
    public void initialize() {

        screeningResultBox.setItems(
                FXCollections.observableArrayList(
                        "Passed",
                        "Failed"
                )
        );

        screeningResultBox.setValue("Passed");

        setupTable();

        loadDonations();

        donationTable.setOnMouseClicked(event -> {

            Donation selectedDonation =
                    donationTable
                            .getSelectionModel()
                            .getSelectedItem();

            if (selectedDonation != null) {

                populateForm(
                        selectedDonation
                );
            }
        });
    }


    private void setupTable() {

        idColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(
                        data.getValue()
                                .getDonationId()
                )
        );

        donorIdColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(
                        data.getValue()
                                .getDonorId()
                )
        );

        donationDateColumn.setCellValueFactory(data ->
                new SimpleObjectProperty<>(
                        data.getValue()
                                .getDonationDate()
                )
        );

        hemoglobinColumn.setCellValueFactory(data ->
                new SimpleDoubleProperty(
                        data.getValue()
                                .getHemoglobinLevel()
                )
        );

        bloodPressureColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue()
                                .getBloodPressure()
                )
        );

        pulseRateColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(
                        data.getValue()
                                .getPulseRate()
                )
        );

        screeningColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue()
                                .getMedicalScreeningResult()
                )
        );

        donationTable.setItems(donations);
    }


    @FXML
    private void addDonation(ActionEvent event) {

        if (!validateForm()) {
            return;
        }

        Donation donation =
                new Donation(

                        Integer.parseInt(
                                donorIdField
                                        .getText()
                                        .trim()
                        ),

                        donationDatePicker.getValue(),

                        Double.parseDouble(
                                hemoglobinField
                                        .getText()
                                        .trim()
                        ),

                        bloodPressureField
                                .getText()
                                .trim(),

                        Integer.parseInt(
                                pulseRateField
                                        .getText()
                                        .trim()
                        ),

                        screeningResultBox.getValue()
                );


        try {

            donationService.addDonation(
                    donation
            );

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Success",
                    "Donation record added successfully."
            );

            clearForm(null);

            loadDonations();

        } catch (SQLException e) {

            handleDatabaseError(e);
        }
    }


    @FXML
    private void updateDonation(ActionEvent event) {

        Donation selectedDonation =
                donationTable
                        .getSelectionModel()
                        .getSelectedItem();


        if (selectedDonation == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "No Selection",
                    "Please select a donation record first."
            );

            return;
        }


        if (!validateForm()) {
            return;
        }


        selectedDonation.setDonorId(
                Integer.parseInt(
                        donorIdField
                                .getText()
                                .trim()
                )
        );

        selectedDonation.setDonationDate(
                donationDatePicker.getValue()
        );

        selectedDonation.setHemoglobinLevel(
                Double.parseDouble(
                        hemoglobinField
                                .getText()
                                .trim()
                )
        );

        selectedDonation.setBloodPressure(
                bloodPressureField
                        .getText()
                        .trim()
        );

        selectedDonation.setPulseRate(
                Integer.parseInt(
                        pulseRateField
                                .getText()
                                .trim()
                )
        );

        selectedDonation.setMedicalScreeningResult(
                screeningResultBox.getValue()
        );


        try {

            donationService.updateDonation(
                    selectedDonation
            );

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Updated",
                    "Donation record updated successfully."
            );

            clearForm(null);

            loadDonations();

        } catch (SQLException e) {

            handleDatabaseError(e);
        }
    }


    @FXML
    private void deleteDonation(ActionEvent event) {

        Donation selectedDonation =
                donationTable
                        .getSelectionModel()
                        .getSelectedItem();


        if (selectedDonation == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "No Selection",
                    "Please select a donation record to delete."
            );

            return;
        }


        try {

            donationService.deleteDonation(
                    selectedDonation.getDonationId()
            );

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Deleted",
                    "Donation record deleted successfully."
            );

            clearForm(null);

            loadDonations();

        } catch (SQLException e) {

            handleDatabaseError(e);
        }
    }


    @FXML
    private void clearForm(ActionEvent event) {

        donorIdField.clear();

        donationDatePicker.setValue(null);

        hemoglobinField.clear();

        bloodPressureField.clear();

        pulseRateField.clear();

        screeningResultBox.setValue("Passed");

        donationTable
                .getSelectionModel()
                .clearSelection();
    }


    @FXML
    private void searchDonations(ActionEvent event) {

        String searchText =
                searchField
                        .getText()
                        .trim();


        if (searchText.isEmpty()) {

            loadDonations();

            return;
        }


        try {

            donations.setAll(
                    donationService.searchDonations(
                            searchText
                    )
            );

        } catch (SQLException e) {

            handleDatabaseError(e);
        }
    }


    @FXML
    private void loadDonations() {

        try {

            donations.setAll(
                    donationService
                            .getAllDonations()
            );

            donationTable.setItems(
                    donations
            );

            searchField.clear();

        } catch (SQLException e) {

            handleDatabaseError(e);
        }
    }


    private void populateForm(
            Donation donation
    ) {

        donorIdField.setText(
                String.valueOf(
                        donation.getDonorId()
                )
        );

        donationDatePicker.setValue(
                donation.getDonationDate()
        );

        hemoglobinField.setText(
                String.valueOf(
                        donation.getHemoglobinLevel()
                )
        );

        bloodPressureField.setText(
                donation.getBloodPressure()
        );

        pulseRateField.setText(
                String.valueOf(
                        donation.getPulseRate()
                )
        );

        screeningResultBox.setValue(
                donation.getMedicalScreeningResult()
        );
    }


    private boolean validateForm() {

        if (donorIdField
                .getText()
                .trim()
                .isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation Error",
                    "Please enter the donor ID."
            );

            return false;
        }


        int donorId;

        try {

            donorId =
                    Integer.parseInt(
                            donorIdField
                                    .getText()
                                    .trim()
                    );

        } catch (NumberFormatException e) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation Error",
                    "Donor ID must be a valid integer."
            );

            return false;
        }


        if (donorId <= 0) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation Error",
                    "Donor ID must be greater than zero."
            );

            return false;
        }


        if (donationDatePicker.getValue() == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation Error",
                    "Please select the donation date."
            );

            return false;
        }


        if (donationDatePicker
                .getValue()
                .isAfter(LocalDate.now())) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation Error",
                    "Donation date cannot be in the future."
            );

            return false;
        }


        if (hemoglobinField
                .getText()
                .trim()
                .isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation Error",
                    "Please enter the hemoglobin level."
            );

            return false;
        }


        double hemoglobin;

        try {

            hemoglobin =
                    Double.parseDouble(
                            hemoglobinField
                                    .getText()
                                    .trim()
                    );

        } catch (NumberFormatException e) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation Error",
                    "Hemoglobin level must be a valid number."
            );

            return false;
        }


        if (hemoglobin < 12.5) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation Error",
                    "Hemoglobin level must be at least 12.5."
            );

            return false;
        }


        if (bloodPressureField
                .getText()
                .trim()
                .isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation Error",
                    "Please enter the blood pressure."
            );

            return false;
        }


        if (pulseRateField
                .getText()
                .trim()
                .isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation Error",
                    "Please enter the pulse rate."
            );

            return false;
        }


        int pulseRate;

        try {

            pulseRate =
                    Integer.parseInt(
                            pulseRateField
                                    .getText()
                                    .trim()
                    );

        } catch (NumberFormatException e) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation Error",
                    "Pulse rate must be a valid integer."
            );

            return false;
        }


        if (pulseRate <= 0) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation Error",
                    "Pulse rate must be greater than zero."
            );

            return false;
        }


        if (screeningResultBox.getValue() == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation Error",
                    "Please select the medical screening result."
            );

            return false;
        }


        return true;
    }


    private void handleDatabaseError(
            SQLException e
    ) {

        e.printStackTrace();

        String message =
                e.getMessage();

        if (message == null) {
            message = "Unknown database error.";
        }


        showAlert(
                Alert.AlertType.ERROR,
                "Database Error",
                message
        );
    }


    @FXML
    private void goToDashboard(
            ActionEvent event
    ) {

        NavigationUtil.goToDashboard(event);
    }


    private void showAlert(
            Alert.AlertType type,
            String title,
            String message
    ) {

        Alert alert =
                new Alert(type);

        alert.setTitle(title);

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }
}