package com.javafx.experiments.scenicview.connector.node;

import java.io.Serializable;

import javafx.scene.Node;
import javafx.scene.image.Image;

public abstract class SVNodeImpl implements SVNode, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3301868461718461962L;
    boolean invalidForFilter;
    boolean showID;
    boolean expanded;
    protected String nodeClass;

    protected SVNodeImpl() {

    }

    protected SVNodeImpl(final String nodeClass) {
        this.nodeClass = nodeClass;
    }

    @Override public void setInvalidForFilter(final boolean invalid) {
        this.invalidForFilter = invalid;
    }

    @Override public boolean isInvalidForFilter() {
        return invalidForFilter;
    }

    @Override public void setShowId(final boolean showID) {
        this.showID = showID;
    }

    @Override public boolean isExpanded() {
        return this.expanded;
    }

    @Override public void setExpanded(final boolean expanded) {
        this.expanded = expanded;
    }

    @Override public Image getIcon() {
        return null;
    }

    @Override public String getNodeClass() {
        return nodeClass;
    }

    public static boolean isNodeVisible(final Node node) {
        if (node == null) {
            return true;
        } else {
            return node.isVisible() && isNodeVisible(node.getParent());
        }
    }

    public static boolean isMouseTransparent(final Node node) {
        if (node == null) {
            return false;
        } else {
            return node.isMouseTransparent() || isMouseTransparent(node.getParent());
        }
    }

}
