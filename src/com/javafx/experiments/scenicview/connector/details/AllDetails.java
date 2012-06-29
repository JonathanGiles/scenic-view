package com.javafx.experiments.scenicview.connector.details;

import javafx.scene.Node;

public class AllDetails {

    private Node target;

    DetailPaneInfo details[] = { new NodeDetailPaneInfo(), new ParentDetailPaneInfo(), new RegionDetailPaneInfo(), new GridPaneDetailPaneInfo(), new ControlDetailPaneInfo(), new TextDetailPaneInfo(), new LabeledDetailPaneInfo(), new FullPropertiesDetailPaneInfo() };

    public AllDetails() {
        // TODO Auto-generated constructor stub
    }

    public void setTarget(final Node value) {
        if (target == value)
            return;

        target = value;
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
            }
        }
    }
}
