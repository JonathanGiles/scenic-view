package com.javafx.experiments.scenicview.update;

import java.util.*;

import com.javafx.experiments.scenicview.connector.*;
import com.javafx.experiments.scenicview.connector.helper.WorkerThread;

abstract class CommonUpdateStrategy extends WorkerThread implements UpdateStrategy {

    AppsRepository repository;
    List<AppController> previous = new ArrayList<AppController>();

    CommonUpdateStrategy(final String threadName) {
        super(threadName, 5000);
    }

    @Override public void start(final AppsRepository repository) {
        this.repository = repository;
        start();
    }

    @Override protected void work() {
        boolean modifications = false;
        final List<AppController> actualApps = getActiveApps();
        final List<StageController> unused = new ArrayList<StageController>();
        for (int i = 0; i < actualApps.size(); i++) {
            unused.addAll(actualApps.get(i).getStages());
        }
        /**
         * First check new apps
         */
        for (int i = 0; i < actualApps.size(); i++) {
            if (isAppOnArray(actualApps.get(i), previous) == -1) {
                repository.appAdded(actualApps.get(i));
                unused.removeAll(actualApps.get(i).getStages());
                modifications = true;
            }
        }
        /**
         * Then check remove apps
         */
        for (int i = 0; i < previous.size(); i++) {
            if (isAppOnArray(previous.get(i), actualApps) == -1) {
                repository.appRemoved(previous.get(i));
                modifications = true;
            }
        }
        /**
         * Then check added/removed Stages
         */
        for (int i = 0; i < actualApps.size(); i++) {
            if (isAppOnArray(actualApps.get(i), previous) != -1) {
                final List<StageController> stages = actualApps.get(i).getStages();
                final List<StageController> previousStages = previous.get(isAppOnArray(actualApps.get(i), previous)).getStages();
                for (int j = 0; j < stages.size(); j++) {
                    if (!isStageOnArray(stages.get(j), previousStages)) {
                        repository.stageAdded(stages.get(j));
                        unused.remove(stages.get(j));
                        modifications = true;
                    }
                }
                for (int j = 0; j < previousStages.size(); j++) {
                    if (!isStageOnArray(previousStages.get(j), stages)) {
                        repository.stageRemoved(previousStages.get(j));
                        modifications = true;
                    }
                }
            }
        }
        if (modifications) {
            previous = actualApps;
        }
        closeUnused(unused);
    }

    protected void closeUnused(final List<StageController> unused) {
        // TODO Auto-generated method stub

    }

    boolean isStageOnArray(final StageController controller, final List<StageController> stages) {
        for (int i = 0; i < stages.size(); i++) {
            if (stages.get(i).getID().getStageID() == controller.getID().getStageID()) {
                return true;
            }
        }
        return false;
    }

    int isAppOnArray(final AppController controller, final List<AppController> apps) {
        for (int i = 0; i < apps.size(); i++) {
            if (apps.get(i).getID() == controller.getID()) {
                return i;
            }
        }
        return -1;
    }

    abstract List<AppController> getActiveApps();

    @Override public boolean needsClassPathConfiguration() {
        return false;
    }
}
