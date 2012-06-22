package com.javafx.experiments.scenicview;

import static com.javafx.experiments.scenicview.DisplayUtils.getBranchCount;

import java.util.*;

import javafx.animation.*;
import javafx.event.*;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

public class StatusBar extends HBox {
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

    void updateNodeCount(final Scene targetScene) {
        nodeCountText.setText(Integer.toString(targetScene != null ? getBranchCount(targetScene.getRoot()) : 0));
    }

    void updateSceneDetails(final Scene targetScene) {
        sceneSizeText.setText(targetScene != null ? targetScene.getWidth() + " x " + targetScene.getHeight() : "");
        updateNodeCount(targetScene);
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
}
