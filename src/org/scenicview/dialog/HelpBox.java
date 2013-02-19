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
package org.scenicview.dialog;

import org.scenicview.DisplayUtils;
import org.scenicview.ScenicView;
import java.util.logging.Level;

import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.*;

import org.fxconnector.StageController;
import org.scenicview.control.ProgressWebView;

public class HelpBox {

    private static final int SCENE_WIDTH = 1024;
    private static final int SCENE_HEIGHT = 768;

    static final Image HELP_ICON = DisplayUtils.getUIImage("help.png");

    final Stage stage;

    public HelpBox(final String title, final String url, final double x, final double y) {
        final BorderPane pane = new BorderPane();
        pane.setId(StageController.FX_CONNECTOR_BASE_ID + "HelpBox");
        pane.setPrefWidth(SCENE_WIDTH);
        pane.setPrefHeight(SCENE_HEIGHT);
        final ProgressWebView wview = new ProgressWebView();
        wview.setPrefHeight(SCENE_HEIGHT);
        wview.setPrefWidth(SCENE_WIDTH);
        wview.doLoad(url);
        pane.setCenter(wview);
        final Scene scene = SceneBuilder.create().width(SCENE_WIDTH).height(SCENE_HEIGHT).root(pane)/*.stylesheets(ScenicView.STYLESHEETS)*/.build();
        stage = StageBuilder.create().title(title).build();
        stage.setScene(scene);
        stage.getIcons().add(HELP_ICON);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {

            @Override public void handle(final WindowEvent arg0) {
                DisplayUtils.showWebView(false);
            }
        });
        stage.show();
    }

    static Level wLevel;
    static Level wpLevel;

    public static HelpBox make(final String title, final String url, final Stage stage) {
        DisplayUtils.showWebView(true);
        final HelpBox node = new HelpBox(title, url, stage.getX() + (stage.getWidth() / 2) - (SCENE_WIDTH / 2), stage.getY() + (stage.getHeight() / 2) - (SCENE_HEIGHT / 2));
        return node;
    }
}
