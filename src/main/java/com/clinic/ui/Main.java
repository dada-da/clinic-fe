package com.clinic.ui;

import com.clinic.ui.controller.AppointmentListViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);

        Tab tabList = new Tab("Lịch Hẹn Hôm Nay");
        FXMLLoader todayListLoader = new FXMLLoader(getClass().getResource("/views/appointment_list_view.fxml"));
        tabList.setContent(todayListLoader.load());
        tabList.setClosable(false);
        AppointmentListViewController todayListController = todayListLoader.getController();
        todayListController.setFilter(AppointmentListViewController.AppointmentFilter.TODAY);
        todayListController.configure();
        todayListController.setTabPane(tabPane);

        Tab tabPatientList = new Tab("Danh Sách Bệnh Nhân");
        FXMLLoader patientListLoader = new FXMLLoader(getClass().getResource("/views/patient_view.fxml"));
        tabPatientList.setContent(patientListLoader.load());

        Tab tabListAll = new Tab("Danh sach Lịch Hẹn");
        FXMLLoader listLoader = new FXMLLoader(getClass().getResource("/views/appointment_list_view.fxml"));
        tabListAll.setContent(listLoader.load());
        tabListAll.setClosable(false);
        AppointmentListViewController listAllController = listLoader.getController();
        listAllController.setTabPane(tabPane);
        listAllController.setFilter(AppointmentListViewController.AppointmentFilter.ALL);
        listAllController.configure();

        tabPane.getTabs().add(tabList);
        tabPane.getTabs().add(tabListAll);
        tabPane.getTabs().add(tabPatientList);

        Scene scene = new Scene(tabPane, 1200, 800);

        primaryStage.setTitle("Hệ Thống Quản Lý Phòng Khám");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
