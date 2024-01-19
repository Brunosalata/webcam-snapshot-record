module br.com.brunosalata.webcamsnapshotrecord {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.desktop;
    requires javafx.swing;
    requires webcam.capture;
    requires xuggle.xuggler;


    opens br.com.brunosalata.webcamsnapshotrecord to javafx.fxml;
    exports br.com.brunosalata.webcamsnapshotrecord;
    exports br.com.brunosalata.webcamsnapshotrecord.videoRecord;
    opens br.com.brunosalata.webcamsnapshotrecord.videoRecord to javafx.fxml;
}