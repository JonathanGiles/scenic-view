package com.javafx.experiments.scenicview.connector.event;

import javafx.scene.input.KeyCode;

import com.javafx.experiments.scenicview.connector.StageID;

public class ShortcutEvent extends AppEvent {

    /**
     * 
     */
    private static final long serialVersionUID = -2848778452928775515L;
    private final KeyCode code;

    public ShortcutEvent(final StageID id, final KeyCode code) {
        super(SVEventType.SHORTCUT, id);
        this.code = code;
    }

    public KeyCode getCode() {
        return code;
    }

}
