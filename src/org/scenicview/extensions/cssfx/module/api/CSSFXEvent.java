/*
 * Scenic View, 
 * Copyright (C) 2012 Jonathan Giles, Ander Ruiz, Amy Fowler, Matthieu Brouillard
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
package org.scenicview.extensions.cssfx.module.api;

public final class CSSFXEvent<T> {
    private final EventType eventType;
    private final T eventData;
    
    public static enum EventType {
        STYLESHEET_ADDED
        , STYLESHEET_REMOVED
        , NODE_ADDED
        , NODE_REMOVED
        , SCENE_ADDED
        , SCENE_REMOVED
        , STAGE_ADDED
        , STAGE_REMOVED
        , STYLESHEET_REPLACED
        , STYLESHEET_MONITORED
    }
    
    private  CSSFXEvent(EventType type, T data) {
        eventType = type;
        eventData = data;
    }

    public EventType getEventType() {
        return eventType;
    }

    public T getEventData() {
        return eventData;
    }
    
    public static <T> CSSFXEvent<T> newEvent(EventType type, T data) {
        return new CSSFXEvent<T>(type, data);
    }

    @Override
    public String toString() {
        return String.format("CSSFXEvent [eventType=%s, eventData=%s]", eventType, eventData);
    }
}
