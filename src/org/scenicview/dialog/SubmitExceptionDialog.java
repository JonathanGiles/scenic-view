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
package org.scenicview.dialog;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.scenicview.ScenicViewGui;

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
        Label label = new Label("Unfortunately an exception has occurred.\r\n\r\nDo you want to submit this to "
                    + "the developers of Scenic View for analysis?\r\nNo personal information will be submitted, "
                    + "and you can click 'Show Exception Details' to see (and edit) everything that will be submitted.");
        label.setWrapText(true);
        VBox.setMargin(label, new Insets(INSETS, INSETS, 0, INSETS));
        
        dontAskAgainCheckBox = new CheckBox("Don't ask me again (remember this answer)");
        VBox.setMargin(dontAskAgainCheckBox, new Insets(0, INSETS, 0, INSETS));
        
        Button showDetailsButton = new Button("Show Exception Details");
        showDetailsButton.setMinWidth(MIN_BUTTON_WIDTH);
        showDetailsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent t) {
                // show info box
                infoBox = new InfoBox(stage, "Exception Details",
                        "The following will be submitted. Feel free to edit the text below to "
                        + "add any relevant information or to remove any personal information.", 
                        submission, true, 800, 600);
            }
        });
        HBox.setMargin(showDetailsButton, new Insets(0, INSETS, 0, INSETS));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button okButton = new Button("Ok");
        okButton.setMinWidth(MIN_BUTTON_WIDTH);
        okButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent t) {
                isSubmissionAllowed = true;
                stage.close();
            }
        });
        HBox.setMargin(okButton, new Insets(0, INSETS, 0, 0));
        
        Button noButton = new Button("No");
        noButton.setMinWidth(MIN_BUTTON_WIDTH);
        noButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent t) {
                isSubmissionAllowed = false;
                stage.close();
            }
        });
        HBox.setMargin(noButton, new Insets(0, INSETS, 0, 0));
        
        HBox buttonsBox = new HBox(showDetailsButton, spacer, okButton, noButton);

        this.panel.getChildren().addAll(label,dontAskAgainCheckBox,buttonsBox);

        Platform.runLater(new Runnable() {
            @Override public void run() {
                scene = new Scene(panel, SCENE_WIDTH, SCENE_HEIGHT);

                stage = new Stage(StageStyle.UTILITY);
                stage.setTitle("Exception");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(scene);
                stage.getIcons().add(ScenicViewGui.APP_ICON);
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