package com.javafx.experiments.scenicview.connector;

import com.javafx.experiments.scenicview.connector.details.DetailPaneType;
import com.javafx.experiments.scenicview.connector.event.AppEventDispatcher;
import com.javafx.experiments.scenicview.connector.node.SVNode;

public interface StageController {

    public static final String SCENIC_VIEW_BASE_ID = "ScenicView.";

    StageID getID();

    void update();

    void configurationUpdated(Configuration configuration);

    void close();

    void setEventDispatcher(AppEventDispatcher stageModelListener);

    boolean isOpened();

    void setSelectedNode(SVNode value);

    AppController getAppController();

    void setDetail(DetailPaneType detailType, int detailID, String value);

    void animationsEnabled(boolean enabled);

    void updateAnimations();

    void pauseAnimation(int animationID);

}
