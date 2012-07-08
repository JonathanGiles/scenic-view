package com.javafx.experiments.scenicview.update;

public interface UpdateStrategy {
    public void start(AppsRepository repository);

    public void finish();
}
