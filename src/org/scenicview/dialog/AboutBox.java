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

import org.scenicview.utils.ScenicViewBooter;
import org.scenicview.utils.PropertiesUtils;
import org.scenicview.DisplayUtils;
import org.scenicview.ScenicView;
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
    private static final int SCENE_HEIGHT = 474;
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

        this.header = ((ImageViewBuilder.create().id("AboutHeader")).image(DisplayUtils.getUIImage("about-header.png"))).build();

        VBox.setMargin(this.header, new Insets(42.0D, LEFT_AND_RIGHT_MARGIN, 0.0D, LEFT_AND_RIGHT_MARGIN));

        this.textArea = new TextArea();
        this.textArea.setFocusTraversable(false);
        this.textArea.setEditable(false);
        this.textArea.setId("aboutDialogDetails");
        this.textArea.setText(getAboutText());
        this.textArea.setWrapText(true);
        this.textArea.setPrefHeight(250.0D);
        VBox.setMargin(this.textArea, new Insets(SPACER_Y, LEFT_AND_RIGHT_MARGIN, 0.0D, LEFT_AND_RIGHT_MARGIN));
        VBox.setVgrow(this.textArea, Priority.ALWAYS);
        this.panel.setAlignment(Pos.TOP_CENTER);
        this.panel.getChildren().addAll(this.header, this.textArea, this.footer);

        this.scene = SceneBuilder.create().width(SCENE_WIDTH).height(SCENE_HEIGHT).root(this.panel)/*.stylesheets(ScenicView.STYLESHEETS)*/.build();

        this.stage = StageBuilder.create().style(StageStyle.UTILITY).title(title).build();
        this.stage.initModality(Modality.APPLICATION_MODAL);
        this.stage.setScene(this.scene);
        this.stage.getIcons().add(ScenicView.APP_ICON);
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
        String toolsPath = properties.getProperty(ScenicViewBooter.TOOLS_JAR_PATH_KEY);
        toolsPath = toolsPath == null ? "Included in runtime classpath" : toolsPath;
        String jfxPath = properties.getProperty(ScenicViewBooter.JFXRT_JAR_PATH_KEY);
        jfxPath = jfxPath == null ? "Included in runtime classpath" : jfxPath;

        final String text = "JavaFX Scenic View " + ScenicView.VERSION + "\n" + "Scenic View developed by Amy Fowler, Ander Ruiz and Jonathan Giles\n" + "\n" + "JavaFX Build Information:" + "\n" + "Java FX " + System.getProperty("javafx.runtime.version") + "\n" + "\n" +

        "Required Libraries:\n" + "jfxrt.jar Home: " + jfxPath + "\n" + "tools.jar Home: " + toolsPath + "\n\n" +

        "Operating System\n" + System.getProperty("os.name") + ", " + System.getProperty("os.arch") + ", " + System.getProperty("os.version") +

        "\n\nJava Version\n" + System.getProperty("java.version") + ", " + System.getProperty("java.vendor") + ", " + System.getProperty("java.runtime.version");

        return text;
    }
}