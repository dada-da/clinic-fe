package com.clinic.ui.controller;

import com.clinic.ui.model.*;
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

public class AppointmentViewController {

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

    // Step 2: Doctor - CHANGED TO STRING COMBOBOX
    @FXML private TitledPane paneDoctor;
    @FXML private ComboBox<String> cmbDoctor;  // Changed from DoctorDTO to String
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
    @FXML private Label lblStatus;

    private final ObjectMapper mapper;
    private PatientDTO currentPatient;
    private AppointmentDTO currentAppointment;
    private List<DoctorDTO> doctorList;  // Keep reference to doctor list

    public AppointmentViewController() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @FXML
    public void initialize() {
        System.out.println("DEBUG: AppointmentViewController.initialize() called");

        // Setup gender combo
        cmbGender.getItems().addAll("MALE", "FEMALE", "OTHER");

        // Load doctors
        loadDoctors();

        // Button actions
        btnCheckPatient.setOnAction(e -> checkPatient());
        btnSavePatient.setOnAction(e -> savePatient());
        btnCreateAppointment.setOnAction(e -> createAppointment());
        btnSaveMedical.setOnAction(e -> saveMedicalRecord());
        btnReset.setOnAction(e -> resetForm());

        updateStatus("Enter patient Social ID to begin");
    }

