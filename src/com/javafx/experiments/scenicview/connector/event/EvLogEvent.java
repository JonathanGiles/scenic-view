package com.javafx.experiments.scenicview.connector.event;

import com.javafx.experiments.scenicview.connector.StageID;
import com.javafx.experiments.scenicview.connector.node.SVNode;

public class EvLogEvent extends AppEvent {

    public static final String PROPERTY_CHANGED = "PROPERTY_CHANGED";
    public static final String OTHER_EVENTS = "OTHER_EVENTS";
    public static final String NODE_REMOVED = "NODE_REMOVED";
    public static final String NODE_ADDED = "NODE_ADDED";

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
