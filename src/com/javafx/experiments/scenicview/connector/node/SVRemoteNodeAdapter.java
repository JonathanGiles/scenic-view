package com.javafx.experiments.scenicview.connector.node;

import java.io.Serializable;
import java.util.*;

import javafx.scene.*;
import javafx.scene.control.*;

import com.javafx.experiments.scenicview.connector.ConnectorUtils;

public class SVRemoteNodeAdapter extends SVNodeImpl implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -5972848763525174505L;
    private String id;
    private int nodeId;
    private boolean visible;
    private boolean mouseTransparent;
    private boolean focused;
    private List<SVNode> nodes;
    private SVRemoteNodeAdapter parent;

    public SVRemoteNodeAdapter() {
        super();
    }

    public SVRemoteNodeAdapter(final Node node, final boolean collapseControls, final boolean collapseContentControls) {
        this(node, collapseControls, collapseContentControls, true, null);
    }

    public SVRemoteNodeAdapter(final Node node, final boolean collapseControls, final boolean collapseContentControls, final boolean fillChildren, final SVRemoteNodeAdapter parent) {
        super(ConnectorUtils.nodeClass(node), node.getClass().getName());
        boolean mustBeExpanded = !(node instanceof Control) || !collapseControls;
        if (!mustBeExpanded && !collapseContentControls) {
            mustBeExpanded = node instanceof TabPane || node instanceof SplitPane || node instanceof ScrollPane || node instanceof Accordion || node instanceof TitledPane;
        }
        setExpanded(mustBeExpanded);
        this.id = node.getId();
        this.nodeId = ConnectorUtils.getNodeUniqueID(node);
        this.visible = isNodeVisible(node);
        this.mouseTransparent = isMouseTransparent(node);
        this.focused = node.isFocused();
        /**
         * This should be improved
         */
        if (node instanceof Parent && fillChildren) {
            nodes = new ArrayList<SVNode>();
            final List<Node> realNodes = ((Parent) node).getChildrenUnmodifiable();
            for (int i = 0; i < realNodes.size(); i++) {
                nodes.add(new SVRemoteNodeAdapter(realNodes.get(i), collapseControls, collapseContentControls, true, this));
            }
        } else if (fillChildren) {
            nodes = Collections.emptyList();
        }
        if (node.getParent() != null && parent == null) {
            this.parent = new SVRemoteNodeAdapter(node.getParent(), collapseControls, collapseContentControls, false, null);
        } else if (parent != null) {
            this.parent = parent;
        }
    }

    @Override public String getId() {
        return id;
    }

    @Override public String getExtendedId() {
        return ConnectorUtils.nodeDetail(this, true);
    }

    @Override public SVNode getParent() {
        return parent;
    }

    @Override public List<SVNode> getChildren() {
        return nodes;
    }

    @Override public boolean equals(final SVNode node) {
        if (node instanceof SVDummyNode) {
            return false;
        }
        return node != null && node.getNodeId() == getNodeId();
    }

    /**
     * This must be removed in the future
     */
    @Override public boolean equals(final Object node) {
        if (node instanceof SVNode) {
            return equals((SVNode) node);
        } else if (node instanceof Node) {
            return getNodeId() == ConnectorUtils.getNodeUniqueID((Node) node);
        }
        return false;
    }

    @Override @Deprecated public Node getImpl() {
        return null;
    }

    @Override public int getNodeId() {
        return nodeId;
    }

    @Override public boolean isVisible() {
        return visible;
    }

    @Override public boolean isMouseTransparent() {
        return mouseTransparent;
    }

    @Override public boolean isFocused() {
        // TODO Auto-generated method stub
        return focused;
    }

    @Override public boolean isRealNode() {
        return true;
    }

    @Override public String toString() {
        return ConnectorUtils.nodeDetail(this, showID);
    }

    @Override public int hashCode() {
        return nodeId;
    }

}
