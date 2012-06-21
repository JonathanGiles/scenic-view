package com.javafx.experiments.scenicview;

import java.util.List;

import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.stage.Window;

import com.javafx.experiments.scenicview.connector.SVNode;

interface Model2GUI {

    void updateWindowDetails(StageModel stageModel, Window targetWindow);

    void updateMousePosition(StageModel stageModel, String string);

    void overlayParentNotFound(StageModel stageModel);

    void updateStageModel(StageModel stageModel);

    void selectOnClick(StageModel stageModel, TreeItem<SVNode> findDeepSelection);

    boolean isIgnoreMouseTransparent();

    boolean isAutoRefreshStyles();

    List<TreeItem<SVNode>> getTreeItems();

    void updateSceneDetails(StageModel stageModel, Scene targetScene);

}
