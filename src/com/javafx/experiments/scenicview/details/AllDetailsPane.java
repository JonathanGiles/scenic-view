/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.javafx.experiments.scenicview.details;

import com.javafx.experiments.scenicview.*;

import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * 
 * @author aim
 */
public class AllDetailsPane extends VBox {
    DetailPane detailPanes[] = { new NodeDetailPane(), new ParentDetailPane(), new RegionDetailPane(), new GridPaneDetailPane(), new ControlDetailPane(), new TextDetailPane(), new LabeledDetailPane(), new FullPropertiesDetailPane() };

    public AllDetailsPane() {
        getStyleClass().add("all-details-pane");
        getChildren().addAll(detailPanes);
        setFillWidth(true);
    }

    private Node target;

    public void setTarget(final Node value) {
        if (target == value)
            return;

        target = value;
        updatePanes();
    }

    public Node getTarget() {
        return target;
    }

    public void setShowDefaultProperties(final boolean show) {
        for (final DetailPane details : detailPanes) {
            details.setShowDefaultProperties(show);
        }
        updatePanes();
    }

    private void updatePanes() {
        for (final DetailPane details : detailPanes) {
            if (details.targetMatches(getTarget())) {
                details.setTarget(getTarget());

                boolean detailVisible = false;
                for (final Node gridChild : details.gridpane.getChildren()) {
                    detailVisible = gridChild.isVisible();
                    if (detailVisible)
                        break;
                }
                details.setExpanded(detailVisible);
                details.setManaged(detailVisible);
                details.setVisible(detailVisible);
            } else {
                details.setExpanded(false);
                details.setManaged(false);
                details.setVisible(false);
            }
        }
    }

    public void filterProperties(final String text) {
        for (int i = 0; i < detailPanes.length; i++) {
            detailPanes[i].filterProperties(text);
        }
    }

    public void setShowCSSProperties(final boolean show) {
        for (final DetailPane details : detailPanes) {
            details.setShowCSSProperties(show);
        }

    }
}
