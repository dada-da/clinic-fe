package com.clinic.ui.controller;

import com.clinic.ui.service.ApiService;
import com.clinic.ui.model.PatientDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PatientViewController {
    @FXML private TableView<PatientDTO> table;
    @FXML private TableColumn<PatientDTO, Integer> colId;
    @FXML private TableColumn<PatientDTO, String> colSocialId;
    @FXML private TableColumn<PatientDTO, String> colName;
    @FXML private TableColumn<PatientDTO, LocalDate> colDob;
    @FXML private TableColumn<PatientDTO, String> colGender;
    @FXML private TableColumn<PatientDTO, String> colPhone;
    @FXML private TableColumn<PatientDTO, String> colEmail;
    @FXML private TableColumn<PatientDTO, String> colAddress;
    @FXML private TextField txtSearch;
    @FXML private Button btnRefresh;
    @FXML private Button btnSearch;
    @FXML private Button btnEdit;
    @FXML private Label lblStatus;

    private final ObjectMapper mapper;

    public PatientViewController() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSocialId.setCellValueFactory(new PropertyValueFactory<>("socialId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colDob.setCellValueFactory(new PropertyValueFactory<>("dob"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));

        colDob.setCellFactory(column -> new TableCell<PatientDTO, LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });

        btnRefresh.setOnAction(e -> loadPatients());
        btnSearch.setOnAction(e -> searchPatients());
        btnEdit.setOnAction(e -> editPatient());

        txtSearch.setOnAction(e -> searchPatients());

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            btnEdit.setDisable(!hasSelection);
        });

        btnEdit.setDisable(true);

        loadPatients();
    }

    private void loadPatients() {
        try {
            updateStatus("Đang tải danh sách bệnh nhân...");
            String json = ApiService.getPatients();

            List<PatientDTO> list = mapper.readValue(json, new TypeReference<List<PatientDTO>>(){});

            table.setItems(FXCollections.observableArrayList(list));
            updateStatus("Đã tải " + list.size() + " bệnh nhân");
        } catch (Exception ex) {
            updateStatus("Lỗi tải bệnh nhân: " + ex.getMessage());
            showError("Không thể tải bệnh nhân", "Không thể tải bệnh nhân từ máy chủ", ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void searchPatients() {
        String searchText = txtSearch.getText();
        if (searchText == null || searchText.trim().isEmpty()) {
            loadPatients();
            return;
        }

        try {
            updateStatus("Đang tìm kiếm bệnh nhân...");
            String json = ApiService.searchPatients(searchText.trim());

            List<PatientDTO> list = mapper.readValue(json, new TypeReference<List<PatientDTO>>(){});

            table.setItems(FXCollections.observableArrayList(list));
            updateStatus("Tìm thấy " + list.size() + " bệnh nhân khớp '" + searchText + "'");
        } catch (Exception ex) {
            updateStatus("Lỗi tìm kiếm bệnh nhân: " + ex.getMessage());
            showError("Không thể tìm kiếm bệnh nhân", "Không thể thực hiện tìm kiếm bệnh nhân", ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void editPatient() {
        PatientDTO selectedPatient = table.getSelectionModel().getSelectedItem();
        if (selectedPatient == null) {
            showWarning("Chưa chọn", "Vui lòng chọn bệnh nhân để chỉnh sửa");
            return;
        }
        updateStatus("Chức năng chỉnh sửa bệnh nhân - sắp ra mắt");
    }

    private void updateStatus(String message) {
        if (lblStatus != null) {
            lblStatus.setText(message);
        }
    }

    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        return alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .isPresent();
    }
}
