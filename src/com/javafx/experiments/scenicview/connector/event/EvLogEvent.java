package com.javafx.experiments.scenicview.connector.event;

import com.javafx.experiments.scenicview.connector.*;

public class EvLogEvent extends AppEvent {

    /**
     * 
     */
    private static final long serialVersionUID = -4130339506376073468L;
    private final SVNode source;
    private final String eventType;
    private final String eventValue;

    public EvLogEvent(final StageID id, final SVNode source, final String eventType, final String eventValue) {
        super(SVEventType.EVENT_LOG, id);
        this.source = source;
        this.eventType = eventType;
        this.eventValue = eventValue;
    }

    public SVNode getSource() {
        return source;
    }

    public String getEventType() {
        return eventType;
    }

    public String getEventValue() {
        return eventValue;
    }

}
