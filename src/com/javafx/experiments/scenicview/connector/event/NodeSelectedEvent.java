package com.javafx.experiments.scenicview.connector.event;

import com.javafx.experiments.scenicview.connector.*;

public class NodeSelectedEvent extends AppEvent {

    /**
     * 
     */
    private static final long serialVersionUID = 6259324216941075314L;
    private final SVNode node;
    
    public NodeSelectedEvent(final StageID id, final SVNode node) {
        super(SVEventType.NODE_SELECTED, id);
        this.node = node;
    }

    public SVNode getNode() {
        return node;
    }

}
