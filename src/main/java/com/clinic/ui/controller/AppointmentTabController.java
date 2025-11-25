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

    private String tabName = "Appointment";
    private Consumer<String> statusReporter = message -> {};
    private ProgressHandle progressHandle = ProgressHandle.NO_OP;

    public AppointmentTabController() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @FXML
    public void initialize() {
        cmbGender.getItems().addAll("MALE", "FEMALE", "OTHER");

        btnCheckPatient.setOnAction(e -> checkPatient());
        btnSavePatient.setOnAction(e -> savePatient());
        btnCreateAppointment.setOnAction(e -> createAppointment());
        btnSaveMedical.setOnAction(e -> saveMedicalRecord());
        btnReset.setOnAction(e -> resetForm());

        setPatientFieldsEditable(true);
        btnSavePatient.setDisable(true);
        paneDoctor.setDisable(true);
        paneMedical.setDisable(true);
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

        updateStatus("Enter patient Social ID to begin");
        loadDoctors();
    }

    private boolean loadDoctors() {
        progressHandle.showIndeterminate("Loading doctors...");
        try {
            String json = ApiService.getDoctors();
            doctorList = mapper.readValue(json, new TypeReference<List<DoctorDTO>>(){});

            ObservableList<String> doctorNames = FXCollections.observableArrayList();
            for (DoctorDTO doctor : doctorList) {
                String displayName = doctor.getFullName() + " - " + doctor.getSpecialty();
                doctorNames.add(displayName);
            }

            cmbDoctor.setItems(doctorNames);
            updateStatus("Loaded " + doctorList.size() + " doctors");
            return true;

        } catch (IOException e) {
            String errorMsg = "Failed to load doctors: " + e.getMessage();
            updateStatus(errorMsg);
            showError("Error", "Failed to load doctors", e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            String errorMsg = "Doctor loading interrupted";
            updateStatus(errorMsg);
            showError("Error", "Failed to load doctors", errorMsg);
        } catch (Exception e) {
            String errorMsg = "Unexpected error loading doctors: " + e.getMessage();
            updateStatus(errorMsg);
            showError("Error", "Failed to load doctors", e.getMessage());
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
            showWarning("Validation Error", "Please enter a Social ID");
            return;
        }

        progressHandle.showIndeterminate("Checking patient...");
        try {
            String json = ApiService.getPatientBySocialId(socialId);
            currentPatient = mapper.readValue(json, PatientDTO.class);

            txtFullName.setText(currentPatient.getFullName());
            dpDob.setValue(currentPatient.getDob());
            cmbGender.setValue(currentPatient.getGender());
            txtPhone.setText(currentPatient.getPhone());
            txtEmail.setText(currentPatient.getEmail());
            txtAddress.setText(currentPatient.getAddress());

            setPatientFieldsEditable(false);
            btnSavePatient.setDisable(true);

            lblPatientStatus.setText("✓ Patient found: " + currentPatient.getFullName());
            lblPatientStatus.setStyle("-fx-text-fill: green;");

            if (ensureDoctorsAvailable()) {
                paneDoctor.setDisable(false);
                updateStatus("Patient found. Now select a doctor.");
            } else {
                paneDoctor.setDisable(true);
                lblPatientStatus.setText("Patient found, but doctors could not be loaded.");
                lblPatientStatus.setStyle("-fx-text-fill: red;");
            }

        } catch (IOException e) {
            if (e.getMessage().contains("not found")) {
                lblPatientStatus.setText("Patient not found. Please fill in details to create new patient.");
                lblPatientStatus.setStyle("-fx-text-fill: orange;");

                setPatientFieldsEditable(true);
                btnSavePatient.setDisable(false);

                updateStatus("Patient not found. Fill in details to create new patient.");
            } else {
                showError("Error", "Failed to check patient", e.getMessage());
            }
        } catch (Exception e) {
            showError("Error", "Failed to check patient", e.getMessage());
        } finally {
            progressHandle.hide();
        }
    }

    private void savePatient() {
        if (!validatePatientFields()) {
            return;
        }

        progressHandle.showIndeterminate("Creating patient...");
        try {
            PatientDTO newPatient = new PatientDTO();
            newPatient.setSocialId(txtSocialId.getText().trim());
            newPatient.setFullName(txtFullName.getText().trim());
            newPatient.setDob(dpDob.getValue());
            newPatient.setGender(cmbGender.getValue());
            newPatient.setPhone(txtPhone.getText().trim());
            newPatient.setEmail(txtEmail.getText().trim());
            newPatient.setAddress(txtAddress.getText().trim());

            String jsonBody = mapper.writeValueAsString(newPatient);
            String response = ApiService.createPatient(jsonBody);

            currentPatient = mapper.readValue(response, PatientDTO.class);

            lblPatientStatus.setText("✓ Patient created: " + currentPatient.getFullName());
            lblPatientStatus.setStyle("-fx-text-fill: green;");

            setPatientFieldsEditable(false);
            btnSavePatient.setDisable(true);

            if (ensureDoctorsAvailable()) {
                paneDoctor.setDisable(false);
                updateStatus("Patient created successfully. Now select a doctor.");
            } else {
                paneDoctor.setDisable(true);
                lblPatientStatus.setText("Patient created, but doctors could not be loaded.");
                lblPatientStatus.setStyle("-fx-text-fill: red;");
            }

        } catch (Exception e) {
            showError("Error", "Failed to create patient", e.getMessage());
        } finally {
            progressHandle.hide();
        }
    }

    private void createAppointment() {
        if (currentPatient == null) {
            showWarning("Error", "Please complete patient information first");
            return;
        }

        if (!ensureDoctorsAvailable()) {
            showError("Error", "Doctors unavailable", "Unable to load doctors. Please try again.");
            return;
        }

        DoctorDTO selectedDoctor = getSelectedDoctor();
        if (selectedDoctor == null) {
            showWarning("Validation Error", "Please select a doctor");
            return;
        }

        progressHandle.showIndeterminate("Creating appointment...");
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

            showInfo("Success", "Appointment created successfully!\nAppointment ID: " + currentAppointment.getId());

            paneDoctor.setDisable(true);
            paneMedical.setDisable(false);

            updateStatus("Appointment created. Now add medical examination details.");

        } catch (Exception e) {
            showError("Error", "Failed to create appointment", e.getMessage());
        } finally {
            progressHandle.hide();
        }
    }

    private void saveMedicalRecord() {
        if (currentAppointment == null) {
            showWarning("Error", "Please create appointment first");
            return;
        }

        if (txtDiagnosis.getText().trim().isEmpty()) {
            showWarning("Validation Error", "Diagnosis is required");
            return;
        }

        progressHandle.showIndeterminate("Saving medical record...");
        try {
            MedicalRecordDTO medicalRecord = new MedicalRecordDTO();
            medicalRecord.setAppointmentId(currentAppointment.getId());
            medicalRecord.setSymptoms(txtSymptoms.getText().trim());
            medicalRecord.setDiagnosis(txtDiagnosis.getText().trim());
            medicalRecord.setTreatment(txtTreatment.getText().trim());

            String jsonBody = mapper.writeValueAsString(medicalRecord);
            ApiService.createMedicalRecord(jsonBody);

            showInfo("Success", "Medical record saved and appointment completed!");

            updateStatus("Appointment completed successfully!");

            boolean createAnother = showConfirmation(
                    "Create Another?",
                    "Appointment completed successfully!",
                    "Do you want to create another appointment?"
            );

            if (createAnother) {
                resetForm();
            } else {
                disableAllFields();
            }

        } catch (Exception e) {
            showError("Error", "Failed to save medical record", e.getMessage());
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

        updateStatus("Enter patient Social ID to begin");
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
            showWarning("Validation Error", "Full name is required");
            return false;
        }
        if (dpDob.getValue() == null) {
            showWarning("Validation Error", "Date of birth is required");
            return false;
        }
        if (cmbGender.getValue() == null) {
            showWarning("Validation Error", "Gender is required");
            return false;
        }
        return true;
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

