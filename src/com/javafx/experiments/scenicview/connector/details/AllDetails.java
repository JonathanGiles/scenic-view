package com.javafx.experiments.scenicview.connector.details;

import javafx.scene.Node;

import com.javafx.experiments.scenicview.connector.StageID;
import com.javafx.experiments.scenicview.connector.event.AppEventDispatcher;

public class AllDetails {

    private Node target;

    final DetailPaneInfo[] details;

    public AllDetails(final AppEventDispatcher dispatcher, final StageID stageID) {
        details = new DetailPaneInfo[] { new NodeDetailPaneInfo(dispatcher, stageID), new ParentDetailPaneInfo(dispatcher, stageID), new RegionDetailPaneInfo(dispatcher, stageID), new GridPaneDetailPaneInfo(dispatcher, stageID), new ControlDetailPaneInfo(dispatcher, stageID), new TextDetailPaneInfo(dispatcher, stageID), new LabeledDetailPaneInfo(dispatcher, stageID), new FullPropertiesDetailPaneInfo(dispatcher, stageID) };
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
            } else {
                detail.clear();
            }
        }
    }

    public void setDetail(final DetailPaneType detailType, final int detailID, final String value) {
        for (final DetailPaneInfo detail : details) {
            if (detail.getType() == detailType) {
                detail.setDetail(detailID, value);
                break;
            }
        }
    }
}
