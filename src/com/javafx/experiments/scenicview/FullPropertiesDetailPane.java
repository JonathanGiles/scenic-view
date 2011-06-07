package com.javafx.experiments.scenicview;

import java.util.*;

import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;

import com.sun.javafx.css.StyleableProperty;

@SuppressWarnings("rawtypes")
public class FullPropertiesDetailPane extends DetailPane {

    boolean showCSSProperties = true;

    public FullPropertiesDetailPane() {
        setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override public void handle(final MouseEvent arg0) {
                ScenicView.setStatusText("CAUTION: This panel does not support fading for default values");
            }
        });
        setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override public void handle(final MouseEvent arg0) {
                ScenicView.clearStatusText();
            }
        });
    }

    Map<String, ObservableValue> orderedProperties;
    Map<String, Detail> details;
    Map<WritableValue, String> styles;

    @Override protected String getPaneName() {
        return "Full Properties Details";
    }

    @Override public Class<? extends Node> getTargetClass() {
        return null;
    }

    @Override public boolean targetMatches(final Object candidate) {
        return true;
    }

    @Override protected void createDetails() {
        // Nothing to do
    }

    @Override public void setTarget(final Object value) {
        if (doSetTarget(value)) {
            createPropertiesPanel();
        }

    }

    @SuppressWarnings({ "deprecation", "unchecked" }) private void createPropertiesPanel() {
        final Node node = (Node) getTarget();
        styles = new HashMap<WritableValue, String>();

        if(node != null) {
            final List<StyleableProperty> list = node.impl_getStyleableProperties();
            for (final Iterator iterator = list.iterator(); iterator.hasNext();) {
                final StyleableProperty styleableProperty = (StyleableProperty) iterator.next();
                final WritableValue wvalue = styleableProperty.getWritableValue(node);
                styles.put(wvalue, styleableProperty.getProperty());
            }
        }
        clearPane();
        if (showCSSProperties) {
            gridpane.getColumnConstraints().clear();
            final ColumnConstraints colInfo = new ColumnConstraints(230);
            gridpane.getColumnConstraints().addAll(colInfo, new ColumnConstraints());
        } else {
            gridpane.getColumnConstraints().clear();
            final ColumnConstraints colInfo = new ColumnConstraints(180);
            gridpane.getColumnConstraints().addAll(colInfo, new ColumnConstraints());
        }
        orderedProperties = new TreeMap<String, ObservableValue>();
        details = new HashMap<String, Detail>();
        for (final Iterator<ObservableValue> iterator = properties.keySet().iterator(); iterator.hasNext();) {
            final ObservableValue type = iterator.next();
            orderedProperties.put(properties.get(type), type);
        }
        int row = 0;
        for (final Iterator<String> iterator = orderedProperties.keySet().iterator(); iterator.hasNext();) {
            final String type = iterator.next();
            final String style = styles.get(orderedProperties.get(type));
            if (style == null || !showCSSProperties) {
                details.put(type, addDetail(type, type + ":", row++));
            } else {
                details.put(type, addDetail(type, type + "(" + style + "):", row++));
            }
        }
        updateAllDetails();
    }

    @Override protected void updateAllDetails() {
        if (orderedProperties != null) {
            for (final Iterator<String> iterator = orderedProperties.keySet().iterator(); iterator.hasNext();) {
                updateDetail(iterator.next(), true);
            }
        }
    }

    @Override protected void updateDetail(final String propertyName) {
        updateDetail(propertyName, false);
    }

    @SuppressWarnings("unchecked") protected void updateDetail(final String propertyName, final boolean all) {
        final Detail detail = details.get(propertyName);
        final ObservableValue observable = orderedProperties.get(propertyName);
        final Object value = observable.getValue();
        detail.valueLabel.setText(value == null ? "----" : value.toString());
        if (observable instanceof Property) {
            if (observable.getValue() instanceof Enum) {
                detail.setEnumProperty((Property) observable, (Class<? extends Enum>) observable.getValue().getClass());
            } else if (!(observable instanceof ObjectProperty)) {
                detail.setSimpleProperty((Property) observable);
            } else {
                detail.setSimpleProperty(null);
                detail.unavailableEdition(STATUS_NOT_SUPPORTED);
            }
        } else {
            detail.setSimpleProperty(null);
            if (observable instanceof ReadOnlyProperty) {
                detail.unavailableEdition(STATUS_READ_ONLY);
            } else {
                System.out.println("Property:" + observable);
            }
        }
        if (!all)
            detail.updated();
    }

    @Override public void setShowCSSProperties(final boolean show) {
        showCSSProperties = show;
        if (getTarget() != null) {
            createPropertiesPanel();
        }
    }

}
