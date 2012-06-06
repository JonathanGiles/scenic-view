package com.javafx.experiments.scenicview;

import javafx.scene.Node;

public interface NodeInfo {
    public boolean isVisible();

    public boolean isMouseTransparent();

    public Node getNode();

    public boolean isInvalidForFilter();
}
