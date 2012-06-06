package com.javafx.experiments.scenicview;

import javafx.scene.Node;

public class DummyNodeInfo implements NodeInfo {

    private final String name;

    DummyNodeInfo(final String name) {
        this.name = name;
    }

    @Override public boolean isVisible() {
        return true;
    }

    @Override public boolean isMouseTransparent() {
        return false;
    }

    @Override public Node getNode() {
        return null;
    }

    @Override public boolean isInvalidForFilter() {
        return false;
    }

    @Override public String toString() {
        return name;
    }

}
