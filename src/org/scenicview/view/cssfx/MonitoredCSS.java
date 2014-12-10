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
package org.scenicview.view.cssfx;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MonitoredCSS {
    private StringProperty css = new SimpleStringProperty();
    private StringProperty mappedBy = new SimpleStringProperty();
    
    public MonitoredCSS(String css) {
        this.css.set(css);
    }
    
    public ReadOnlyStringProperty css() {
        return css;
    }
    
    public StringProperty mappedBy() {
        return mappedBy;
    }

    public String getCSS() {
        return css().get();
    }

    public String getMappedBy() {
        return mappedBy().get();
    }
}
