package com.javafx.experiments.scenicview.connector.node;

import javafx.scene.image.Image;

public abstract class SVNodeImpl implements SVNode {

    boolean invalidForFilter;
    boolean showID;
    boolean expanded;
    protected final String nodeClass;

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

}
