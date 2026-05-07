module com.example.traffic.smarttrafficsystem {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.traffic to javafx.fxml;

    exports com.example.traffic;
    exports com.example.traffic.ai;
    exports com.example.traffic.graph;
    exports com.example.traffic.model;
    exports com.example.traffic.simulation;
    exports com.example.traffic.ui;
    exports com.example.traffic.ui.models;
}