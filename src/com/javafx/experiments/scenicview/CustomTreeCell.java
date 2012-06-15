package com.javafx.experiments.scenicview;

import javafx.scene.control.TreeCell;
import javafx.scene.paint.Color;

class CustomTreeCell extends TreeCell<NodeInfo> {
    @Override public void updateItem(final NodeInfo item, final boolean empty) {
        super.updateItem(item, empty);
        
        if (getTreeItem() != null) {
            setGraphic(getTreeItem().getGraphic());
        }

        if (item != null) {
            setText(item.toString());
            setOpacity(1);
        }

        if (item != null && (!item.isVisible() || item.isInvalidForFilter())) {
            setOpacity(0.3);
        }

        if (item != null && item.getNode() != null && item.getNode().isFocused()) {
            setTextFill(Color.RED);
        }
    }
}