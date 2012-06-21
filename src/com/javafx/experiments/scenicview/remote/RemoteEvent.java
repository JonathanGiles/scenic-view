package com.javafx.experiments.scenicview.remote;

import java.io.Serializable;

public class RemoteEvent implements Serializable {

    String name;

    public RemoteEvent(final String name) {
        this.name = name;
    }
    
}
