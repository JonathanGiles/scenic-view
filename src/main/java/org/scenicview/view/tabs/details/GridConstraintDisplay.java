/*
 * Scenic View, 
 * Copyright (C) 2012 Jonathan Giles, Ander Ruiz, Amy Fowler 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.scenicview.view.tabs.details;

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