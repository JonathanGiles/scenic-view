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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import org.fxconnector.StageController;
import org.scenicview.ScenicView;

public class InfoBox {
    
    private final TextArea textArea;
    
    public InfoBox(final String title, final String labelText, final String textAreaText) {
        this(title, labelText, textAreaText, 700, 600);
    }
    
    public InfoBox(final String title, final String labelText, 
            final String textAreaText, final int width, final int height) {
        this(title, labelText, textAreaText, false, width, height);
    }
    
    public InfoBox(final String title, final String labelText, 
            final String textAreaText, final boolean editable, final int width, final int height) {
        this(null, title, labelText, textAreaText, editable, width, height);
    }

    public InfoBox(final Window owner, final String title, final String labelText, 
            final String textAreaText, final boolean editable, final int width, final int height) {
        final VBox pane = new VBox(20);
        pane.setId(StageController.FX_CONNECTOR_BASE_ID + "InfoBox");
        final Scene scene = new Scene(pane, width, height); 

        final Stage stage = new Stage(StageStyle.UTILITY);
        stage.setTitle(title);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);
        stage.setScene(scene);
        stage.getIcons().add(ScenicView.APP_ICON);

        final Label label = new Label(labelText);
        stage.setWidth(width);
        stage.setHeight(height);
        textArea = new TextArea();
        if (textAreaText != null) {
            textArea.setEditable(editable);
            textArea.setText(textAreaText);
            VBox.setMargin(textArea, new Insets(5, 5, 0, 5));
            VBox.setVgrow(textArea, Priority.ALWAYS);
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
        if (textArea != null) {
            pane.getChildren().addAll(label, textArea, close);
        } else {
            pane.getChildren().addAll(label, close);
        }

        stage.show();
    }
    
    public String getText() {
        return textArea.getText();
    }
}
