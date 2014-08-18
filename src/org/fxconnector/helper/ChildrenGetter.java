package org.fxconnector.helper;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.SubScene;

public class ChildrenGetter {

    private ChildrenGetter() { }
    
    public static ObservableList<Node> getChildren(Node node) {
        if (node == null) return FXCollections.emptyObservableList();
        
        if (node instanceof Parent) {
            return ((Parent)node).getChildrenUnmodifiable();
        } else if (node instanceof SubScene) {
            return ((SubScene)node).getRoot().getChildrenUnmodifiable();
        }
        
        return FXCollections.emptyObservableList();
    }
}
