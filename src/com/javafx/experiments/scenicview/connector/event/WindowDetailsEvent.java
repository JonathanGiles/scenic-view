package com.javafx.experiments.scenicview.connector.event;

import com.javafx.experiments.scenicview.connector.StageID;

public class WindowDetailsEvent extends AppEvent {

    /**
     * 
     */
    private static final long serialVersionUID = 6484452153829938311L;
    private final String windowType;
    private final String bounds;
    private final boolean focused;
    private final boolean stylesRefreshable;

    public WindowDetailsEvent(final StageID id, final String windowType, final String bounds, final boolean focused, final boolean stylesRefreshable) {
        super(SVEventType.WINDOW_DETAILS, id);
        this.windowType = windowType;
        this.bounds = bounds;
        this.focused = focused;
        this.stylesRefreshable = stylesRefreshable;
    }

    public String getWindowType() {
        return windowType;
    }

    public String getBounds() {
        return bounds;
    }

    public boolean isFocused() {
        return focused;
    }

    public boolean isStylesRefreshable() {
        return stylesRefreshable;
    }

}
