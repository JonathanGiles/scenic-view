package com.javafx.experiments.scenicview.update;

import java.util.List;

import com.javafx.experiments.scenicview.connector.AppController;

/**
 * This strategy will be used when we are showing only one stage
 * 
 * @author Ander
 * 
 */
public class DummyUpdateStrategy implements UpdateStrategy {

    List<AppController> controller;

    public DummyUpdateStrategy(final List<AppController> controller) {
        this.controller = controller;
    }

    @Override public void start(final AppsRepository repository) {
        for (int i = 0; i < controller.size(); i++) {
            repository.appAdded(controller.get(i));
        }
    }

    @Override public void finish() {
        // TODO Auto-generated method stub

    }
}
