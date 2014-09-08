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
package org.scenicview;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Duration;

class StatusBar extends HBox {
    private final Label windowTypeLabel;
    private final Label stageBoundsText;
    private final Label sceneSizeText;
    private final Label sceneMousePosText;
    private final Label nodeCountText;
    private final Label statusLabel = createValueLabel(null);
    private final List<Node> standardNodes = new ArrayList<>();
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
        
        clearTimeout = new Timeline(new KeyFrame(Duration.millis(timeout), event -> clearStatusText()));
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
