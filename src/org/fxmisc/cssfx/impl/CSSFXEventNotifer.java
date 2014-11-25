package org.fxmisc.cssfx.impl;

import org.fxmisc.cssfx.api.CSSFXEvent;

public interface CSSFXEventNotifer {

    public abstract void eventNotify(CSSFXEvent<?> e);

}