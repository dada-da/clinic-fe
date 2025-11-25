package com.clinic.ui.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Region;

import java.io.IOException;

public class AppointmentViewController {

    @FXML private TabPane tabPaneAppointments;
    @FXML private Button btnAddAppointment;
    @FXML private Label lblStatus;
    @FXML private ProgressBar progressBar;

    private int tabCounter = 1;

    @FXML
    public void initialize() {
        if (progressBar != null) {
            progressBar.setVisible(false);
            progressBar.setProgress(0);
        }

        if (btnAddAppointment != null) {
            btnAddAppointment.setOnAction(e -> addAppointmentTab());
        }

        addAppointmentTab();
    }

    private void addAppointmentTab() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/appointment_tab.fxml"));
            Region content = loader.load();
            AppointmentTabController tabController = loader.getController();

            String tabTitle = "Lịch hẹn " + tabCounter++;
            Tab tab = new Tab(tabTitle);
            tab.setContent(content);
            tab.setClosable(true);

            tabController.configure(
                    tabTitle,
                    this::updateStatus,
                    new ViewProgressHandle(tabTitle)
            );

            tab.setOnClosed(event -> {
                if (tabPaneAppointments.getTabs().isEmpty()) {
                    Platform.runLater(this::addAppointmentTab);
                }
            });

            tabPaneAppointments.getTabs().add(tab);
            tabPaneAppointments.getSelectionModel().select(tab);

        } catch (IOException e) {
            updateStatus("Không mở được tab lịch hẹn: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateStatus(String message) {
        if (lblStatus != null) {
            lblStatus.setText(message);
        }
        System.out.println("STATUS: " + message);
    }

    private class ViewProgressHandle implements AppointmentTabController.ProgressHandle {
        private final String tabTitle;

        private ViewProgressHandle(String tabTitle) {
            this.tabTitle = tabTitle;
        }

        @Override
        public void showIndeterminate(String message) {
            if (progressBar == null) {
                return;
            }
            Platform.runLater(() -> {
                progressBar.progressProperty().unbind();
                progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                progressBar.setVisible(true);
                updateStatus("[" + tabTitle + "] " + message);
            });
        }

        @Override
        public void hide() {
            if (progressBar == null) {
                return;
            }
            Platform.runLater(() -> {
                progressBar.progressProperty().unbind();
                progressBar.setVisible(false);
                progressBar.setProgress(0);
            });
        }
    }
}

