package com.javafx.experiments.scenicview.connector.event;

import com.javafx.experiments.scenicview.connector.StageID;

public class SceneDetailsEvent extends NodeCountEvent {

    /**
     * 
     */
    private static final long serialVersionUID = -3029484692774441455L;
    private final String size;
    
    public SceneDetailsEvent(final StageID id, final int nodeCount, final String size) {
        super(SVEventType.SCENE_DETAILS, id, nodeCount);
        this.size = size;
    }

    public String getSize() {
        return size;
    }

}
