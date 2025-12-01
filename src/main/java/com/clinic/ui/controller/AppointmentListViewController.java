package com.clinic.ui.controller;

import com.clinic.ui.model.AppointmentDTO;
import com.clinic.ui.service.ApiService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class AppointmentListViewController {

    public enum AppointmentFilter {
        TODAY,
        ALL
    }

    @FXML private Label lblTitle;
    @FXML private TableView<AppointmentDTO> tableAppointments;
    @FXML private TableColumn<AppointmentDTO, Integer> colId;
    @FXML private TableColumn<AppointmentDTO, LocalDateTime> colTime;
    @FXML private TableColumn<AppointmentDTO, String> colPatient;
    @FXML private TableColumn<AppointmentDTO, String> colDoctor;
    @FXML private TableColumn<AppointmentDTO, String> colReason;
    @FXML private TableColumn<AppointmentDTO, String> colStatus;
    @FXML private Button btnRefresh;
    @FXML private Button btnNewAppointment;
    @FXML private Label lblDate;
    @FXML private Label lblStatus;
    @FXML private Label lblCount;

    private final ObjectMapper mapper;
    private TabPane tabPane;
    private Consumer<String> statusReporter = message -> {};
    private Map<Integer, Tab> appointmentTabs = new HashMap<>();
    private AppointmentFilter currentFilter = AppointmentFilter.TODAY;

    public AppointmentListViewController() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @FXML
    public void initialize() {
        System.out.println("DEBUG: AppointmentListViewController.initialize() called");

        colId.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getId()));
        colTime.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getDateTime()));
        colPatient.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPatientName()));
        colDoctor.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDoctorName()));
        colReason.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getReason()));
        colStatus.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(translateStatus(cellData.getValue().getStatus())));

        colTime.setCellFactory(column -> new TableCell<AppointmentDTO, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });

        colStatus.setCellFactory(column -> new TableCell<AppointmentDTO, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Đang khám")) {
                        setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2;");
                    } else if (item.equals("Hoàn tất")) {
                        setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #388e3c;");
                    } else if (item.equals("Đã hủy")) {
                        setStyle("-fx-background-color: #ffebee; -fx-text-fill: #d32f2f;");
                    } else if (item.equals("Vắng mặt")) {
                        setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #f57c00;");
                    }
                }
            }
        });

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy");
        lblDate.setText("Ngày: " + LocalDate.now().format(dateFormatter));

        tableAppointments.setRowFactory(tv -> {
            TableRow<AppointmentDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openAppointmentTab(row.getItem());
                }
            });
            return row;
        });

        btnRefresh.setOnAction(e -> loadAppointments());
        btnNewAppointment.setOnAction(e -> createNewAppointmentTab());
    }

    public void setTabPane(TabPane tabPane) {
        this.tabPane = tabPane;
    }

    public void setStatusReporter(Consumer<String> statusReporter) {
        if (statusReporter != null) {
            this.statusReporter = statusReporter;
        }
    }

    public void setFilter(AppointmentFilter filter) {
        this.currentFilter = filter;
        updateFilterDisplay();
    }

    public void configure() {
        updateFilterDisplay();
        updateStatus("Sẵn sàng");
        loadAppointments();
    }

    private void updateFilterDisplay() {
        if (currentFilter == AppointmentFilter.ALL) {
            if (lblTitle != null) {
                lblTitle.setText("Danh Sách Tất Cả Lịch Hẹn");
            }
            if (lblDate != null) {
                lblDate.setText("Tất cả lịch hẹn");
            }
        } else {
            if (lblTitle != null) {
                lblTitle.setText("Danh Sách Lịch Hẹn Hôm Nay");
            }
            if (lblDate != null) {
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy");
                lblDate.setText("Ngày: " + LocalDate.now().format(dateFormatter));
            }
        }
    }

    private void loadAppointments() {
        try {
            System.out.println("DEBUG: Loading appointments with filter: " + currentFilter);
            updateStatus("Đang tải danh sách lịch hẹn...");

            String json;
            switch (currentFilter) {
                case ALL:
                    json = ApiService.getAppointments();
                    break;
                case TODAY:
                default:
                    json = ApiService.getTodayAppointments();
                    break;
            }

            System.out.println("DEBUG: Received JSON length: " + json.length());

            List<AppointmentDTO> appointments = mapper.readValue(json, new TypeReference<List<AppointmentDTO>>(){});
            System.out.println("DEBUG: Parsed " + appointments.size() + " appointments");

            tableAppointments.setItems(FXCollections.observableArrayList(appointments));

            String filterText = currentFilter == AppointmentFilter.ALL ? "tất cả" : "hôm nay";
            lblCount.setText("Tổng: " + appointments.size() + " lịch hẹn " + filterText);
            updateStatus("Đã tải " + appointments.size() + " lịch hẹn " + filterText);

        } catch (Exception e) {
            System.err.println("ERROR loading appointments: " + e.getMessage());
            e.printStackTrace();
            updateStatus("Lỗi khi tải danh sách lịch hẹn: " + e.getMessage());
            showError("Lỗi", "Không thể tải danh sách lịch hẹn", e.getMessage());
        }
    }

    private void openAppointmentTab(AppointmentDTO appointment) {
        if (tabPane == null) {
            System.err.println("ERROR: TabPane not set!");
            return;
        }

        if (appointmentTabs.containsKey(appointment.getId())) {
            Tab existingTab = appointmentTabs.get(appointment.getId());
            tabPane.getSelectionModel().select(existingTab);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/appointment_view.fxml"));
            javafx.scene.Parent content = loader.load();

            AppointmentTabController controller = loader.getController();

            String tabName = "LH #" + appointment.getId();
            Tab newTab = new Tab(tabName, content);
            newTab.setClosable(true);

            controller.configure(
                    tabName,
                    this::updateStatus,
                    new AppointmentTabController.ProgressHandle() {
                        @Override
                        public void showIndeterminate(String message) {
                            updateStatus(message);
                        }

                        @Override
                        public void hide() {
                            // No-op for now
                        }
                    }
            );

            controller.loadAppointment(appointment);

            tabPane.getTabs().add(newTab);
            appointmentTabs.put(appointment.getId(), newTab);

            newTab.setOnClosed(e -> appointmentTabs.remove(appointment.getId()));

            tabPane.getSelectionModel().select(newTab);

            System.out.println("DEBUG: Opened appointment #" + appointment.getId() + " in new tab");

        } catch (IOException e) {
            System.err.println("ERROR: Failed to load appointment view: " + e.getMessage());
            e.printStackTrace();
            showError("Lỗi", "Không thể mở lịch hẹn", e.getMessage());
        }
    }

    private void createNewAppointmentTab() {
        if (tabPane == null) {
            System.err.println("ERROR: TabPane not set!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/appointment_view.fxml"));
            javafx.scene.Parent content = loader.load();

            AppointmentTabController controller = loader.getController();

            String tabName = "Lịch hẹn mới";
            Tab newTab = new Tab(tabName, content);
            newTab.setClosable(true);

            controller.configure(
                    tabName,
                    this::updateStatus,
                    new AppointmentTabController.ProgressHandle() {
                        @Override
                        public void showIndeterminate(String message) {
                            updateStatus(message);
                        }

                        @Override
                        public void hide() {
                            // No-op
                        }
                    }
            );

            tabPane.getTabs().add(newTab);

            tabPane.getSelectionModel().select(newTab);

            System.out.println("DEBUG: Created new appointment tab");

        } catch (IOException e) {
            System.err.println("ERROR: Failed to create new appointment tab: " + e.getMessage());
            e.printStackTrace();
            showError("Lỗi", "Không thể tạo tab mới", e.getMessage());
        }
    }

    public void refreshList() {
        Platform.runLater(this::loadAppointments);
    }

    private String translateStatus(String status) {
        if (status == null) return "";
        switch (status) {
            case "SCHEDULED": return "Đang khám";
            case "COMPLETED": return "Hoàn tất";
            case "CANCELLED": return "Đã hủy";
            case "NO_SHOW": return "Vắng mặt";
            default: return status;
        }
    }

    private void updateStatus(String message) {
        if (lblStatus != null) {
            Platform.runLater(() -> lblStatus.setText(message));
        }
        statusReporter.accept(message);
        System.out.println("STATUS: " + message);
    }

    private void showError(String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}
