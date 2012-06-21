package com.javafx.experiments.scenicview.connector;

public class EvLogEvent extends AppEvent {

    private final SVNode source;
    private final String eventType;
    private final String eventValue;
    
    public EvLogEvent(final SVNode source, final String eventType, final String eventValue) {
        super(SVEventType.EVENT_LOG);
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
