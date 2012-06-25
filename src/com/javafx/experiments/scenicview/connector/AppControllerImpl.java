package com.javafx.experiments.scenicview.connector;

import java.util.*;

public class AppControllerImpl implements AppController {

    private static final String LOCAL_ID = "Local";

    private final String name;
    private final List<StageController> stages = new ArrayList<StageController>();

    private final int id;

    public AppControllerImpl() {
        this(0, LOCAL_ID);
    }

    public AppControllerImpl(final int id, final String name) {
        this.id = id;
        this.name = name;
    }

    @Override public String toString() {
        return name;
    }

    @Override public List<StageController> getStages() {
        return stages;
    }

    @Override public void close() {
        for (int i = 0; i < stages.size(); i++) {
            stages.get(i).close();
        }
    }

    @Override public boolean isLocal() {
        return LOCAL_ID.equals(name);
    }

    @Override public int getID() {
        return id;
    }

}
