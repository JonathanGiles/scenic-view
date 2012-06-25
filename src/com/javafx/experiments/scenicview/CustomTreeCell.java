package com.javafx.experiments.scenicview;

import javafx.scene.control.TreeCell;
import javafx.scene.paint.Color;

import com.javafx.experiments.scenicview.connector.node.SVNode;

class CustomTreeCell extends TreeCell<SVNode> {
    @Override public void updateItem(final SVNode item, final boolean empty) {
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

        if (item != null && item.isFocused()) {
            setTextFill(Color.RED);
        }
    }
}