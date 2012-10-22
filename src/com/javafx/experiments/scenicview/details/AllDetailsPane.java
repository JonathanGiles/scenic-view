/*
 * Copyright (c) 2012 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
