package com.javafx.experiments.scenicview.update;

import com.javafx.experiments.scenicview.connector.*;

public interface AppsRepository {

    void appAdded(AppController appController);

    void appRemoved(AppController appController);

    void stageAdded(StageController stageController);

    void stageRemoved(StageController stageController);

}
