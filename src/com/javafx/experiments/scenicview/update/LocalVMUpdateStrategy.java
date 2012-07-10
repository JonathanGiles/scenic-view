package com.javafx.experiments.scenicview.update;

import java.util.*;

import javafx.stage.*;

import com.javafx.experiments.scenicview.connector.*;
import com.javafx.experiments.scenicview.connector.helper.*;
import com.javafx.experiments.scenicview.connector.helper.WindowChecker.WindowFilter;

public class LocalVMUpdateStrategy extends CommonUpdateStrategy implements WindowFilter {

    public LocalVMUpdateStrategy() {
        super(LocalVMUpdateStrategy.class.getName());
    }

    @Override List<AppController> getActiveApps() {
        final AppController local = new AppControllerImpl();
        final List<Window> stages = WindowChecker.getValidWindows(this);
        for (int i = 0; i < stages.size(); i++) {
            final StageController sc = new StageControllerImpl(stages.get(i).getScene().getRoot(), local);
            local.getStages().add(sc);
        }
        final List<AppController> controllers = new ArrayList<AppController>(1);
        controllers.add(local);
        return controllers;
    }

    @Override public boolean accept(final Window window) {
        if (window instanceof Stage) {
            return ConnectorUtils.acceptWindow(window);
        } else {
            System.out.println("Scenic View only supports Stages right now, but found a " + window.getClass());
            return false;
        }

    }

}
