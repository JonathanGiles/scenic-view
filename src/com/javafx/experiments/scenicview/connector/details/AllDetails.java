package com.javafx.experiments.scenicview.connector.details;

import javafx.scene.Node;

public class AllDetails {

    private Node target;

    DetailPaneInfo[] details = new DetailPaneInfo[] {};

    public AllDetails() {
        // TODO Auto-generated constructor stub
    }

    public void setTarget(final Node value) {
        if (target == value)
            return;

        target = value;
        updatePanes();
    }

    public void setShowDefaultProperties(final boolean show) {
        for (final DetailPaneInfo detail : details) {
            detail.setShowDefaultProperties(show);
        }
        updatePanes();
    }

    public void setShowCSSProperties(final boolean show) {
        for (final DetailPaneInfo detail : details) {
            detail.setShowCSSProperties(show);
        }

    }

    private void updatePanes() {
        for (final DetailPaneInfo detail : details) {
            if (detail.targetMatches(target)) {
                detail.setTarget(target);

                // boolean detailVisible = false;
                // for (final Node gridChild : details.gridpane.getChildren()) {
                // detailVisible = gridChild.isVisible();
                // if (detailVisible)
                // break;
                // }
            }
        }
    }
}
