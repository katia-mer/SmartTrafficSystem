module com.example.traffic.smarttrafficsystem {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.traffic.smarttrafficsystem to javafx.fxml;
    exports com.example.traffic.smarttrafficsystem;
}