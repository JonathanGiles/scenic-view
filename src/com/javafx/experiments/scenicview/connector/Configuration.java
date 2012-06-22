package com.javafx.experiments.scenicview.connector;

import java.io.Serializable;

public class Configuration implements Serializable {

    private boolean showBounds;

    private boolean showBaseline;

    private boolean eventLogEnabled;

    public boolean isShowBounds() {
        return showBounds;
    }

    public void setShowBounds(final boolean showBounds) {
        this.showBounds = showBounds;
    }

    public boolean isShowBaseline() {
        return showBaseline;
    }

    public void setShowBaseline(final boolean showBaseline) {
        this.showBaseline = showBaseline;
    }

    public boolean isEventLogEnabled() {
        return eventLogEnabled;
    }

    public void setEventLogEnabled(final boolean eventLogEnabled) {
        this.eventLogEnabled = eventLogEnabled;
    }

}
