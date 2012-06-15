package com.javafx.experiments.scenicview.dialog;

import java.util.logging.*;

import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.*;

import com.javafx.experiments.scenicview.*;

public class HelpBox {

    private static final int SCENE_WIDTH = 1024;
    private static final int SCENE_HEIGHT = 768;

    static final Image HELP_ICON = DisplayUtils.getUIImage("help.png");

    final Stage stage;

    public HelpBox(final String title, final String url, final double x, final double y) {
        final BorderPane pane = new BorderPane();
        pane.setPrefWidth(SCENE_WIDTH);
        pane.setPrefHeight(SCENE_HEIGHT);
        final WebView wview = new WebView();
        wview.setPrefHeight(SCENE_HEIGHT);
        wview.setPrefWidth(SCENE_WIDTH);
        wview.getEngine().load(url);
        pane.setCenter(wview);
        final Scene scene = SceneBuilder.create().width(SCENE_WIDTH).height(SCENE_HEIGHT).root(pane).stylesheets(ScenicView.STYLESHEETS).build();
        stage = StageBuilder.create().title(title).build();
        stage.setScene(scene);
        stage.getIcons().add(HELP_ICON);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            
            @Override public void handle(final WindowEvent arg0) {
                Logger.getLogger("com.sun.webpane").setLevel(wLevel);
                Logger.getLogger("webcore.platform.api.SharedBufferInputStream").setLevel(wpLevel);
            }
        });
        stage.show();
    }
    
    static Level wLevel;
    static Level wpLevel;

    public static HelpBox make(final String title, final String url, final Stage stage) {
        /**
         * Ugly patch to remove the visual trace of the WebPane
         */
        final Logger webLogger = java.util.logging.Logger.getLogger("com.sun.webpane");
        final Logger webPltLogger = java.util.logging.Logger.getLogger("webcore.platform.api.SharedBufferInputStream");
        wLevel = webLogger.getLevel();
        wpLevel = webPltLogger.getLevel();
        webLogger.setLevel(Level.SEVERE);
        webPltLogger.setLevel(Level.SEVERE);
        
        final HelpBox node = new HelpBox(title, url, stage.getX() + (stage.getWidth() / 2) - (SCENE_WIDTH / 2), stage.getY() + (stage.getHeight() / 2) - (SCENE_HEIGHT / 2));

        return node;
    }
}
