package bloodbank.controller;

import bloodbank.NavigationUtil;
import bloodbank.model.Donor;
import bloodbank.service.DonorService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDate;

public class DonorController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private ComboBox<String> bloodTypeBox;
    @FXML private DatePicker dobPicker;
    @FXML private ComboBox<String> genderBox;
    @FXML private TextField contactNumberField;
    @FXML private TextField emailField;
    @FXML private TextField addressField;
    @FXML private DatePicker lastDonationPicker;
    @FXML private ComboBox<String> eligibilityStatusBox;
    @FXML private TextField searchField;

    @FXML private TableView<Donor> donorTable;
    @FXML private TableColumn<Donor, Number> idColumn;
    @FXML private TableColumn<Donor, String> firstNameColumn;
    @FXML private TableColumn<Donor, String> lastNameColumn;
    @FXML private TableColumn<Donor, String> bloodTypeColumn;
    @FXML private TableColumn<Donor, LocalDate> dobColumn;
    @FXML private TableColumn<Donor, String> genderColumn;
    @FXML private TableColumn<Donor, String> contactColumn;
    @FXML private TableColumn<Donor, String> emailColumn;
    @FXML private TableColumn<Donor, String> addressColumn;
    @FXML private TableColumn<Donor, LocalDate> lastDonationColumn;
    @FXML private TableColumn<Donor, String> eligibilityColumn;

    private final DonorService donorService = new DonorService();
    private final ObservableList<Donor> donors =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        bloodTypeBox.setItems(FXCollections.observableArrayList(
                "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
        ));

        genderBox.setItems(FXCollections.observableArrayList(
                "M", "F", "O"
        ));

        eligibilityStatusBox.setItems(FXCollections.observableArrayList(
                "Eligible", "Temporary_Deferral", "Permanent_Deferral"
        ));

        idColumn.setCellValueFactory(
                new PropertyValueFactory<>("donorId"));
        firstNameColumn.setCellValueFactory(
                new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(
                new PropertyValueFactory<>("lastName"));
        bloodTypeColumn.setCellValueFactory(
                new PropertyValueFactory<>("bloodType"));
        dobColumn.setCellValueFactory(
                new PropertyValueFactory<>("dob"));
        genderColumn.setCellValueFactory(
                new PropertyValueFactory<>("gender"));
        contactColumn.setCellValueFactory(
                new PropertyValueFactory<>("contactNumber"));
        emailColumn.setCellValueFactory(
                new PropertyValueFactory<>("email"));
        addressColumn.setCellValueFactory(
                new PropertyValueFactory<>("address"));
        lastDonationColumn.setCellValueFactory(
                new PropertyValueFactory<>("lastDonationDate"));
        eligibilityColumn.setCellValueFactory(
                new PropertyValueFactory<>("eligibilityStatus"));

        donorTable.setItems(donors);

        donorTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldDonor, newDonor) -> {
                    if (newDonor != null) {
                        populateForm(newDonor);
                    }
                });

        loadDonors();
    }

    @FXML
    public void addDonor() {
        try {
            Donor donor = readForm();
            donorService.addDonor(donor);

            showAlert(Alert.AlertType.INFORMATION,
                    "Success", "Donor registered successfully.");

            clearForm();
            loadDonors();

        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, "Invalid Input", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Could not register donor.\n" + e.getMessage());
        }
    }

    @FXML
    public void updateDonor() {
        Donor selected = donorTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING,
                    "No Selection", "Select a donor from the table first.");
            return;
        }

        try {
            Donor donor = readForm();
            donor.setDonorId(selected.getDonorId());

            donorService.updateDonor(donor);

            showAlert(Alert.AlertType.INFORMATION,
                    "Success", "Donor updated successfully.");

            clearForm();
            loadDonors();

        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, "Invalid Input", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Could not update donor.\n" + e.getMessage());
        }
    }

    @FXML
    public void deleteDonor() {
        Donor selected = donorTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING,
                    "No Selection", "Select a donor to delete.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText("Delete donor?");
        confirmation.setContentText(
                selected.getFirstName() + " " + selected.getLastName());

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL)
                != ButtonType.OK) {
            return;
        }

        try {
            donorService.deleteDonor(selected.getDonorId());

            showAlert(Alert.AlertType.INFORMATION,
                    "Success", "Donor deleted successfully.");

            clearForm();
            loadDonors();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Could not delete donor.\n" + e.getMessage());
        }
    }

    @FXML
    public void loadDonors() {
        try {
            donors.setAll(donorService.getAllDonors());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Unavailable",
                    "Donor records could not be loaded.\n"
                            + "Check the database connection and schema.\n\n"
                            + e.getMessage());
        }
    }

    @FXML
    public void searchDonors() {
        String query = searchField.getText();

        try {
            if (query == null || query.isBlank()) {
                loadDonors();
            } else {
                donors.setAll(donorService.searchDonors(query));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Search Failed",
                    "Could not search donors.\n" + e.getMessage());
        }
    }

    @FXML
    public void clearForm() {
        firstNameField.clear();
        lastNameField.clear();
        bloodTypeBox.getSelectionModel().clearSelection();
        dobPicker.setValue(null);
        genderBox.getSelectionModel().clearSelection();
        contactNumberField.clear();
        emailField.clear();
        addressField.clear();
        lastDonationPicker.setValue(null);
        eligibilityStatusBox.setValue("Eligible");
        donorTable.getSelectionModel().clearSelection();
    }

    @FXML
    public void goToDashboard(ActionEvent event) {
        NavigationUtil.goToDashboard(event);
    }

    private Donor readForm() {
        Donor donor = new Donor();

        donor.setFirstName(required(firstNameField.getText(), "First name"));
        donor.setLastName(required(lastNameField.getText(), "Last name"));
        donor.setBloodType(bloodTypeBox.getValue());

        donor.setDob(dobPicker.getValue());
        donor.setGender(genderBox.getValue());

        donor.setContactNumber(
                required(contactNumberField.getText(), "Contact number"));

        donor.setEmail(emailField.getText());
        donor.setAddress(required(addressField.getText(), "Address"));
        donor.setLastDonationDate(lastDonationPicker.getValue());

        String status = eligibilityStatusBox.getValue();
        donor.setEligibilityStatus(
                status == null ? "Eligible" : status);

        return donor;
    }

    private String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " is required.");
        }
        return value.trim();
    }

    private void populateForm(Donor donor) {
        firstNameField.setText(donor.getFirstName());
        lastNameField.setText(donor.getLastName());
        bloodTypeBox.setValue(donor.getBloodType());
        dobPicker.setValue(donor.getDob());
        genderBox.setValue(donor.getGender());
        contactNumberField.setText(donor.getContactNumber());
        emailField.setText(donor.getEmail());
        addressField.setText(donor.getAddress());
        lastDonationPicker.setValue(donor.getLastDonationDate());
        eligibilityStatusBox.setValue(donor.getEligibilityStatus());
    }

    private void showAlert(Alert.AlertType type,
                           String title,
                           String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
