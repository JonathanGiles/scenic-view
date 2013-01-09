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


import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import com.javafx.experiments.scenicview.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class SubmitExceptionDialog {
    private static final int SCENE_WIDTH = 500;
    private static final int SCENE_HEIGHT = 175;
    private static final double MIN_BUTTON_WIDTH = 100;
    private static final double INSETS = 5;
    private final VBox panel;
    private Stage stage;
    private Scene scene;
    
    private final CheckBox dontAskAgainCheckBox;
    private InfoBox infoBox;
    
    private final String submissionText;
    
    private boolean isSubmissionAllowed = false;

    public SubmitExceptionDialog(final String submission) {
        this.submissionText = submission;
        this.panel = new VBox(20);
        this.panel.getStyleClass().add("about");
        this.panel.setPadding(new Insets(5, 10, 10, 10));
        
        // --- Label 
        Label label = LabelBuilder.create()
                .text("Unfortunately an exception has occurred.\r\n\r\nDo you want to submit this to "
                    + "the developers of Scenic View for analysis?\r\nNo personal information will be submitted, "
                    + "and you can click 'Show Exception Details' to see (and edit) everything that will be submitted.")
                .wrapText(true)
                .build();
        VBox.setMargin(label, new Insets(INSETS, INSETS, 0, INSETS));
        
        dontAskAgainCheckBox = CheckBoxBuilder.create()
                .text("Don't ask me again (remember this answer)")
                .build();
        VBox.setMargin(dontAskAgainCheckBox, new Insets(0, INSETS, 0, INSETS));
        
        Button showDetailsButton = ButtonBuilder.create()
                .text("Show Exception Details")
                .minWidth(MIN_BUTTON_WIDTH)
                .onAction(new EventHandler<ActionEvent>() {
                    @Override public void handle(ActionEvent t) {
                        // show info box
                        infoBox = new InfoBox(stage, "Exception Details",
                                "The following will be submitted. Feel free to edit the text below to "
                                + "add any relevant information or to remove any personal information.", 
                                submission, true, 800, 600);
                    }
                }).build();
        HBox.setMargin(showDetailsButton, new Insets(0, INSETS, 0, INSETS));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button okButton = ButtonBuilder.create()
                .text("Ok")
                .minWidth(MIN_BUTTON_WIDTH)
                .onAction(new EventHandler<ActionEvent>() {
                    @Override public void handle(ActionEvent t) {
                        isSubmissionAllowed = true;
                        stage.close();
                    }
                }).build();
        HBox.setMargin(okButton, new Insets(0, INSETS, 0, 0));
        
        Button noButton = ButtonBuilder.create()
                .text("No")
                .minWidth(MIN_BUTTON_WIDTH)
                .onAction(new EventHandler<ActionEvent>() {
                    @Override public void handle(ActionEvent t) {
                        isSubmissionAllowed = false;
                        stage.close();
                    }
                }).build();
        HBox.setMargin(noButton, new Insets(0, INSETS, 0, 0));
        
        HBox buttonsBox = HBoxBuilder.create()
                .children(showDetailsButton, spacer, okButton, noButton)
                .build();

        this.panel.getChildren().addAll(label,dontAskAgainCheckBox,buttonsBox);

        Platform.runLater(new Runnable() {
            @Override public void run() {
                scene = SceneBuilder.create().width(SCENE_WIDTH).height(SCENE_HEIGHT).root(panel).stylesheets(ScenicView.STYLESHEETS).build();

                stage = StageBuilder.create().style(StageStyle.UTILITY).title("Exception").build();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(scene);
                stage.getIcons().add(ScenicView.APP_ICON);
                stage.setResizable(false);

                stage.showAndWait();
            }
        });
        
    }

    public boolean isSubmissionAllowed() {
        return isSubmissionAllowed;
    }
    
    public boolean isRememberDecision() {
        return dontAskAgainCheckBox.isSelected();
    }
    
    public String getSubmissionText() {
        return infoBox == null ? submissionText : infoBox.getText();
    }
}