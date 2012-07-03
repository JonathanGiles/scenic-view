/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.javafx.experiments.scenicview.details;

import java.util.*;

import javafx.scene.Node;
import javafx.scene.layout.VBox;

import com.javafx.experiments.scenicview.connector.details.*;
import com.javafx.experiments.scenicview.details.GDetailPane.RemotePropertySetter;

/**
 * 
 * @author aim
 */
public class AllDetailsPane extends VBox {

    List<GDetailPane> gDetailPanes = new ArrayList<GDetailPane>();

    static boolean showDefaultProperties = true;
    APILoader loader;

    public AllDetailsPane(final APILoader loader) {
        this.loader = loader;
        getStyleClass().add("all-details-pane");
        setFillWidth(true);
    }

    public void setShowDefaultProperties(final boolean show) {
        showDefaultProperties = show;
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
            pane = new GDetailPane(type, paneName, loader);
            gDetailPanes.add(pane);
            getChildren().add(pane);
        }
        return pane;
    }
}
