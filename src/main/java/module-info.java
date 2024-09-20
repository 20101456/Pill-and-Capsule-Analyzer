module com.jercode.ca1_pill_and_capsule_analyser {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.jercode.ca1_pill_and_capsule_analyser to javafx.fxml;
    exports com.jercode.ca1_pill_and_capsule_analyser;
}