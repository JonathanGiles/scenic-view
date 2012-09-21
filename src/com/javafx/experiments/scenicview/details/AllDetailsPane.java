/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.javafx.experiments.scenicview.details;

import java.util.*;

import javafx.event.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
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

    Menu menu;

    MenuItem dumpDetails;

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
            updatedDetailPane(type);
        }
    }

    public void updateDetails(final DetailPaneType type, final String paneName, final List<Detail> details, final RemotePropertySetter setter) {
        final GDetailPane pane = getPane(type, paneName);
        pane.updateDetails(details, setter);
        updatedDetailPane(pane);
    }

    private void updatedDetailPane(final GDetailPane pane) {
        boolean detailVisible = false;
        for (final Node gridChild : pane.gridpane.getChildren()) {
            detailVisible = gridChild.isVisible();
            if (detailVisible)
                break;
        }
        pane.setExpanded(detailVisible);
        pane.setManaged(detailVisible);
        pane.setVisible(detailVisible);
        updateDump();
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

    @Override public Menu getMenu() {
        if (menu == null) {
            menu = new Menu("Details");
            dumpDetails = new MenuItem("Copy Details to Clipboard");
            dumpDetails.setOnAction(new EventHandler<ActionEvent>() {

                @Override public void handle(final ActionEvent arg0) {
                    final StringBuilder sb = new StringBuilder();
                    for (final Iterator<GDetailPane> iterator = gDetailPanes.iterator(); iterator.hasNext();) {
                        sb.append(iterator.next());
                    }
                    final Clipboard clipboard = Clipboard.getSystemClipboard();
                    final ClipboardContent content = new ClipboardContent();
                    content.putString(sb.toString());
                    clipboard.setContent(content);
                }
            });
            updateDump();
            menu.getItems().addAll(dumpDetails);
        }
        return menu;
    }

    private void updateDump() {
        boolean anyVisible = false;
        for (final Iterator<GDetailPane> iterator = gDetailPanes.iterator(); iterator.hasNext();) {
            if (iterator.next().isVisible()) {
                anyVisible = true;
                break;
            }
        }
        dumpDetails.setDisable(!anyVisible);
    }
}
