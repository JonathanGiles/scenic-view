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
package org.scenicview.view.dialog;

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
import org.scenicview.view.ScenicViewGui;

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
        stage.getIcons().add(ScenicViewGui.APP_ICON);

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
