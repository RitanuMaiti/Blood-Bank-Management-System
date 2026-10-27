package bloodbank.controller;

import bloodbank.NavigationUtil;
import bloodbank.model.BloodUnit;
import bloodbank.service.InventoryService;

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

public class InventoryController {

    @FXML
    private TextField donationIdField;

    @FXML
    private ComboBox<String> bloodTypeBox;

    @FXML
    private ComboBox<String> componentTypeBox;

    @FXML
    private TextField volumeField;

    @FXML
    private DatePicker collectionDatePicker;

    @FXML
    private DatePicker expiryDatePicker;

    @FXML
    private ComboBox<String> statusBox;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<BloodUnit> inventoryTable;

    @FXML
    private TableColumn<BloodUnit, Number> unitIdColumn;

    @FXML
    private TableColumn<BloodUnit, Number> donationIdColumn;

    @FXML
    private TableColumn<BloodUnit, String> bloodTypeColumn;

    @FXML
    private TableColumn<BloodUnit, String> componentColumn;

    @FXML
    private TableColumn<BloodUnit, Number> volumeColumn;

    @FXML
    private TableColumn<BloodUnit, LocalDate> collectionDateColumn;

    @FXML
    private TableColumn<BloodUnit, LocalDate> expiryDateColumn;

    @FXML
    private TableColumn<BloodUnit, String> statusColumn;

    private final InventoryService inventoryService =
            new InventoryService();

    private final ObservableList<BloodUnit> units =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        setupComboBoxes();

        setupTable();

        loadUnits();

