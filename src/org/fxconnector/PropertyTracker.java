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
package org.fxconnector;

import org.fxconnector.remote.util.ScenicViewExceptionLogger;
import java.lang.reflect.Method;
import java.util.*;

import org.fxconnector.remote.util.ScenicViewExceptionLogger;

import javafx.beans.*;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;

@SuppressWarnings("rawtypes")
public abstract class PropertyTracker {

    Map<ObservableValue, String> properties = new HashMap<ObservableValue, String>();
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
                    ScenicViewExceptionLogger.submitException(e, "Failed to get property " + method.getName());
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
