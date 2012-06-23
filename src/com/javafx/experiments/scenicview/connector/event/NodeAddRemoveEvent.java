package com.javafx.experiments.scenicview.connector.event;

import com.javafx.experiments.scenicview.connector.*;

public class NodeAddRemoveEvent extends AppEvent {

    /**
     * 
     */
    private static final long serialVersionUID = 3697198794332055904L;
    private final SVNode node;

    public NodeAddRemoveEvent(final SVEventType type, final StageID id, final SVNode node) {
        super(type, id);
        this.node = node;
    }

    public SVNode getNode() {
        return node;
    }

}
