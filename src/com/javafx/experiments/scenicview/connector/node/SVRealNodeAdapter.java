package com.javafx.experiments.scenicview.connector.node;

import java.util.*;

import javafx.scene.*;
import javafx.scene.control.*;

import com.javafx.experiments.scenicview.DisplayUtils;

public class SVRealNodeAdapter extends SVNodeImpl implements SVNode {

    private final Node node;
    private final boolean collapseControls;
    private final boolean collapseContentControls;

    public SVRealNodeAdapter(final Node node) {
        this(node, true, true);
    }

    public SVRealNodeAdapter(final Node node, final boolean collapseControls, final boolean collapseContentControls) {
        super(DisplayUtils.nodeClass(node));
        this.node = node;
        this.collapseControls = collapseControls;
        this.collapseContentControls = collapseContentControls;
        boolean mustBeExpanded = !(node instanceof Control) || !collapseControls;
        if (!mustBeExpanded && !collapseContentControls) {
            mustBeExpanded = node instanceof TabPane || node instanceof SplitPane || node instanceof ScrollPane || node instanceof Accordion || node instanceof TitledPane;
        }
        setExpanded(mustBeExpanded);
    }

    @Override public String getId() {
        return node.getId();
    }

    @Override public SVNode getParent() {
        /**
         * This should be improved
         */
        return new SVRealNodeAdapter(node.getParent(), collapseControls, collapseContentControls);
    }

    @Override public List<SVNode> getChildren() {
        /**
         * This should be improved
         */
        if (node instanceof Parent) {
            final List<SVNode> nodes = new ArrayList<SVNode>();
            final List<Node> realNodes = ((Parent) node).getChildrenUnmodifiable();
            for (int i = 0; i < realNodes.size(); i++) {
                nodes.add(new SVRealNodeAdapter(realNodes.get(i), collapseControls, collapseContentControls));
            }
            return nodes;
        }
        return Collections.emptyList();
    }

    /**
     * This must be removed in the future
     */
    @Override public boolean equals(final Object node) {
        if (node instanceof SVNode) {
            return equals((SVNode) node);
        } else {
            return this.node == node;
        }
    }

    @Override public boolean equals(final SVNode node) {
        if (node instanceof SVRealNodeAdapter) {
            return node.getImpl() == this.node;
        } else {
            return node != null && getNodeId() == node.getNodeId();
        }
    }

    @Override public Node getImpl() {
        return node;
    }

    @Override public int getNodeId() {
        return node.hashCode();
    }

    @Override public boolean isVisible() {
        return isNodeVisible(node);
    }

    public static boolean isNodeVisible(final Node node) {
        if (node == null) {
            return true;
        } else {
            return node.isVisible() && isNodeVisible(node.getParent());
        }
    }

    @Override public boolean isMouseTransparent() {
        return isMouseTransparent(node);
    }

    public static boolean isMouseTransparent(final Node node) {
        if (node == null) {
            return false;
        } else {
            return node.isMouseTransparent() || isMouseTransparent(node.getParent());
        }
    }

    @Override public boolean isFocused() {
        return node.isFocused();
    }

    @Override public String toString() {
        return DisplayUtils.nodeDetail(this, showID);
    }

    @Override public String getExtendedId() {
        return DisplayUtils.nodeDetail(this, true);
    }

    @Override public boolean isRealNode() {
        return true;
    }
}
