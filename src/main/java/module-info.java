module org.scenicview.scenicview {
    requires transitive javafx.fxml;
    requires transitive javafx.web;
    requires transitive javafx.swing;

    requires java.instrument;
    requires java.rmi;
    requires java.logging;
    requires jdk.attach;
    requires java.desktop;

    opens org.scenicview.view.cssfx to javafx.fxml;
    opens org.scenicview.view.threedom to javafx.fxml;
    opens org.fxconnector.remote to java.instrument, java.rmi;

    exports org.scenicview.view.cssfx to javafx.fxml;
    exports org.scenicview.view.threedom to javafx.fxml;
    exports org.fxconnector.remote to java.instrument;

    exports org.fxconnector;
    exports org.scenicview;
}