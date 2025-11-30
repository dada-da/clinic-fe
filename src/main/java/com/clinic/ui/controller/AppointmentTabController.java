package com.clinic.ui.controller;

import com.clinic.ui.model.AppointmentDTO;
import com.clinic.ui.model.DoctorDTO;
import com.clinic.ui.model.MedicalRecordDTO;
import com.clinic.ui.model.PatientDTO;
import com.clinic.ui.service.ApiService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

public class AppointmentTabController {

    // Step 1: Patient
    @FXML private TextField txtSocialId;
    @FXML private Button btnCheckPatient;
    @FXML private TextField txtFullName;
    @FXML private DatePicker dpDob;
    @FXML private ComboBox<String> cmbGender;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;
    @FXML private TextArea txtAddress;
    @FXML private Button btnSavePatient;
    @FXML private Label lblPatientStatus;

    // Step 2: Doctor
    @FXML private TitledPane paneDoctor;
    @FXML private ComboBox<String> cmbDoctor;
    @FXML private TextField txtReason;
    @FXML private Button btnCreateAppointment;

    // Step 3: Medical
    @FXML private TitledPane paneMedical;
    @FXML private TextArea txtSymptoms;
    @FXML private TextArea txtDiagnosis;
    @FXML private TextArea txtTreatment;
    @FXML private Button btnSaveMedical;

    // Common
    @FXML private Button btnReset;

    private final ObjectMapper mapper;
    private PatientDTO currentPatient;
    private AppointmentDTO currentAppointment;
    private List<DoctorDTO> doctorList;

    private static final List<String> GENDER_LABELS = List.of("Nam", "Nữ", "Khác");

    private String tabName = "Lịch hẹn";
    private Consumer<String> statusReporter = message -> {};
    private ProgressHandle progressHandle = ProgressHandle.NO_OP;

