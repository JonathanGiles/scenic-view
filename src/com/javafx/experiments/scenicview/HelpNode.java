package com.javafx.experiments.scenicview;

import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.*;

public class HelpNode {

    private static final int SCENE_WIDTH = 1024;
    private static final int SCENE_HEIGHT = 768;

    static final Image HELP_ICON = new Image(HelpNode.class.getResource("images/ui/help.png").toString());

    final Stage stage;

    public HelpNode(final String title, final String url, final double x, final double y) {
        final BorderPane pane = new BorderPane();
        pane.setPrefWidth(SCENE_WIDTH);
        pane.setPrefHeight(SCENE_HEIGHT);
        final WebView wview = new WebView();
        wview.setPrefHeight(SCENE_HEIGHT);
        wview.setPrefWidth(SCENE_WIDTH);
        wview.getEngine().load(url);
        pane.setCenter(wview);
        final Scene scene = SceneBuilder.create().width(SCENE_WIDTH).height(SCENE_HEIGHT).root(pane).stylesheets(new String[] { AboutBox.class.getResource("scenicview.css").toString() }).build();
        stage = StageBuilder.create().title(title).build();
        stage.setScene(scene);
        stage.getIcons().add(HELP_ICON);
//        stage.setX(x);
//        stage.setY(y);
        stage.show();
    }

    public static HelpNode make(final String title, final String url, final Stage stage) {
        return new HelpNode(title, url, stage.getX() + (stage.getWidth() / 2) - (SCENE_WIDTH / 2), stage.getY() + (stage.getHeight() / 2) - (SCENE_HEIGHT / 2));
    }
}
