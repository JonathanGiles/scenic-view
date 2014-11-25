package org.fxmisc.cssfx.api;

import java.nio.file.Path;

import javafx.scene.Parent;
import javafx.scene.Scene;

public class MonitoredStylesheet {
    private Scene scene;
    private Parent parent;
    private String originalURI;
    private Path source;
    
    public Scene getScene() {
        return scene;
    }
    public void setScene(Scene scene) {
        this.scene = scene;
    }
    public Parent getParent() {
        return parent;
    }
    public void setParent(Parent p) {
        this.parent = p;
    }
    public String getOriginalURI() {
        return originalURI;
    }
    public void setOriginalURI(String originalURI) {
        this.originalURI = originalURI;
    }
    public Path getSource() {
        return source;
    }
    public void setSource(Path source) {
        this.source = source;
    }
    
    @Override
    public String toString() {
        if (source == null) {
            return String.format("%s in [%s] is not mapped", originalURI, (parent==null)?scene:parent);
        }
        return String.format("%s in [%s] is mapped to %s", originalURI, (parent==null)?scene:parent, source);
    }
}
