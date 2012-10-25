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
package com.javafx.experiments.scenicview.dialog;

import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import com.javafx.experiments.fxconnector.StageController;
import com.javafx.experiments.scenicview.ScenicView;

public class InfoBox {

    public InfoBox(final String title, final String labelText, final String textAreaText, final int width, final int height) {
        final VBox pane = new VBox();
        pane.setId(StageController.FX_CONNECTOR_BASE_ID + "InfoBox");
        final Scene scene = SceneBuilder.create().width(width).height(height).root(pane).stylesheets(ScenicView.STYLESHEETS).build();

        final Stage stage = StageBuilder.create().style(StageStyle.UTILITY).title(title).build();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setScene(scene);
        stage.getIcons().add(ScenicView.APP_ICON);

        final Label label = new Label(labelText);
        stage.setWidth(width);
        stage.setHeight(height);
        TextArea area = null;
        if (textAreaText != null) {
            area = new TextArea(textAreaText);
            area.setFocusTraversable(false);
            area.setEditable(false);
            VBox.setMargin(area, new Insets(5, 5, 0, 5));
            VBox.setVgrow(area, Priority.ALWAYS);
        }
        final Button close = new Button("Close");
        VBox.setMargin(label, new Insets(5, 5, 0, 5));

        VBox.setMargin(close, new Insets(5, 5, 5, 5));

        pane.setAlignment(Pos.CENTER);

        close.setDefaultButton(true);
        close.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent arg0) {
                stage.close();
            }
        });
        if (area != null) {
            pane.getChildren().addAll(label, area, close);
        } else {
            pane.getChildren().addAll(label, close);
        }

        stage.show();
    }

    public static InfoBox make(final String title, final String labelText, final String textAreaText) {
        return make(title, labelText, textAreaText, 700, 600);
    }

    public static InfoBox make(final String title, final String labelText, final String textAreaText, final int width, final int height) {
        return new InfoBox(title, labelText, textAreaText, width, height);
    }

}
