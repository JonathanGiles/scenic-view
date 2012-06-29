package com.javafx.experiments.scenicview.connector;

import javafx.collections.ObservableList;
import javafx.scene.Node;

/**
 * Special interface for autotesting ScenicView
 * 
 * @author Ander
 * 
 */
public interface CParent {
    public ObservableList<Node> getChildren();
}
