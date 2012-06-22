package com.javafx.experiments.scenicview.connector;

public abstract class SVNodeImpl implements SVNode {

    boolean invalidForFilter;
    boolean showID;

    @Override public void setInvalidForFilter(final boolean invalid) {
        this.invalidForFilter = invalid;
    }

    @Override public boolean isInvalidForFilter() {
        return invalidForFilter;
    }

    @Override public void setShowId(final boolean showID) {
        this.showID = showID;
    }

}
