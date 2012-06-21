package com.javafx.experiments.scenicview.connector;

import java.io.Serializable;

public class AppEvent implements Serializable {

    public enum SVEventType { EVENT_LOG }
    
    /**
     * 
     */
    private static final long serialVersionUID = -2556951288718105815L;
    
    private final SVEventType type;

    public AppEvent(final SVEventType type) {
        this.type = type;
    }

    public SVEventType getType() {
        return type;
    }
    
}
