module com.example.traffic.smarttrafficsystem {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.traffic to javafx.fxml;
    exports com.example.traffic;
}