package com.javafx.experiments.scenicview.connector;

public abstract class SVNodeImpl implements SVNode {

    boolean invalidForFilter;
    boolean showID;
    boolean expanded;

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

}
