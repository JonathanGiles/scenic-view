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
package org.fxconnector.event;

import org.fxconnector.StageID;
import org.fxconnector.node.SVNode;

public class EvCSSFXEvent extends FXConnectorEvent {
    private static final long serialVersionUID = 7270158278849080779L;
    private final SVNode origin;
    private final String uri;
    private final String source;
    
    public EvCSSFXEvent(SVEventType type, StageID id, SVNode origin, String uri) {
        this(type, id, origin, uri, null);
    }
    
    public EvCSSFXEvent(SVEventType type, StageID id, SVNode origin, String uri, String source) {
        super(type, id);
        this.origin = origin;
        this.uri = uri;
        this.source = source;
    }

    public SVNode getOrigin() {
        return origin;
    }

    public String getUri() {
        return uri;
    }

    public String getSource() {
        return source;
    }
}
