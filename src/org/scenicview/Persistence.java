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
 package org.scenicview;

import java.util.*;

import javafx.scene.control.*;
import javafx.stage.Stage;

import org.scenicview.utils.PropertiesUtils;

class Persistence {

    private static Properties properties;// = PropertiesUtils.loadProperties();
    private static final Map<String, Object> persistentComponents = new HashMap<>();

    static void loadProperties() {
        properties = PropertiesUtils.getProperties();
    }

    static String loadProperty(final String propertyName, final String defaultValue) {
        return properties.getProperty(propertyName, defaultValue);
    }

    static void saveProperty(final String propertyName, final String defaultValue) {
        properties.put(propertyName, defaultValue);
    }

    static void loadProperty(final String propertyName, final Object component, final Object defaultValue) {
        final String property = properties.getProperty(propertyName, defaultValue.toString());
        if (component instanceof CheckMenuItem) {
            ((CheckMenuItem) component).setSelected(Boolean.parseBoolean(property));
        }
        // We should think of a better way of doing this
        else if (component instanceof SplitPane) {
            ((SplitPane) component).setDividerPosition(0, Double.parseDouble(property));
        }
        // else if(component instanceof Control) {
        // if(propertyName.toLowerCase().indexOf("width")!=-1) {
        // ((Control) component).setPrefWidth(Double.parseDouble(property));
        // }
        // else {
        // ((Control) component).setPrefHeight(Double.parseDouble(property));
        // }
        // }
        else if (component instanceof Stage) {
            if (propertyName.toLowerCase().indexOf("width") != -1) {
                ((Stage) component).setWidth(Double.parseDouble(property));
            } else {
                ((Stage) component).setHeight(Double.parseDouble(property));
            }
        }
        persistentComponents.put(propertyName, component);
    }

    static void saveProperties() {
        for (final Iterator<String> iterator = persistentComponents.keySet().iterator(); iterator.hasNext();) {
            final String propertyName = iterator.next();
            final Object component = persistentComponents.get(propertyName);
            if (component instanceof CheckMenuItem) {
                properties.put(propertyName, Boolean.toString(((CheckMenuItem) component).isSelected()));
            } else if (component instanceof SplitPane) {
                properties.put(propertyName, Double.toString(((SplitPane) component).getDividerPositions()[0]));
            }
            // else if(component instanceof Control) {
            // if(propertyName.toLowerCase().indexOf("width") != -1) {
            // properties.put(propertyName, Double.toString(((Control)
            // component).getWidth()));
            // }
            // else {
            // properties.put(propertyName, Double.toString(((Control)
            // component).getHeight()));
            // }
            // }
            else if (component instanceof Stage) {
                if (propertyName.toLowerCase().indexOf("width") != -1) {
                    properties.put(propertyName, Double.toString(((Stage) component).getWidth()));
                } else {
                    properties.put(propertyName, Double.toString(((Stage) component).getHeight()));
                }
            }
        }
        PropertiesUtils.saveProperties();
    }

}
