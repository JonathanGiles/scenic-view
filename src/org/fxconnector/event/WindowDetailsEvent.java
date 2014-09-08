/*
 * Scenic View, 
 * Copyright (C) 2012 Jonathan Giles, Ander Ruiz, Amy Fowler 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fxconnector.event;

import org.fxconnector.StageID;

public class WindowDetailsEvent extends FXConnectorEvent {

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
