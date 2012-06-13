package com.javafx.experiments.scenicview;

import java.lang.reflect.Method;
import java.util.*;

import javafx.beans.*;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;

public abstract class PropertyTracker {

    @SuppressWarnings("rawtypes") Map<ObservableValue, String> properties = new HashMap<ObservableValue, String>();
    private final InvalidationListener propListener;
    
    public PropertyTracker() {
        propListener = new InvalidationListener() {
            @Override public void invalidated(final Observable property) {
                updateDetail(properties.get(property), (ObservableValue)property);
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
                        // System.out.println("propertyName="+propertyName+".");
                        properties.put(property, propertyName);
                    }
                } catch (final Exception e) {
                    System.err.println("Failed to get property " + method.getName());
                    e.printStackTrace();
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
