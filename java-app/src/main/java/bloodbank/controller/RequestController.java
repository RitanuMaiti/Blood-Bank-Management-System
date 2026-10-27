package bloodbank.controller;

import bloodbank.NavigationUtil;
import bloodbank.model.BloodRequest;
import bloodbank.service.RequestService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class RequestController {

    @FXML private TextField hospitalIdField;
    @FXML private TextField unitsRequestedField;
    @FXML private TextField searchField;

    @FXML private ComboBox<String> bloodTypeBox;
    @FXML private ComboBox<String> componentBox;
    @FXML private ComboBox<String> urgencyBox;
    @FXML private ComboBox<String> statusBox;

    @FXML private TableView<BloodRequest> requestTable;
    @FXML private TableColumn<BloodRequest, Number> idColumn;
    @FXML private TableColumn<BloodRequest, Number> hospitalColumn;
    @FXML private TableColumn<BloodRequest, String> bloodTypeColumn;
    @FXML private TableColumn<BloodRequest, String> componentColumn;
    @FXML private TableColumn<BloodRequest, Number> unitsColumn;
    @FXML private TableColumn<BloodRequest, String> urgencyColumn;
    @FXML private TableColumn<BloodRequest, LocalDateTime> dateColumn;
    @FXML private TableColumn<BloodRequest, String> statusColumn;

    private final RequestService requestService = new RequestService();

    private final ObservableList<BloodRequest> requests =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        bloodTypeBox.setItems(FXCollections.observableArrayList(
                "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
        ));

        componentBox.setItems(FXCollections.observableArrayList(
                "Whole Blood",
                "Red Blood Cells",
                "Platelets",
                "Fresh Frozen Plasma"
        ));

        urgencyBox.setItems(FXCollections.observableArrayList(
                "Routine", "Urgent", "Critical"
        ));

        statusBox.setItems(FXCollections.observableArrayList(
                "Pending",
                "Partially_Fulfilled",
                "Fulfilled",
                "Cancelled"
        ));

        idColumn.setCellValueFactory(
                new PropertyValueFactory<>("requestId"));
        hospitalColumn.setCellValueFactory(
                new PropertyValueFactory<>("hospitalId"));
        bloodTypeColumn.setCellValueFactory(
                new PropertyValueFactory<>("requiredBloodType"));
        componentColumn.setCellValueFactory(
                new PropertyValueFactory<>("requiredComponent"));
        unitsColumn.setCellValueFactory(
                new PropertyValueFactory<>("unitsRequested"));
        urgencyColumn.setCellValueFactory(
                new PropertyValueFactory<>("urgencyLevel"));
        dateColumn.setCellValueFactory(
                new PropertyValueFactory<>("requestTimestamp"));
        statusColumn.setCellValueFactory(
                new PropertyValueFactory<>("requestStatus"));

        requestTable.setItems(requests);

        requestTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldRequest, newRequest) -> {
                    if (newRequest != null) {
                        populateForm(newRequest);
                    }
                });

        loadRequests();
    }

    @FXML
    public void addRequest() {
        try {
            BloodRequest request = readForm();

            request.setRequestTimestamp(LocalDateTime.now());
            request.setRequestStatus("Pending");

            requestService.create(request);

            showMessage(
                    Alert.AlertType.INFORMATION,
                    "Success",
                    "Blood request created successfully."
            );

            clearForm();
            loadRequests();

        } catch (NumberFormatException e) {
            showMessage(
                    Alert.AlertType.WARNING,
                    "Invalid Input",
                    "Hospital ID and units must be valid whole numbers."
            );
        } catch (IllegalArgumentException e) {
            showMessage(
                    Alert.AlertType.WARNING,
                    "Invalid Input",
                    e.getMessage()
            );
        } catch (SQLException e) {
            showMessage(
                    Alert.AlertType.ERROR,
                    "Database Error",
                    "Could not create the request.\n" + e.getMessage()
            );
        }
    }

    @FXML
    public void updateRequestStatus() {
        BloodRequest selected =
                requestTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showMessage(
                    Alert.AlertType.WARNING,
                    "No Selection",
                    "Select a request from the table first."
            );
            return;
        }

        String newStatus = statusBox.getValue();

        if (newStatus == null || newStatus.isBlank()) {
            showMessage(
                    Alert.AlertType.WARNING,
                    "Missing Status",
                    "Select the new request status."
            );
            return;
        }

        try {
            requestService.updateStatus(
                    selected.getRequestId(),
                    newStatus
            );

            showMessage(
                    Alert.AlertType.INFORMATION,
                    "Success",
                    "Request status updated successfully."
            );

            clearForm();
            loadRequests();

        } catch (SQLException e) {
            showMessage(
                    Alert.AlertType.ERROR,
                    "Database Error",
                    "Could not update the request status.\n"
                            + e.getMessage()
            );
        }
    }

    @FXML
    public void deleteRequest() {
        BloodRequest selected =
                requestTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showMessage(
                    Alert.AlertType.WARNING,
                    "No Selection",
                    "Select a request to delete."
            );
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText("Delete this blood request?");
        confirmation.setContentText(
                "Request ID: " + selected.getRequestId()
        );

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL)
                != ButtonType.OK) {
            return;
        }

        try {
            requestService.delete(selected.getRequestId());

            showMessage(
                    Alert.AlertType.INFORMATION,
                    "Success",
                    "Request deleted successfully."
            );

            clearForm();
            loadRequests();

        } catch (SQLException e) {
            showMessage(
                    Alert.AlertType.ERROR,
                    "Database Error",
                    "Could not delete the request.\n" + e.getMessage()
            );
        }
    }

    @FXML
    public void loadRequests() {
        try {
            requests.setAll(requestService.getAll());
        } catch (SQLException e) {
            showMessage(
                    Alert.AlertType.ERROR,
                    "Database Unavailable",
                    "Could not load blood requests.\n" + e.getMessage()
            );
        }
    }

    @FXML
    public void searchRequests() {
        String query = searchField.getText();

        if (query == null || query.isBlank()) {
            loadRequests();
            return;
        }

        String normalized = query.trim().toLowerCase();

        try {
            requests.setAll(requestService.getAll().stream()
                    .filter(r ->
                            String.valueOf(r.getRequestId()).contains(normalized)
                            || String.valueOf(r.getHospitalId()).contains(normalized)
                            || String.valueOf(r.getRequiredBloodType()).toLowerCase().contains(normalized)
                            || String.valueOf(r.getRequiredComponent()).toLowerCase().contains(normalized)
                            || String.valueOf(r.getUrgencyLevel()).toLowerCase().contains(normalized)
                            || String.valueOf(r.getRequestStatus()).toLowerCase().contains(normalized)
                    )
                    .toList());
        } catch (SQLException e) {
            showMessage(Alert.AlertType.ERROR,
                    "Search Failed",
                    "Could not search requests.\n" + e.getMessage());
        }
    }

    @FXML
    public void clearForm() {
        hospitalIdField.clear();
        unitsRequestedField.clear();

        bloodTypeBox.getSelectionModel().clearSelection();
        componentBox.getSelectionModel().clearSelection();
        urgencyBox.getSelectionModel().clearSelection();
        statusBox.setValue("Pending");

        requestTable.getSelectionModel().clearSelection();
    }

    @FXML
    public void goToDashboard(ActionEvent event) {
        NavigationUtil.goToDashboard(event);
    }

    private BloodRequest readForm() {
        String hospitalText = required(
                hospitalIdField.getText(), "Hospital ID");

        String unitsText = required(
                unitsRequestedField.getText(), "Units requested");

        int hospitalId = Integer.parseInt(hospitalText);
        int units = Integer.parseInt(unitsText);

        if (hospitalId <= 0) {
            throw new IllegalArgumentException(
                    "Hospital ID must be greater than zero.");
        }

        if (units <= 0) {
            throw new IllegalArgumentException(
                    "Units requested must be greater than zero.");
        }

        BloodRequest request = new BloodRequest();

        request.setHospitalId(hospitalId);
        request.setRequiredBloodType(
                requiredSelection(bloodTypeBox.getValue(), "Blood type"));
        request.setRequiredComponent(
                requiredSelection(componentBox.getValue(), "Component"));
        request.setUnitsRequested(units);
        request.setUrgencyLevel(
                requiredSelection(urgencyBox.getValue(), "Urgency"));

        return request;
    }

    private String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " is required.");
        }

        return value.trim();
    }

    private String requiredSelection(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Please select " + field.toLowerCase() + ".");
        }

        return value;
    }

    private void populateForm(BloodRequest request) {
        hospitalIdField.setText(
                String.valueOf(request.getHospitalId()));

        unitsRequestedField.setText(
                String.valueOf(request.getUnitsRequested()));

        bloodTypeBox.setValue(request.getRequiredBloodType());
        componentBox.setValue(request.getRequiredComponent());
        urgencyBox.setValue(request.getUrgencyLevel());
        statusBox.setValue(request.getRequestStatus());
    }

    private void showMessage(
            Alert.AlertType type,
            String title,
            String message) {

        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
