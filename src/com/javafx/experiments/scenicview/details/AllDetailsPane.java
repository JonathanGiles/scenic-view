/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.javafx.experiments.scenicview.details;

import java.util.*;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import com.javafx.experiments.scenicview.ContextMenuContainer;
import com.javafx.experiments.scenicview.connector.details.*;
import com.javafx.experiments.scenicview.details.GDetailPane.RemotePropertySetter;

/**
 * 
 * @author aim
 */
public abstract class AllDetailsPane extends ScrollPane implements ContextMenuContainer {

    List<GDetailPane> gDetailPanes = new ArrayList<GDetailPane>();

    static boolean showDefaultProperties = true;
    APILoader loader;

    VBox vbox;

    public AllDetailsPane(final APILoader loader) {
        this.loader = loader;

        setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setFitToWidth(true);
        vbox = new VBox();
        vbox.setFillWidth(true);
        setContent(vbox);
        getStyleClass().add("all-details-pane");

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
            vbox.getChildren().add(pane);
        }
        return pane;
    }

}
