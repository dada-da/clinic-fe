module clinic.fe {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;

    requires java.net.http;

    opens com.clinic.ui to javafx.fxml;
    opens com.clinic.ui.controller to javafx.fxml;
    opens com.clinic.ui.model to com.fasterxml.jackson.databind, javafx.base;

    exports com.clinic.ui;
}
