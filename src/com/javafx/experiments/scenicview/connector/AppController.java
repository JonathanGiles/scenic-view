package com.javafx.experiments.scenicview.connector;

import java.util.*;

public class AppController {

    private static final String LOCAL_ID = "Local";

    private final String id;
    private final List<StageController> stages = new ArrayList<StageController>();

    public AppController() {
        this(LOCAL_ID);
    }

    public AppController(final String id) {
        this.id = id;
    }

    public String getID() {
        return id;
    }

    public List<StageController> getStages() {
        return stages;
    }

    public void close() {
        for (int i = 0; i < stages.size(); i++) {
            stages.get(i).close();
        }
    }

    public boolean isLocal() {
        return LOCAL_ID.equals(id);
    }

}
