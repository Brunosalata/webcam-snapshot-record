module br.com.brunosalata.webcamsnapshotrecord {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.desktop;
    requires javafx.swing;
    requires webcam.capture;


    opens br.com.brunosalata.webcamsnapshotrecord to javafx.fxml;
    exports br.com.brunosalata.webcamsnapshotrecord;
}