    private boolean loadDoctors() {
        try {
            System.out.println("DEBUG: Loading doctors...");
            updateStatus("Loading doctors...");

            String json = ApiService.getDoctors();
            System.out.println("DEBUG: Received JSON length: " + json.length());

            doctorList = mapper.readValue(json, new TypeReference<List<DoctorDTO>>(){});
            System.out.println("DEBUG: Parsed " + doctorList.size() + " doctors");

            ObservableList<String> doctorNames = FXCollections.observableArrayList();
            for (DoctorDTO doctor : doctorList) {
                String displayName = doctor.getFullName() + " - " + doctor.getSpecialty();
                doctorNames.add(displayName);
                System.out.println("DEBUG: Added doctor: " + displayName);
            }

            cmbDoctor.setItems(doctorNames);

            System.out.println("DEBUG: Doctors loaded. ComboBox items: " + cmbDoctor.getItems().size());
            updateStatus("Loaded " + doctorList.size() + " doctors");
            return true;

        } catch (IOException e) {
            String errorMsg = "Failed to load doctors: " + e.getMessage();
            System.err.println("ERROR: " + errorMsg);
            e.printStackTrace();
            updateStatus(errorMsg);
            showError("Error", "Failed to load doctors", e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            String errorMsg = "Doctor loading interrupted";
            System.err.println("ERROR: " + errorMsg);
            updateStatus(errorMsg);
            showError("Error", "Failed to load doctors", errorMsg);
        } catch (Exception e) {
            String errorMsg = "Unexpected error loading doctors: " + e.getMessage();
            System.err.println("ERROR: " + errorMsg);
            e.printStackTrace();
            updateStatus(errorMsg);
            showError("Error", "Failed to load doctors", e.getMessage());
        }
        return false;
    }

    private boolean ensureDoctorsAvailable() {
        if (doctorList == null || doctorList.isEmpty() || cmbDoctor.getItems() == null || cmbDoctor.getItems().isEmpty()) {
            System.out.println("DEBUG: Doctor list empty, reloading...");
            return loadDoctors();
        }
        return true;
    }

    private DoctorDTO getSelectedDoctor() {
        String selectedName = cmbDoctor.getValue();
        if (selectedName == null || doctorList == null) {
            return null;
        }

        // Find the doctor by matching the display name
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

        try {
            System.out.println("DEBUG: Checking patient with social ID: " + socialId);
            updateStatus("Checking patient...");

            String json = ApiService.getPatientBySocialId(socialId);
            currentPatient = mapper.readValue(json, PatientDTO.class);

            System.out.println("DEBUG: Patient found: " + currentPatient.getFullName());

            // Patient found - populate fields
            txtFullName.setText(currentPatient.getFullName());
            dpDob.setValue(currentPatient.getDob());
            cmbGender.setValue(currentPatient.getGender());
            txtPhone.setText(currentPatient.getPhone());
            txtEmail.setText(currentPatient.getEmail());
            txtAddress.setText(currentPatient.getAddress());

            // Disable editing for existing patient
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
                // Patient not found - enable creation
                System.out.println("DEBUG: Patient not found, enabling creation");
                lblPatientStatus.setText("Patient not found. Please fill in details to create new patient.");
                lblPatientStatus.setStyle("-fx-text-fill: orange;");

                setPatientFieldsEditable(true);
                btnSavePatient.setDisable(false);

                updateStatus("Patient not found. Fill in details to create new patient.");
            } else {
                System.err.println("ERROR checking patient: " + e.getMessage());
                showError("Error", "Failed to check patient", e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("ERROR checking patient: " + e.getMessage());
            e.printStackTrace();
            showError("Error", "Failed to check patient", e.getMessage());
        }
    }

    private void savePatient() {
        if (!validatePatientFields()) {
            return;
        }

        try {
            System.out.println("DEBUG: Creating patient...");
            updateStatus("Creating patient...");

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

            System.out.println("DEBUG: Patient created: " + currentPatient.getFullName());

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
            System.err.println("ERROR creating patient: " + e.getMessage());
            e.printStackTrace();
            showError("Error", "Failed to create patient", e.getMessage());
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

        try {
            System.out.println("DEBUG: Creating appointment...");
            System.out.println("DEBUG: Patient ID: " + currentPatient.getId());
            System.out.println("DEBUG: Doctor ID: " + selectedDoctor.getId());

            updateStatus("Creating appointment...");

            AppointmentDTO appointment = new AppointmentDTO();
            appointment.setPatientId(currentPatient.getId());
            appointment.setDoctorId(selectedDoctor.getId());
            appointment.setDateTime(LocalDateTime.now());
            appointment.setStatus("SCHEDULED");
            appointment.setReason(txtReason.getText().trim());

            String jsonBody = mapper.writeValueAsString(appointment);
            System.out.println("DEBUG: Appointment JSON: " + jsonBody);

            String response = ApiService.createAppointment(jsonBody);

            currentAppointment = mapper.readValue(response, AppointmentDTO.class);

            System.out.println("DEBUG: Appointment created with ID: " + currentAppointment.getId());

            showInfo("Success", "Appointment created successfully!\nAppointment ID: " + currentAppointment.getId());

            // Disable appointment creation, enable medical record
            paneDoctor.setDisable(true);
            paneMedical.setDisable(false);

            updateStatus("Appointment created. Now add medical examination details.");

        } catch (Exception e) {
            System.err.println("ERROR creating appointment: " + e.getMessage());
            e.printStackTrace();
            showError("Error", "Failed to create appointment", e.getMessage());
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

        try {
            System.out.println("DEBUG: Saving medical record...");
            updateStatus("Saving medical record...");

            MedicalRecordDTO medicalRecord = new MedicalRecordDTO();
            medicalRecord.setAppointmentId(currentAppointment.getId());
            medicalRecord.setSymptoms(txtSymptoms.getText().trim());
            medicalRecord.setDiagnosis(txtDiagnosis.getText().trim());
            medicalRecord.setTreatment(txtTreatment.getText().trim());

            String jsonBody = mapper.writeValueAsString(medicalRecord);
            ApiService.createMedicalRecord(jsonBody);

            System.out.println("DEBUG: Medical record saved successfully");

            showInfo("Success", "Medical record saved and appointment completed!");

            updateStatus("Appointment completed successfully!");

            // Ask if user wants to create another appointment
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
            System.err.println("ERROR saving medical record: " + e.getMessage());
            e.printStackTrace();
            showError("Error", "Failed to save medical record", e.getMessage());
        }
    }

    private void resetForm() {
        System.out.println("DEBUG: Resetting form...");

        // Clear all fields
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

        // Reset state
        currentPatient = null;
        currentAppointment = null;

        // Enable/disable panes
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
        if (lblStatus != null) {
            lblStatus.setText(message);
        }
        System.out.println("STATUS: " + message);
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
}