package com.javafx.experiments.scenicview.connector.remote;

import java.util.List;

import com.javafx.experiments.scenicview.connector.AppController;

public interface FXConnector {

    List<AppController> connect();

    void close();

}
