package com.javafx.experiments.scenicview;

import com.javafx.experiments.scenicview.connector.event.AppEvent;

interface Model2GUI {

    void updateStageModel(StageModel stageModel);

    void dispatchEvent(AppEvent appEvent);

}
