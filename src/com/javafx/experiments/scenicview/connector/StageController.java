package com.javafx.experiments.scenicview.connector;

import com.javafx.experiments.scenicview.connector.event.AppEventDispatcher;
import com.javafx.experiments.scenicview.connector.node.SVNode;

public interface StageController {

    public static final String SCENIC_VIEW_BASE_ID = "ScenicView.";

    StageID getID();

    void update();

    void configurationUpdated(Configuration configuration);

    void close();

    void setEventDispatcher(AppEventDispatcher stageModelListener);

    void setSelectedNode(SVNode value);

    AppController getAppController();

}
