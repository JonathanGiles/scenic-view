package com.javafx.experiments.scenicview.connector.event;

import com.javafx.experiments.scenicview.connector.StageID;

public class NodeCountEvent extends AppEvent {

    /**
     * 
     */
    private static final long serialVersionUID = 1192149233377886461L;
    private final int nodeCount;

    public NodeCountEvent(final StageID id, final int nodeCount) {
        this(SVEventType.NODE_COUNT, id, nodeCount);
    }
    

    protected NodeCountEvent(final SVEventType type, final StageID id, final int nodeCount) {
        super(type, id);
        this.nodeCount = nodeCount;
    }

    public int getNodeCount() {
        return nodeCount;
    }

}
