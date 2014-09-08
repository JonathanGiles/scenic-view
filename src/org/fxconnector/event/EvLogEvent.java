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
import org.fxconnector.node.SVNode;

public class EvLogEvent extends FXConnectorEvent {

    public static final String PROPERTY_CHANGED = "PROPERTY_CHANGED";
    public static final String OTHER_EVENTS = "OTHER_EVENTS";
    public static final String NODE_REMOVED = "NODE_REMOVED";
    public static final String NODE_ADDED = "NODE_ADDED";

    /**
     * 
     */
    private static final long serialVersionUID = -4130339506376073468L;
    private final SVNode source;
    private final String eventType;
    private final String eventValue;

    public EvLogEvent(final StageID id, final SVNode source, final String eventType, final String eventValue) {
        super(SVEventType.EVENT_LOG, id);
        this.source = source;
        this.eventType = eventType;
        this.eventValue = eventValue;
    }

    public SVNode getSource() {
        return source;
    }

    public String getEventType() {
        return eventType;
    }

    public String getEventValue() {
        return eventValue;
    }

}
