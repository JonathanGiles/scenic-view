/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.javafx.experiments.scenicview.details;

import java.util.*;

import javafx.scene.Node;
import javafx.scene.layout.VBox;

import com.javafx.experiments.scenicview.connector.details.*;
import com.javafx.experiments.scenicview.connector.details.Detail;
import com.javafx.experiments.scenicview.details.GDetailPane.RemotePropertySetter;

/**
 * 
 * @author aim
 */
public class AllDetailsPane extends VBox {
    DetailPane detailPanes[] = { new GridPaneDetailPane() };

    List<GDetailPane> gDetailPanes = new ArrayList<GDetailPane>();

    static boolean showDefaultProperties = true;

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
        showDefaultProperties = show;
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
                details.setExpanded(false);
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
        for (final Iterator<GDetailPane> iterator = gDetailPanes.iterator(); iterator.hasNext();) {
            final GDetailPane type = iterator.next();
            type.filterProperties(text);
        }
    }

    public void updateDetails(final DetailPaneType type, final String paneName, final List<Detail> details, final RemotePropertySetter setter) {
        final GDetailPane pane = getPane(type, paneName);
        pane.updateDetails(details, setter);

        boolean detailVisible = false;
        for (final Node gridChild : pane.gridpane.getChildren()) {
            detailVisible = gridChild.isVisible();
            if (detailVisible)
                break;
        }
        pane.setExpanded(detailVisible);
        pane.setManaged(detailVisible);
        pane.setVisible(detailVisible);
    }

    public void updateDetail(final DetailPaneType type, final String paneName, final Detail detail) {
        getPane(type, paneName).updateDetail(detail);
    }

    private GDetailPane getPane(final DetailPaneType type, final String paneName) {
        GDetailPane pane = null;
        boolean found = false;
        for (int i = 0; i < gDetailPanes.size(); i++) {
            if (gDetailPanes.get(i).type == type) {
                found = true;
                pane = gDetailPanes.get(i);
                break;
            }
        }
        if (!found) {
            pane = new GDetailPane(type, paneName);
            gDetailPanes.add(pane);
            getChildren().add(pane);
        }
        return pane;
    }
}
