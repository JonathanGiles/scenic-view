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
package org.fxconnector.details;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import org.fxconnector.StageID;
import org.fxconnector.event.FXConnectorEventDispatcher;
import org.scenicview.utils.Logger;

@SuppressWarnings("rawtypes")
class FullPropertiesDetailPaneInfo extends DetailPaneInfo {

    boolean showCSSProperties = true;

    FullPropertiesDetailPaneInfo(final FXConnectorEventDispatcher dispatcher, final StageID stageID) {
        super(dispatcher, stageID, DetailPaneType.FULL);
    }

    Map<String, ObservableValue> orderedProperties;
    Map<String, Detail> fullPropertiesDetails;
    Map<WritableValue, String> styles;

    @Override protected String getPaneName() {
        return "Full Properties Details";
    }

    @Override Class<? extends Node> getTargetClass() {
        return null;
    }

    @Override public boolean targetMatches(final Object candidate) {
        return candidate != null;
    }

    @Override protected void createDetails() {
        // Nothing to do
    }

    @Override public void setTarget(final Object value) {
        if (doSetTarget(value)) {
            createPropertiesPanel();
        }

    }

    @SuppressWarnings("unchecked") private void createPropertiesPanel() {
        final Node node = (Node) getTarget();
        styles = new HashMap<>();
        details.clear();
        if (node != null) {
            final List<CssMetaData<? extends Styleable, ?>> list = node.getCssMetaData();
            for (final Iterator iterator = list.iterator(); iterator.hasNext();) {
                final CssMetaData cssMetaData = (CssMetaData) iterator.next();
                final WritableValue wvalue = cssMetaData.getStyleableProperty(node);
                styles.put(wvalue, cssMetaData.getProperty());
            }
        }

        orderedProperties = new TreeMap<>();
        fullPropertiesDetails = new HashMap<>();
        final Map<ObservableValue, String> properties = tracker.getProperties();
        for (final Iterator<ObservableValue> iterator = properties.keySet().iterator(); iterator.hasNext();) {
            final ObservableValue type = iterator.next();
            orderedProperties.put(properties.get(type), type);
        }
        for (final Iterator<String> iterator = orderedProperties.keySet().iterator(); iterator.hasNext();) {
            final String type = iterator.next();
            final String style = styles.get(orderedProperties.get(type));
            if (style == null || !showCSSProperties) {
                fullPropertiesDetails.put(type, addDetail(type, type + ":"));
            } else {
                fullPropertiesDetails.put(type, addDetail(type, type + "(" + style + "):"));
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
        sendAllDetails();
    }

    @Override protected void updateDetail(final String propertyName) {
        updateDetail(propertyName, false);
    }

    @SuppressWarnings({ "unchecked", "deprecation" }) protected void updateDetail(final String propertyName, final boolean all) {
        final Detail detail = fullPropertiesDetails.get(propertyName);
        final ObservableValue observable = orderedProperties.get(propertyName);
        final Object value = observable.getValue();
        if (value instanceof Image) {
            detail.setValue("Image (" + ((Image) value).impl_getUrl() + ")");
        } else {
            detail.setValue(value == null ? Detail.EMPTY_DETAIL : value.toString());
            detail.setDefault(value == null);
        }

        if (observable instanceof Property) {
            if (observable.getValue() instanceof Enum) {
                detail.setEnumProperty((Property) observable, (Class<? extends Enum>) observable.getValue().getClass());
            } else if (!(observable instanceof ObjectProperty)) {
                detail.setSimpleProperty((Property) observable);
            } else if (observable.getValue() instanceof Color) {
                detail.setSimpleProperty((Property) observable);
            } else {
                detail.setSimpleProperty(null);
                detail.unavailableEdition(Detail.STATUS_NOT_SUPPORTED);
            }
        } else {
            detail.setSimpleProperty(null);
            if (observable instanceof ReadOnlyProperty) {
                detail.unavailableEdition(Detail.STATUS_READ_ONLY);
            } else {
                Logger.print("Strange Property:" + observable);
            }
        }
        if (!all)
            detail.updated();
    }

    @Override void setShowCSSProperties(final boolean show) {
        showCSSProperties = show;
        if (getTarget() != null) {
            createPropertiesPanel();
        }
    }

}
