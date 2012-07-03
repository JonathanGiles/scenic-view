package com.javafx.experiments.scenicview.connector.event;

import java.io.Serializable;

import com.javafx.experiments.scenicview.connector.StageID;

public class AppEvent implements Serializable {

    public enum SVEventType {
        EVENT_LOG, MOUSE_POSITION, WINDOW_DETAILS, NODE_SELECTED, NODE_ADDED, NODE_REMOVED, NODE_COUNT, SCENE_DETAILS, ROOT_UPDATED, DETAILS, DETAIL_UPDATED, ANIMATIONS_UPDATED
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
