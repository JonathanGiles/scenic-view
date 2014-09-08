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
package org.fxconnector;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.scenicview.utils.ExceptionLogger;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;

@SuppressWarnings("rawtypes")
public abstract class PropertyTracker {

    Map<ObservableValue, String> properties = new HashMap<>();
    private final InvalidationListener propListener;

    public PropertyTracker() {
        propListener = new InvalidationListener() {
            @Override public void invalidated(final Observable property) {
                updateDetail(properties.get(property), (ObservableValue) property);
            }
        };
    }

    protected abstract void updateDetail(String string, ObservableValue property);

    public void clear() {
        for (final ObservableValue ov : properties.keySet()) {
            ov.removeListener(propListener);
        }
        properties.clear();
    }

    public void setTarget(final Object target) {
        properties.clear();
        // Using reflection, locate all properties and their corresponding
        // property references
        for (final Method method : target.getClass().getMethods()) {
            if (method.getName().endsWith("Property")) {
                try {
                    final Class returnType = method.getReturnType();
                    if (ObservableValue.class.isAssignableFrom(returnType)) {
                        // we've got a winner
                        final String propertyName = method.getName().substring(0, method.getName().lastIndexOf("Property"));
                        // Request access
                        method.setAccessible(true);
                        final ObservableValue property = (ObservableValue) method.invoke(target);
                        properties.put(property, propertyName);
                    }
                } catch (final Exception e) {
                    ExceptionLogger.submitException(e, "Failed to get property " + method.getName());
                }
            }
        }

        for (final ObservableValue ov : properties.keySet()) {
            ov.addListener(propListener);
        }
    }

    public Map<ObservableValue, String> getProperties() {
        return properties;
    }

}
