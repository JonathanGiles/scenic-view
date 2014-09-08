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

import java.io.Serializable;

import org.fxconnector.StageID;


public class FXConnectorEvent implements Serializable {

    public enum SVEventType {
        EVENT_LOG, 
        MOUSE_POSITION, 
        WINDOW_DETAILS, 
        NODE_SELECTED, 
        NODE_ADDED, 
        NODE_REMOVED, 
        NODE_COUNT, 
        SCENE_DETAILS, 
        ROOT_UPDATED, 
        DETAILS, 
        DETAIL_UPDATED, 
        ANIMATIONS_UPDATED, 
        SHORTCUT
    }

    /**
     * 
     */
    private static final long serialVersionUID = -2556951288718105815L;

    private final SVEventType type;
    private final StageID stageID;

    public FXConnectorEvent(final SVEventType type, final StageID id) {
        this.type = type;
        this.stageID = id;
    }

    public SVEventType getType() {
        return type;
    }

    public StageID getStageID() {
        return stageID;
    }

    @Override public String toString() {
        return "AppEvent [type=" + type + ", stageID=" + stageID + "]";
    }

}
