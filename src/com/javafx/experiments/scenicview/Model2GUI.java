package com.javafx.experiments.scenicview;

import java.util.List;

import javafx.scene.control.TreeItem;
import javafx.stage.Window;

interface Model2GUI {

    void updateWindowDetails(StageModel stageModel, Window targetWindow);

    void updateMousePosition(StageModel stageModel, String string);

    void overlayParentNotFound(StageModel stageModel);

    void updateStageModel(StageModel stageModel);

    void selectOnClick(StageModel stageModel, TreeItem<NodeInfo> findDeepSelection);

    boolean isIgnoreMouseTransparent();

    boolean isAutoRefreshStyles();

    List<TreeItem<NodeInfo>> getTreeItems();

}
