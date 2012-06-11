package com.javafx.experiments.scenicview;

import java.util.List;

import javafx.beans.value.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.ListView;
import javafx.scene.layout.*;
import javafx.stage.*;

import com.javafx.experiments.scenicview.helper.*;
import com.javafx.experiments.scenicview.helper.WindowChecker.WindowFilter;

public class StageSelectionBox {
    private static final int SCENE_WIDTH = 200;
    private static final int SCENE_HEIGHT = 300;
    private static final int LEFT_AND_RIGHT_MARGIN = 10;
    private static final int SPACER_Y = 10;
    private final VBox panel;
    private final Stage stage;
    private final Scene scene;
    private final ListView<String> windowList;

    private StageSelectionBox(final String title, final double x, final double y, final Stage scenicView) {
        this.panel = new VBox();
        this.panel.getStyleClass().add("stageSelection");

        this.windowList = new ListView<String>();
        this.windowList.setFocusTraversable(false);
        this.windowList.setEditable(false);
        this.windowList.setId("stageSelectionList");
        this.windowList.setPrefHeight(221.0D);
        final List<Window> stages = WindowChecker.getValidWindows(new WindowFilter() {
            
            @Override public boolean accept(final Window window) {
                // TODO Auto-generated method stub
                return window instanceof Stage && window != scenicView;
            }
        });
        
        final ObservableList<String> stageNames = FXCollections.observableArrayList();
        for (int i = 0; i < stages.size(); i++) {
            stageNames.add(((Stage)stages.get(i)).getTitle());
        }
        windowList.setItems(stageNames);
        
        VBox.setMargin(this.windowList, new Insets(SPACER_Y, LEFT_AND_RIGHT_MARGIN, SPACER_Y, LEFT_AND_RIGHT_MARGIN));
        VBox.setVgrow(this.windowList, Priority.ALWAYS);
        this.panel.setAlignment(Pos.TOP_CENTER);
        this.panel.getChildren().addAll(this.windowList);

        this.scene = SceneBuilder.create().width(SCENE_WIDTH).height(SCENE_HEIGHT).root(this.panel).stylesheets(new String[] { StageSelectionBox.class.getResource("scenicview.css").toString() }).build();

        this.stage = StageBuilder.create().style(StageStyle.UTILITY).title(title).build();
        this.stage.initModality(Modality.APPLICATION_MODAL);
        this.stage.setScene(this.scene);
        this.stage.getIcons().add(ScenicView.APP_ICON);
        this.stage.setResizable(false);
        this.stage.setX(x);
        this.stage.setY(y);
        this.stage.show();
        windowList.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

            @Override public void changed(final ObservableValue<? extends Number> arg0, final Number arg1, final Number newValue) {
                if(scenicView!=null) {
                    scenicView.close();
                }
                ScenicView.show(stages.get(newValue.intValue()).getScene());
                stage.close();
            }
        });
    }

    public static StageSelectionBox make(final String title, final Stage stage) {
        return new StageSelectionBox(title, stage==null?0:stage.getX() + (stage.getWidth() / 2) - (SCENE_WIDTH / 2), stage==null?0:stage.getY() + (stage.getHeight() / 2) - (SCENE_HEIGHT / 2), stage);
    }

}