package com.javafx.experiments.scenicview.connector.event;

import java.io.Serializable;

import com.javafx.experiments.scenicview.connector.StageID;

public class AppEvent implements Serializable {

    public enum SVEventType {
        EVENT_LOG, MOUSE_POSITION, WINDOW_DETAILS
    }

    /**
     * 
     */
    private static final long serialVersionUID = -2556951288718105815L;

    private final SVEventType type;
    private final StageID stageID;

    public AppEvent(final SVEventType type, final StageID id) {
        this.type = type;
        this.stageID = id;
    }

    public SVEventType getType() {
        return type;
    }

    public StageID getStageID() {
        return stageID;
    }

}
