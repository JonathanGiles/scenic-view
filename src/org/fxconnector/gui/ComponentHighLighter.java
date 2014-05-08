/*
 * Copyright (c) 2012 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.fxconnector.gui;

import org.fxconnector.*;
import org.fxconnector.node.SVNode;

import org.fxconnector.StageController;
import org.fxconnector.ConnectorUtils;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.control.TitledPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;

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
