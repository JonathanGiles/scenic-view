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
package org.scenicview.view.tabs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.Node;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;

import org.fxconnector.details.Detail;
import org.fxconnector.details.DetailPaneType;
import org.fxconnector.node.SVNode;
import org.scenicview.view.ContextMenuContainer;
import org.scenicview.view.DisplayUtils;
import org.scenicview.view.ScenicViewGui;
import org.scenicview.view.tabs.details.GDetailPane;
import org.scenicview.view.tabs.details.GDetailPane.RemotePropertySetter;

/**
 * 
 */
public class DetailsTab extends Tab implements ContextMenuContainer {
    
    public static final String TAB_NAME = "Details";

    List<GDetailPane> gDetailPanes = new ArrayList<>();

    public static boolean showDefaultProperties = true;
    
    private final Consumer<String> loader;
    private final ScenicViewGui scenicView;

    VBox vbox;

    Menu menu;

    MenuItem dumpDetails;
    
    public DetailsTab(final ScenicViewGui view, final Consumer<String> loader) {
        super(TAB_NAME);
        this.scenicView = view;
        this.loader = loader;

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFitToWidth(true);
        vbox = new VBox();
        vbox.setFillWidth(true);
        scrollPane.setContent(vbox);
        getStyleClass().add("all-details-pane");
        
        setGraphic(new ImageView(DisplayUtils.getUIImage("details.png")));
        setContent(scrollPane);
        setClosable(false);
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
            pane = new GDetailPane(scenicView, type, paneName, loader);
            gDetailPanes.add(pane);
            vbox.getChildren().add(pane);
        }
        return pane;
    }

    @Override public Menu getMenu() {
        if (menu == null) {
            menu = new Menu("Details");
            
            // --- copy to clipboard
            dumpDetails = new MenuItem("Copy Details to Clipboard");
            dumpDetails.setOnAction(event -> {
                final StringBuilder sb = new StringBuilder();
                for (final Iterator<GDetailPane> iterator = gDetailPanes.iterator(); iterator.hasNext();) {
                    sb.append(iterator.next());
                }
                final Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString(sb.toString());
                clipboard.setContent(content);
            });
            updateDump();
            menu.getItems().addAll(dumpDetails);
            
            // --- show default properties
            final CheckMenuItem showDefaultProperties = scenicView.buildCheckMenuItem("Show Default Properties", "Show default properties",
                    "Hide default properties", "showDefaultProperties", Boolean.TRUE);
            showDefaultProperties.selectedProperty().addListener(new InvalidationListener() {
                @Override public void invalidated(final Observable arg0) {
                    setShowDefaultProperties(showDefaultProperties.isSelected());
                    final SVNode selected = scenicView.getSelectedNode();
                    scenicView.configurationUpdated();
                    scenicView.setSelectedNode(scenicView.activeStage, selected);
                }
            });
            setShowDefaultProperties(showDefaultProperties.isSelected());
            menu.getItems().addAll(showDefaultProperties);

            // --- show css properties
            final CheckMenuItem showCSSProperties = scenicView.buildCheckMenuItem("Show CSS Properties", "Show CSS properties", "Hide CSS properties",
                    "showCSSProperties", Boolean.FALSE);
            showCSSProperties.selectedProperty().addListener(new InvalidationListener() {
                @Override public void invalidated(final Observable arg0) {
                    scenicView.configuration.setCSSPropertiesDetail(showCSSProperties.isSelected());
                    final SVNode selected = scenicView.getSelectedNode();
                    scenicView.configurationUpdated();
                    scenicView.setSelectedNode(scenicView.activeStage, selected);
                }
            });
            scenicView.configuration.setCSSPropertiesDetail(showCSSProperties.isSelected());
            menu.getItems().addAll(showCSSProperties);
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
