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

import org.scenicview.utils.PropertiesUtils;
import org.scenicview.DisplayUtils;
import org.scenicview.ScenicView;
import org.scenicview.ScenicViewGui;

import java.util.Properties;

import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import org.fxconnector.StageController;

public class AboutBox {
    private static final int SCENE_WIDTH = 476;
    private static final int SCENE_HEIGHT = 500;
    private static final int LEFT_AND_RIGHT_MARGIN = 30;
    private static final int SPACER_Y = 38;
    private final VBox panel;
    private final Stage stage;
    private final Scene scene;
    private final ImageView header;
    private final Button footer;
    private final TextArea textArea;

    private AboutBox(final String title, final double x, final double y) {
        this.panel = new VBox();
        this.panel.setId(StageController.FX_CONNECTOR_BASE_ID + "AboutBox");
        this.panel.getStyleClass().add("about");

        this.footer = new Button("Close");
        this.footer.setDefaultButton(true);
        this.footer.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent arg0) {
                stage.close();
            }
        });
        VBox.setMargin(this.footer, new Insets(SPACER_Y / 2, LEFT_AND_RIGHT_MARGIN, SPACER_Y / 2, LEFT_AND_RIGHT_MARGIN));

        this.header = new ImageView(DisplayUtils.getUIImage("about-header.png"));
        this.header.setId("AboutHeader");

        VBox.setMargin(this.header, new Insets(42.0D, LEFT_AND_RIGHT_MARGIN, 0.0D, LEFT_AND_RIGHT_MARGIN));

        this.textArea = new TextArea();
        this.textArea.setFocusTraversable(false);
        this.textArea.setEditable(false);
//        this.textArea.setId("aboutDialogDetails");
        this.textArea.setText(getAboutText());
        this.textArea.setWrapText(true);
//        this.textArea.setPrefHeight(250.0D);
        VBox.setMargin(this.textArea, new Insets(SPACER_Y, LEFT_AND_RIGHT_MARGIN, 0.0D, LEFT_AND_RIGHT_MARGIN));
        VBox.setVgrow(this.textArea, Priority.ALWAYS);
        this.panel.setAlignment(Pos.TOP_CENTER);
        this.panel.getChildren().addAll(this.header, this.textArea, this.footer);

        this.scene = new Scene(panel, SCENE_WIDTH, SCENE_HEIGHT);

        this.stage = new Stage(StageStyle.UTILITY);
        this.stage.setTitle(title);
        this.stage.initModality(Modality.APPLICATION_MODAL);
        this.stage.setScene(this.scene);
        this.stage.getIcons().add(ScenicViewGui.APP_ICON);
        this.stage.setResizable(false);
        this.stage.setX(x);
        this.stage.setY(y);
        this.stage.show();
    }

    public static AboutBox make(final String title, final Stage stage) {
        return new AboutBox(title, stage.getX() + (stage.getWidth() / 2) - (SCENE_WIDTH / 2), stage.getY() + (stage.getHeight() / 2) - (SCENE_HEIGHT / 2));
    }

    private static String getAboutText() {
        final Properties properties = PropertiesUtils.getProperties();
        String toolsPath = properties.getProperty(ScenicView.JDK_PATH_KEY);
        toolsPath = toolsPath == null ? "Included in runtime classpath" : toolsPath;
        
        final String text = "JavaFX Scenic View " + ScenicViewGui.VERSION + "\n" + 
            "Scenic View developed by Amy Fowler, Ander Ruiz and Jonathan Giles\n" + "\n" +
                
            "JavaFX Build Information:" + "\n" + 
            "    Java FX " + System.getProperty("javafx.runtime.version") + "\n" + "\n" +

            "Required Libraries:\n" + 
            "    tools.jar Home: " + toolsPath + "\n\n" +

            "Operating System:\n" + 
            "    " + System.getProperty("os.name") + ", " + System.getProperty("os.arch") + ", " + System.getProperty("os.version") +

            "\n\nJava Version:\n" + 
            "    " + System.getProperty("java.version") + ", " + System.getProperty("java.vendor") + ", " + System.getProperty("java.runtime.version");

        return text;
    }
}