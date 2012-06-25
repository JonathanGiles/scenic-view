package com.javafx.experiments.scenicview.connector;

import java.util.List;

public interface AppController {

    public int getID();

    public List<StageController> getStages();

    public void close();

    public boolean isLocal();

}
