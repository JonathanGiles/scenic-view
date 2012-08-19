package com.javafx.experiments.scenicview.dialog;

import java.util.logging.Level;

import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.*;

import com.javafx.experiments.scenicview.*;
import com.javafx.experiments.scenicview.connector.StageController;

public class HelpBox {

    private static final int SCENE_WIDTH = 1024;
    private static final int SCENE_HEIGHT = 768;

    static final Image HELP_ICON = DisplayUtils.getUIImage("help.png");

    final Stage stage;

    public HelpBox(final String title, final String url, final double x, final double y) {
        final BorderPane pane = new BorderPane();
        pane.setId(StageController.SCENIC_VIEW_BASE_ID + "HelpBox");
        pane.setPrefWidth(SCENE_WIDTH);
        pane.setPrefHeight(SCENE_HEIGHT);
        final ProgressWebView wview = new ProgressWebView();
        wview.setPrefHeight(SCENE_HEIGHT);
        wview.setPrefWidth(SCENE_WIDTH);
        wview.doLoad(url);
        pane.setCenter(wview);
        final Scene scene = SceneBuilder.create().width(SCENE_WIDTH).height(SCENE_HEIGHT).root(pane).stylesheets(ScenicView.STYLESHEETS).build();
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
