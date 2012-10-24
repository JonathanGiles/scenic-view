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
package com.javafx.experiments.scenicview;

import java.util.*;

import javafx.animation.*;
import javafx.event.*;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

class StatusBar extends HBox {
    private final Label windowTypeLabel;
    private final Label stageBoundsText;
    private final Label sceneSizeText;
    private final Label sceneMousePosText;
    private final Label nodeCountText;
    private final Label statusLabel = createValueLabel(null);
    private final List<Node> standardNodes = new ArrayList<Node>();
    private Timeline clearTimeout;

    StatusBar() {
        setId("main-statusbar");
        setSpacing(4);

        Tooltip tooltip = new Tooltip("Windows bounds in the screen");
        windowTypeLabel = createLabel("Stage:", tooltip);
        stageBoundsText = createValueLabel(tooltip);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.setPrefSize(100, 10);
        spacer.setMinSize(14, 12);
        spacer.setPrefSize(14, 12);
        spacer.setMaxSize(14, 12);
        getChildren().addAll(windowTypeLabel, stageBoundsText, spacer);

        tooltip = new Tooltip("Scene dimensions in pixels");
        Label label = createLabel("Scene:", tooltip);
        spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.setPrefSize(14, 12);
        spacer.setPrefSize(14, 12);
        spacer.setMaxSize(14, 12);
        sceneSizeText = createValueLabel(tooltip);
        getChildren().addAll(label, sceneSizeText, spacer);

        tooltip = new Tooltip("Mouse position on the main scene");
        label = createLabel("Mouse Position:", tooltip);
        spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.setPrefSize(18, 12);
        sceneMousePosText = createValueLabel(tooltip);

        getChildren().addAll(label, sceneMousePosText, spacer);

        tooltip = new Tooltip("Total number of nodes in the scene");
        label = createLabel("Total Node count:", tooltip);
        nodeCountText = createValueLabel(tooltip);
        getChildren().addAll(label, nodeCountText);
        standardNodes.addAll(getChildrenUnmodifiable());
    }

    private Label createValueLabel(final Tooltip tooltip) {
        final Label label = createLabel("", tooltip);
        label.getStyleClass().setAll("label", "value-label");
        return label;
    }

    private Label createLabel(final String value, final Tooltip tooltip) {
        final Label label = new Label(value);
        label.getStyleClass().add("value-name");
        if (tooltip != null) {
            label.setTooltip(tooltip);
        }
        return label;
    }

    void updateWindowDetails(final String targetWindow, final String bounds, final boolean focused) {
        if (targetWindow != null) {
            windowTypeLabel.setText(targetWindow + ":");
            if (!focused) {
                updateMousePosition("---");
            }
        }
        stageBoundsText.setText(bounds);
    }

    void updateNodeCount(final int count) {
        nodeCountText.setText(Integer.toString(count));
    }

    void updateSceneDetails(final String size, final int count) {
        sceneSizeText.setText(size);
        updateNodeCount(count);
    }

    void updateMousePosition(final String position) {
        sceneMousePosText.setText(position);
    }

    void setStatusText(final String text) {
        getChildren().clear();
        setAlignment(Pos.TOP_RIGHT);
        statusLabel.setText(text);
        getChildren().add(statusLabel);
        if (clearTimeout != null) {
            clearTimeout.stop();
            clearTimeout = null;
        }
    }

    public void setStatusText(final String text, final long timeout) {
        setStatusText(text);
        clearTimeout = TimelineBuilder.create().keyFrames(new KeyFrame(Duration.millis(timeout), new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent arg0) {
                clearStatusText();
            }
        })).build();
        clearTimeout.play();
    }

    void clearStatusText() {
        getChildren().clear();
        setAlignment(Pos.TOP_LEFT);
        getChildren().addAll(standardNodes);
        clearTimeout = null;
    }

    boolean hasStatus() {
        return getChildren().contains(statusLabel);
    }
}
