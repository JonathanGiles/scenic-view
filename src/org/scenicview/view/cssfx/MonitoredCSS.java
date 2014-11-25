package org.scenicview.view.cssfx;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MonitoredCSS {
    private StringProperty css = new SimpleStringProperty();
    private StringProperty mappedBy = new SimpleStringProperty();
    
    public MonitoredCSS(String css) {
        this.css.set(css);
    }
    
    public ReadOnlyStringProperty css() {
        return css;
    }
    
    public StringProperty mappedBy() {
        return mappedBy;
    }

    public String getCSS() {
        return css().get();
    }

    public String getMappedBy() {
        return mappedBy().get();
    }
}