    public AppointmentTabController() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @FXML
    public void initialize() {
        cmbGender.setItems(FXCollections.observableArrayList(GENDER_LABELS));

        btnCheckPatient.setOnAction(e -> checkPatient());
        btnSavePatient.setOnAction(e -> savePatient());
        btnCreateAppointment.setOnAction(e -> createAppointment());
        btnSaveMedical.setOnAction(e -> saveMedicalRecord());
        btnReset.setOnAction(e -> resetForm());

        setPatientFieldsEditable(true);
        btnSavePatient.setDisable(true);
        paneDoctor.setDisable(true);
        paneMedical.setDisable(true);

        String pattern = "dd/MM/yyyy";
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(pattern);

        dpDob.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                }
                return "";
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    try {
                        return LocalDate.parse(string, dateFormatter);
                    } catch (DateTimeParseException e) {
                        return null;
                    }
                }
                return null;
            }
        });

        dpDob.setPromptText(pattern.toLowerCase());

        dpDob.setEditable(true);
    }

    public void configure(String tabName,
                          Consumer<String> statusReporter,
                          ProgressHandle progressHandle) {
        if (tabName != null && !tabName.isBlank()) {
            this.tabName = tabName;
        }
        if (statusReporter != null) {
            this.statusReporter = statusReporter;
        }
        if (progressHandle != null) {
            this.progressHandle = progressHandle;
        }

        updateStatus("Nhập mã định danh bệnh nhân để bắt đầu");
        loadDoctors();
    }

    private boolean loadDoctors() {
        progressHandle.showIndeterminate("Đang tải danh sách bác sĩ...");
        try {
            String json = ApiService.getDoctors();
            doctorList = mapper.readValue(json, new TypeReference<List<DoctorDTO>>(){});

            ObservableList<String> doctorNames = FXCollections.observableArrayList();
            for (DoctorDTO doctor : doctorList) {
                String displayName = doctor.getFullName() + " - " + doctor.getSpecialty();
                doctorNames.add(displayName);
            }

            cmbDoctor.setItems(doctorNames);
            updateStatus("Đã tải " + doctorList.size() + " bác sĩ");
            return true;

        } catch (IOException e) {
            String errorMsg = "Không thể tải danh sách bác sĩ: " + e.getMessage();
            updateStatus(errorMsg);
            showError("Lỗi", "Không thể tải danh sách bác sĩ", e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            String errorMsg = "Việc tải danh sách bác sĩ bị gián đoạn";
            updateStatus(errorMsg);
            showError("Lỗi", "Không thể tải danh sách bác sĩ", errorMsg);
        } catch (Exception e) {
            String errorMsg = "Lỗi không xác định khi tải danh sách bác sĩ: " + e.getMessage();
            updateStatus(errorMsg);
            showError("Lỗi", "Không thể tải danh sách bác sĩ", e.getMessage());
        } finally {
            progressHandle.hide();
        }
        return false;
    }

    private boolean ensureDoctorsAvailable() {
        if (doctorList == null || doctorList.isEmpty() || cmbDoctor.getItems() == null || cmbDoctor.getItems().isEmpty()) {
            return loadDoctors();
        }
        return true;
    }

    public void loadAppointment(AppointmentDTO appointment) {
        System.out.println("DEBUG: Loading appointment for viewing: " + appointment.getId());

        progressHandle.showIndeterminate("Đang tải thông tin lịch hẹn...");
        try {
            String patientJson = ApiService.getPatientById(appointment.getPatientId());
            currentPatient = mapper.readValue(patientJson, PatientDTO.class);

            txtSocialId.setText(currentPatient.getSocialId());
            txtFullName.setText(currentPatient.getFullName());
            dpDob.setValue(currentPatient.getDob());
            cmbGender.setValue(toGenderLabel(currentPatient.getGender()));
            txtPhone.setText(currentPatient.getPhone());
            txtEmail.setText(currentPatient.getEmail());
            txtAddress.setText(currentPatient.getAddress());

            setPatientFieldsEditable(false);
            btnCheckPatient.setDisable(true);
            btnSavePatient.setDisable(true);

            lblPatientStatus.setText("✓ Bệnh nhân: " + currentPatient.getFullName());
            lblPatientStatus.setStyle("-fx-text-fill: green;");

            if (!ensureDoctorsAvailable()) {
                showError("Lỗi", "Không thể tải danh sách bác sĩ", "Vui lòng thử lại");
                return;
            }

            String doctorJson = ApiService.getDoctorById(appointment.getDoctorId());
            DoctorDTO doctor = mapper.readValue(doctorJson, DoctorDTO.class);
            String doctorDisplay = doctor.getFullName() + " - " + doctor.getSpecialty();
            cmbDoctor.setValue(doctorDisplay);

            txtReason.setText(appointment.getReason());

            currentAppointment = appointment;

            paneDoctor.setDisable(false);
            btnCreateAppointment.setDisable(true);

            try {
                String medicalJson = ApiService.getMedicalRecordByAppointmentId(appointment.getId());
                MedicalRecordDTO medical = mapper.readValue(medicalJson, MedicalRecordDTO.class);

                txtSymptoms.setText(medical.getSymptoms());
                txtDiagnosis.setText(medical.getDiagnosis());
                txtTreatment.setText(medical.getTreatment());

                paneMedical.setDisable(false);
                btnSaveMedical.setDisable(true);

                updateStatus("Đang xem lịch hẹn đã hoàn tất #" + appointment.getId());

            } catch (Exception e) {
                if (appointment.getStatus().equals("SCHEDULED")) {
                    paneMedical.setDisable(false);
                    btnSaveMedical.setDisable(false);

                    updateStatus("Lịch hẹn #" + appointment.getId() + " - Thêm bệnh án để hoàn tất");
                } else {
                    paneMedical.setDisable(true);
                    updateStatus("Đang xem lịch hẹn #" + appointment.getId());
                }
            }

        } catch (Exception e) {
            System.err.println("ERROR loading appointment: " + e.getMessage());
            e.printStackTrace();
            showError("Lỗi", "Không thể tải lịch hẹn", e.getMessage());
        } finally {
            progressHandle.hide();
        }
    }

    private DoctorDTO getSelectedDoctor() {
        String selectedName = cmbDoctor.getValue();
        if (selectedName == null || doctorList == null) {
            return null;
        }

        for (DoctorDTO doctor : doctorList) {
            String displayName = doctor.getFullName() + " - " + doctor.getSpecialty();
            if (displayName.equals(selectedName)) {
                return doctor;
            }
        }

        return null;
    }

    private void checkPatient() {
        String socialId = txtSocialId.getText().trim();

        if (socialId.isEmpty()) {
            showWarning("Lỗi xác thực", "Vui lòng nhập mã định danh");
            return;
        }

        progressHandle.showIndeterminate("Đang kiểm tra bệnh nhân...");
        try {
            String json = ApiService.getPatientBySocialId(socialId);
            currentPatient = mapper.readValue(json, PatientDTO.class);

            txtFullName.setText(currentPatient.getFullName());
            dpDob.setValue(currentPatient.getDob());
            cmbGender.setValue(toGenderLabel(currentPatient.getGender()));
            txtPhone.setText(currentPatient.getPhone());
            txtEmail.setText(currentPatient.getEmail());
            txtAddress.setText(currentPatient.getAddress());

            setPatientFieldsEditable(false);
            btnSavePatient.setDisable(true);

            lblPatientStatus.setText("✓ Đã tìm thấy bệnh nhân: " + currentPatient.getFullName());
            lblPatientStatus.setStyle("-fx-text-fill: green;");

            if (ensureDoctorsAvailable()) {
                paneDoctor.setDisable(false);
                updateStatus("Đã tìm thấy bệnh nhân. Vui lòng chọn bác sĩ.");
            } else {
                paneDoctor.setDisable(true);
                lblPatientStatus.setText("Đã tìm thấy bệnh nhân nhưng không tải được danh sách bác sĩ.");
                lblPatientStatus.setStyle("-fx-text-fill: red;");
            }

        } catch (IOException e) {
            if (e.getMessage().contains("not found")) {
                lblPatientStatus.setText("Không tìm thấy bệnh nhân. Vui lòng nhập thông tin để tạo mới.");
                lblPatientStatus.setStyle("-fx-text-fill: orange;");

                setPatientFieldsEditable(true);
                btnSavePatient.setDisable(false);

                updateStatus("Không tìm thấy bệnh nhân. Hãy nhập thông tin để tạo mới.");
            } else {
                showError("Lỗi", "Không thể kiểm tra bệnh nhân", e.getMessage());
            }
        } catch (Exception e) {
            showError("Lỗi", "Không thể kiểm tra bệnh nhân", e.getMessage());
        } finally {
            progressHandle.hide();
        }
    }

    private void savePatient() {
        if (!validatePatientFields()) {
            return;
        }

        progressHandle.showIndeterminate("Đang tạo hồ sơ bệnh nhân...");
        try {
            PatientDTO newPatient = new PatientDTO();
            newPatient.setSocialId(txtSocialId.getText().trim());
            newPatient.setFullName(txtFullName.getText().trim());
            newPatient.setDob(dpDob.getValue());
            newPatient.setGender(toGenderCode(cmbGender.getValue()));
            newPatient.setPhone(txtPhone.getText().trim());
            newPatient.setEmail(txtEmail.getText().trim());
            newPatient.setAddress(txtAddress.getText().trim());

            String jsonBody = mapper.writeValueAsString(newPatient);
            String response = ApiService.createPatient(jsonBody);

            currentPatient = mapper.readValue(response, PatientDTO.class);

            lblPatientStatus.setText("✓ Đã tạo bệnh nhân: " + currentPatient.getFullName());
            lblPatientStatus.setStyle("-fx-text-fill: green;");

            setPatientFieldsEditable(false);
            btnSavePatient.setDisable(true);

            if (ensureDoctorsAvailable()) {
                paneDoctor.setDisable(false);
                updateStatus("Tạo bệnh nhân thành công. Vui lòng chọn bác sĩ.");
            } else {
                paneDoctor.setDisable(true);
                lblPatientStatus.setText("Đã tạo bệnh nhân nhưng không tải được danh sách bác sĩ.");
                lblPatientStatus.setStyle("-fx-text-fill: red;");
            }

        } catch (Exception e) {
            showError("Lỗi", "Không thể tạo bệnh nhân", e.getMessage());
        } finally {
            progressHandle.hide();
        }
    }

    private void createAppointment() {
        if (currentPatient == null) {
            showWarning("Lỗi", "Vui lòng hoàn tất thông tin bệnh nhân trước");
            return;
        }

        if (!ensureDoctorsAvailable()) {
            showError("Lỗi", "Không có dữ liệu bác sĩ", "Không thể tải danh sách bác sĩ. Vui lòng thử lại.");
            return;
        }

        DoctorDTO selectedDoctor = getSelectedDoctor();
        if (selectedDoctor == null) {
            showWarning("Lỗi xác thực", "Vui lòng chọn bác sĩ");
            return;
        }

        progressHandle.showIndeterminate("Đang tạo lịch hẹn...");
        try {
            AppointmentDTO appointment = new AppointmentDTO();
            appointment.setPatientId(currentPatient.getId());
            appointment.setDoctorId(selectedDoctor.getId());
            appointment.setDateTime(LocalDateTime.now());
            appointment.setStatus("SCHEDULED");
            appointment.setReason(txtReason.getText().trim());

            String jsonBody = mapper.writeValueAsString(appointment);
            String response = ApiService.createAppointment(jsonBody);

            currentAppointment = mapper.readValue(response, AppointmentDTO.class);

            showInfo("Thành công", "Tạo lịch hẹn thành công!");

            paneDoctor.setDisable(true);
            paneMedical.setDisable(false);

            updateStatus("Đã tạo lịch hẹn. Vui lòng nhập thông tin khám bệnh.");

        } catch (Exception e) {
            showError("Lỗi", "Không thể tạo lịch hẹn", e.getMessage());
        } finally {
            progressHandle.hide();
        }
    }

    private void saveMedicalRecord() {
        if (currentAppointment == null) {
            showWarning("Lỗi", "Vui lòng tạo lịch hẹn trước");
            return;
        }

        if (txtDiagnosis.getText().trim().isEmpty()) {
            showWarning("Lỗi xác thực", "Vui lòng nhập chẩn đoán");
            return;
        }

        progressHandle.showIndeterminate("Đang lưu bệnh án...");
        try {
            MedicalRecordDTO medicalRecord = new MedicalRecordDTO();
            medicalRecord.setAppointmentId(currentAppointment.getId());
            medicalRecord.setSymptoms(txtSymptoms.getText().trim());
            medicalRecord.setDiagnosis(txtDiagnosis.getText().trim());
            medicalRecord.setTreatment(txtTreatment.getText().trim());

            String jsonBody = mapper.writeValueAsString(medicalRecord);
            ApiService.createMedicalRecord(jsonBody);

            showInfo("Thành công", "Đã lưu bệnh án và hoàn tất lịch hẹn!");

            updateStatus("Hoàn tất lịch hẹn thành công!");

            boolean createAnother = showConfirmation(
                    "Tạo thêm?",
                    "Hoàn tất lịch hẹn thành công!",
                    "Bạn có muốn tạo lịch hẹn khác không?"
            );

            if (createAnother) {
                resetForm();
            } else {
                disableAllFields();
            }

        } catch (Exception e) {
            showError("Lỗi", "Không thể lưu bệnh án", e.getMessage());
        } finally {
            progressHandle.hide();
        }
    }

    private void resetForm() {
        txtSocialId.clear();
        txtFullName.clear();
        dpDob.setValue(null);
        cmbGender.setValue(null);
        txtPhone.clear();
        txtEmail.clear();
        txtAddress.clear();
        txtReason.clear();
        txtSymptoms.clear();
        txtDiagnosis.clear();
        txtTreatment.clear();
        cmbDoctor.setValue(null);

        lblPatientStatus.setText("");

        currentPatient = null;
        currentAppointment = null;

        setPatientFieldsEditable(true);
        btnCheckPatient.setDisable(false);
        btnSavePatient.setDisable(true);
        paneDoctor.setDisable(true);
        paneMedical.setDisable(true);

        updateStatus("Nhập mã định danh bệnh nhân để bắt đầu");
    }

    private void disableAllFields() {
        setPatientFieldsEditable(false);
        btnCheckPatient.setDisable(true);
        btnSavePatient.setDisable(true);
        btnCreateAppointment.setDisable(true);
        btnSaveMedical.setDisable(true);
        paneDoctor.setDisable(true);
        paneMedical.setDisable(true);
    }

    private void setPatientFieldsEditable(boolean editable) {
        txtFullName.setEditable(editable);
        dpDob.setDisable(!editable);
        cmbGender.setDisable(!editable);
        txtPhone.setEditable(editable);
        txtEmail.setEditable(editable);
        txtAddress.setEditable(editable);
    }

    private boolean validatePatientFields() {
        if (txtFullName.getText().trim().isEmpty()) {
            showWarning("Lỗi xác thực", "Vui lòng nhập họ và tên");
            return false;
        }
        if (dpDob.getValue() == null) {
            showWarning("Lỗi xác thực", "Vui lòng chọn ngày sinh");
            return false;
        }
        if (cmbGender.getValue() == null) {
            showWarning("Lỗi xác thực", "Vui lòng chọn giới tính");
            return false;
        }
        return true;
    }

    private String toGenderLabel(String code) {
        if (code == null) {
            return null;
        }
        switch (code) {
            case "MALE":
                return "Nam";
            case "FEMALE":
                return "Nữ";
            case "OTHER":
                return "Khác";
            default:
                return code;
        }
    }

    private String toGenderCode(String label) {
        if (label == null) {
            return null;
        }
        switch (label) {
            case "Nam":
                return "MALE";
            case "Nữ":
                return "FEMALE";
            case "Khác":
                return "OTHER";
            default:
                return label;
        }
    }

    private void updateStatus(String message) {
        statusReporter.accept("[" + tabName + "] " + message);
        System.out.println("STATUS: [" + tabName + "] " + message);
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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

    public interface ProgressHandle {
        default void showIndeterminate(String message) {}
        default void hide() {}

        ProgressHandle NO_OP = new ProgressHandle() {};
    }
}

