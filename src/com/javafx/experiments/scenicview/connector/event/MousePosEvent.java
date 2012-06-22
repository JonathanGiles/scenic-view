package com.javafx.experiments.scenicview.connector.event;

import com.javafx.experiments.scenicview.connector.StageID;

public class MousePosEvent extends AppEvent {

    /**
     * 
     */
    private static final long serialVersionUID = -3472298445464561790L;
    private final String position;

    public MousePosEvent(final StageID id, final String position) {
        super(SVEventType.MOUSE_POSITION, id);
        this.position = position;
    }

    public String getPosition() {
        return position;
    }

}
