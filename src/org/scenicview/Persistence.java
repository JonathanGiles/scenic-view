/*
 * Copyright (c) 2012 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
