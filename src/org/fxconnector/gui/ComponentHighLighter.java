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
package org.fxconnector.gui;

import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.control.TitledPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;

import org.fxconnector.ConnectorUtils;
import org.fxconnector.StageController;
import org.fxconnector.node.SVNode;

public class ComponentHighLighter extends Group {

    public ComponentHighLighter(final SVNode node, final double width, final double height, final Bounds bounds) {
        if (width == -1) {
            final Rectangle rect = new Rectangle();
            rect.setFill(Color.TRANSPARENT);
            rect.setStroke(Color.ORANGE);
            rect.setMouseTransparent(true);
            rect.setLayoutX(bounds.getMinX());
            rect.setLayoutY(bounds.getMinY());
            rect.setStrokeWidth(3);
            rect.setWidth(bounds.getMaxX() - bounds.getMinX());
            rect.setHeight(bounds.getMaxY() - bounds.getMinY());
            getChildren().add(rect);
        } else {
            final Rectangle base = new Rectangle(width, height);
            final Rectangle rect = new Rectangle();
            rect.setLayoutX(bounds.getMinX());
            rect.setLayoutY(bounds.getMinY());
            rect.setStrokeWidth(3);
            rect.setWidth(bounds.getMaxX() - bounds.getMinX());
            rect.setHeight(bounds.getMaxY() - bounds.getMinY());
            final Shape shape = Shape.subtract(base, rect);
            shape.setMouseTransparent(false);
            shape.setFill(Color.BLACK);
            shape.setOpacity(0.7);
            getChildren().add(shape);
            final TitledPane pane = new TitledPane();
            pane.setCollapsible(false);
            pane.setText(node.getExtendedId());
            pane.setPrefHeight(60);
            pane.setPrefWidth(100);
            final Text label = new Text();
            label.setText("x:" + ConnectorUtils.format(bounds.getMinX()) + " y:" + ConnectorUtils.format(bounds.getMinY()) + "\nw:" + ConnectorUtils.format(rect.getWidth()) + " h:" + ConnectorUtils.format(rect.getHeight()));
            pane.setContent(label);
            pane.setLayoutX(bounds.getMinX() + (rect.getWidth() / 2) - (pane.getPrefWidth() / 2));
            pane.setFocusTraversable(false);

            if (pane.getLayoutX() < 0) {
                pane.setLayoutX(0);
            } else if (pane.getLayoutX() + pane.getPrefWidth() >= width) {
                pane.setLayoutX(width - pane.getPrefWidth());
            }
            if (bounds.getMinY() - pane.getPrefHeight() >= 0) {
                pane.setLayoutY(bounds.getMinY() - pane.getPrefHeight());
            } else if (bounds.getMinY() + rect.getHeight() + pane.getPrefHeight() <= height) {
                pane.setLayoutY(bounds.getMinY() + rect.getHeight());
            } else {
                pane.setLayoutY(0);
            }
            getChildren().add(pane);
        }
        setManaged(false);
        setId(StageController.FX_CONNECTOR_BASE_ID + "componentHighLighter");
    }

}
