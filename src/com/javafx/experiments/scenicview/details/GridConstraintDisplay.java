package com.javafx.experiments.scenicview.details;

import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

class GridConstraintDisplay extends GridPane {
    protected Node[] labels = { new Label("index"), new Label("min"), new Label("pref"), new Label("max"), new Label("percent"), new Label("grow"), new Label("alignment"), new Label("fill") };

    public GridConstraintDisplay() {
        getStyleClass().add("gridpane-constraint-display");
        setHgap(4);
        setVgap(1);
        setSnapToPixel(true);
        final ColumnConstraints cc = new ColumnConstraints();
        cc.setHalignment(HPos.CENTER);
        for (int i = 0; i < 8; i++) {
            getColumnConstraints().add(cc);
        }
    }

    protected void setConstraints(final boolean hasConstraints) {
        final Node children[] = getChildren().toArray(new Node[0]);
        for (final Node child : children) {
            GridPane.clearConstraints(child);
            getChildren().remove(child);
        }
        if (!hasConstraints) {
            addRow(0, new Label("no constraints set"));
        } else {
            addRow(0, labels);
        }
    }

    protected void addObject(final Object v, final int rowIndex, final int colIndex) {
        add(new Text(v != null ? v.toString() : "-"), colIndex, rowIndex);
    }

}