        inventoryTable.setOnMouseClicked(event -> {

            BloodUnit selectedUnit =
                    inventoryTable
                            .getSelectionModel()
                            .getSelectedItem();

            if (selectedUnit != null) {
                populateForm(selectedUnit);
            }
        });
    }

    private void setupComboBoxes() {

        bloodTypeBox.setItems(
                FXCollections.observableArrayList(
                        "A+",
                        "A-",
                        "B+",
                        "B-",
                        "AB+",
                        "AB-",
                        "O+",
                        "O-"
                )
        );

        componentTypeBox.setItems(
                FXCollections.observableArrayList(
                        "Whole Blood",
                        "Red Blood Cells",
                        "Platelets",
                        "Fresh Frozen Plasma"
                )
        );

        statusBox.setItems(
                FXCollections.observableArrayList(
                        "Available",
                        "Reserved",
                        "Dispatched",
                        "Discarded"
                )
        );

        statusBox.setValue("Available");
    }

    private void setupTable() {

        unitIdColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(
                        data.getValue().getUnitId()
                )
        );

        donationIdColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(
                        data.getValue().getDonationId()
                )
        );

        bloodTypeColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getBloodType()
                )
        );

        componentColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getComponentType()
                )
        );

        volumeColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(
                        data.getValue().getVolumeMl()
                )
        );

        collectionDateColumn.setCellValueFactory(data ->
                new SimpleObjectProperty<>(
                        data.getValue().getCollectionDate()
                )
        );

        expiryDateColumn.setCellValueFactory(data ->
                new SimpleObjectProperty<>(
                        data.getValue().getExpiryDate()
                )
        );

        statusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getStatus()
                )
        );

        inventoryTable.setItems(units);
    }

    @FXML
    private void addUnit(ActionEvent event) {

        if (!validateForm()) {
            return;
        }

        BloodUnit unit =
                new BloodUnit(
                        Integer.parseInt(
                                donationIdField
                                        .getText()
                                        .trim()
                        ),
                        bloodTypeBox.getValue(),
                        componentTypeBox.getValue(),
                        Integer.parseInt(
                                volumeField
                                        .getText()
                                        .trim()
                        ),
                        collectionDatePicker.getValue(),
                        expiryDatePicker.getValue(),
                        statusBox.getValue()
                );

        try {

            inventoryService.addUnit(unit);

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Success",
                    "Blood unit added successfully."
            );

            clearForm(null);

            loadUnits();

        } catch (SQLException e) {

            handleDatabaseError(e);
        }
    }

    @FXML
    private void updateUnit(ActionEvent event) {

        BloodUnit selectedUnit =
                inventoryTable
                        .getSelectionModel()
                        .getSelectedItem();

        if (selectedUnit == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "No Selection",
                    "Please select a blood unit from the table first."
            );

            return;
        }

        if (!validateForm()) {
            return;
        }

        selectedUnit.setDonationId(
                Integer.parseInt(
                        donationIdField
                                .getText()
                                .trim()
                )
        );

        selectedUnit.setBloodType(
                bloodTypeBox.getValue()
        );

        selectedUnit.setComponentType(
                componentTypeBox.getValue()
        );

        selectedUnit.setVolumeMl(
                Integer.parseInt(
                        volumeField
                                .getText()
                                .trim()
                )
        );

        selectedUnit.setCollectionDate(
                collectionDatePicker.getValue()
        );

        selectedUnit.setExpiryDate(
                expiryDatePicker.getValue()
        );

        selectedUnit.setStatus(
                statusBox.getValue()
        );

        try {

            inventoryService.updateUnit(
                    selectedUnit
            );

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Updated",
                    "Blood unit updated successfully."
            );

            clearForm(null);

            loadUnits();

        } catch (SQLException e) {

            handleDatabaseError(e);
        }
    }

    @FXML
    private void deleteUnit(ActionEvent event) {

        BloodUnit selectedUnit =
                inventoryTable
                        .getSelectionModel()
                        .getSelectedItem();

        if (selectedUnit == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "No Selection",
                    "Please select a blood unit to delete."
            );

            return;
        }

        try {

            inventoryService.deleteUnit(
                    selectedUnit.getUnitId()
            );

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Deleted",
                    "Blood unit deleted successfully."
            );

            clearForm(null);

            loadUnits();

        } catch (SQLException e) {

            handleDatabaseError(e);
        }
    }

    @FXML
    private void clearForm(ActionEvent event) {

        donationIdField.clear();

        bloodTypeBox.setValue(null);

        componentTypeBox.setValue(null);

        volumeField.clear();

        collectionDatePicker.setValue(null);

        expiryDatePicker.setValue(null);

        statusBox.setValue("Available");

        inventoryTable
                .getSelectionModel()
                .clearSelection();
    }

    @FXML
    private void searchUnits(ActionEvent event) {

        String searchText =
                searchField
                        .getText()
                        .trim();

        if (searchText.isEmpty()) {

            loadUnits();

            return;
        }

        try {

            units.setAll(
                    inventoryService.searchUnits(
                            searchText
                    )
            );

        } catch (SQLException e) {

            handleDatabaseError(e);
        }
    }

    @FXML
    private void loadUnits() {

        try {

            units.setAll(
                    inventoryService.getAllUnits()
            );

            inventoryTable.setItems(units);

            searchField.clear();

        } catch (SQLException e) {

            handleDatabaseError(e);
        }
    }

    private void populateForm(BloodUnit unit) {

        donationIdField.setText(
                String.valueOf(
                        unit.getDonationId()
                )
        );

        bloodTypeBox.setValue(
                unit.getBloodType()
        );

        componentTypeBox.setValue(
                unit.getComponentType()
        );

        volumeField.setText(
                String.valueOf(
                        unit.getVolumeMl()
                )
        );

        collectionDatePicker.setValue(
                unit.getCollectionDate()
        );

        expiryDatePicker.setValue(
                unit.getExpiryDate()
        );

        statusBox.setValue(
                unit.getStatus()
        );
    }

    private boolean validateForm() {

        if (donationIdField
                .getText()
                .trim()
                .isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation Error",
                    "Please enter the donation ID."
            );

            return false;
        }

        int donationId;

        try {

            donationId =
                    Integer.parseInt(
                            donationIdField
                                    .getText()
                                    .trim()
                    );

        } catch (NumberFormatException e) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation Error",
                    "Donation ID must be a valid integer."
            );

            return false;
        }

        if (donationId <= 0) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation Error",
                    "Donation ID must be greater than zero."
            );

            return false;
        }

        if (bloodTypeBox.getValue() == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation Error",
                    "Please select the blood type."
            );

            return false;
        }

        if (componentTypeBox.getValue() == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation Error",
                    "Please select the component type."
            );

            return false;
        }

        if (volumeField
                .getText()
                .trim()
                .isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation Error",
                    "Please enter the volume."
            );

            return false;
        }

        int volume;

        try {

            volume =
                    Integer.parseInt(
                            volumeField
                                    .getText()
                                    .trim()
                    );

        } catch (NumberFormatException e) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation Error",
                    "Volume must be a valid integer."
            );

            return false;
        }

        if (volume <= 0) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation Error",
                    "Volume must be greater than zero."
            );

            return false;
        }

        if (collectionDatePicker.getValue() == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation Error",
                    "Please select the collection date."
            );

            return false;
        }

        if (expiryDatePicker.getValue() == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation Error",
                    "Please select the expiry date."
            );

            return false;
        }

        LocalDate collectionDate =
                collectionDatePicker.getValue();

        LocalDate expiryDate =
                expiryDatePicker.getValue();

        if (collectionDate.isAfter(LocalDate.now())) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation Error",
                    "Collection date cannot be in the future."
            );

            return false;
        }

        if (expiryDate.isBefore(collectionDate)) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation Error",
                    "Expiry date cannot be before the collection date."
            );

            return false;
        }

        if (statusBox.getValue() == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation Error",
                    "Please select the inventory status."
            );

            return false;
        }

        return true;
    }

    private void handleDatabaseError(SQLException e) {

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
    private void goToDashboard(ActionEvent event) {

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