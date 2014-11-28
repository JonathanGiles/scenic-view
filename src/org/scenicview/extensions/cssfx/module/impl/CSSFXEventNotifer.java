package org.scenicview.extensions.cssfx.module.impl;

import org.scenicview.extensions.cssfx.module.api.CSSFXEvent;

public interface CSSFXEventNotifer {

    public abstract void eventNotify(CSSFXEvent<?> e);

}