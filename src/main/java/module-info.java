module org.salemshah.audiorecorder {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires java.net.http;
    requires org.json;
    requires javafx.swing;

    opens org.salemshah.audiorecorder to javafx.fxml;
    exports org.salemshah.audiorecorder;
}