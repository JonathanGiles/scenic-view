package org.fxmisc.cssfx.impl.events;

/*
 * #%L
 * CSSFX
 * %%
 * Copyright (C) 2014 CSSFX by Matthieu Brouillard
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


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
