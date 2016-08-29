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

import java.lang.reflect.Method;

import org.scenicview.utils.ExceptionLogger;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.beans.value.WritableValue;
import javafx.scene.paint.Color;

@SuppressWarnings("rawtypes")
class SimpleSerializer implements WritableValue<String> {

    enum EditionType {
        TEXT_FIELD, COMBO, SLIDER, COLOR_PICKER
    };

    private final Property property;
    private Class<? extends Enum> enumClass;
    private EditionType editionType;
    /**
     * This is getting a bit messy, it works for now but...
     */
    private double minValue;
    private double maxValue;

    public SimpleSerializer(final Property property) {
        this.property = property;
        if (property instanceof BooleanProperty) {
            editionType = EditionType.COMBO;
        } else if (property instanceof ObjectProperty && property.getValue() instanceof Color) {
            editionType = EditionType.COLOR_PICKER;
        } else {
            editionType = EditionType.TEXT_FIELD;
        }
    }

    public void setEnumClass(final Class<? extends Enum> enumClass) {
        this.enumClass = enumClass;
        if (enumClass != null) {
            editionType = EditionType.COMBO;
        }
    }

    public EditionType getEditionType() {
        return editionType;
    }

    @Override public String getValue() {
        if (property.getValue() != null) {
            return property.getValue().toString();
        }
        return "";
    }

    public String[] getValidValues() {
        if (enumClass != null) {
            try {
                final Method m = enumClass.getMethod("values");
                final Object[] values = (Object[]) m.invoke(null, (Object[]) null);
                final String[] sValues = new String[values.length];
                for (int i = 0; i < sValues.length; i++) {
                    sValues[i] = values[i].toString();
                }
                return sValues;
            } catch (final Exception e) {
                ExceptionLogger.submitException(e);
                return null;
            }
        } else if (property instanceof BooleanProperty) {
            return new String[] { "true", "false" };
        }
        return null;
    }

    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(final double minValue) {
        this.minValue = minValue;
        editionType = EditionType.SLIDER;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(final double maxValue) {
        this.maxValue = maxValue;
        editionType = EditionType.SLIDER;
    }

    @SuppressWarnings("unchecked") @Override public void setValue(final String value) {
        try {
            if (property instanceof BooleanProperty) {
                property.setValue(Boolean.parseBoolean(value));
            } else if (property instanceof IntegerProperty) {
                property.setValue(Integer.parseInt(value));
            } else if (property instanceof DoubleProperty) {
                property.setValue(Double.parseDouble(value));
            } else if (property instanceof StringProperty) {
                property.setValue(value);
            } else if (property instanceof ObjectProperty) {
                if (enumClass != null) {
                    final Method m = enumClass.getMethod("valueOf", String.class);
                    property.setValue(m.invoke(null, value));
                } else if (property.getValue() instanceof Color) {
                    property.setValue(Color.valueOf(value));
                } else {
                    throw new RuntimeException(Detail.STATUS_NOT_SUPPORTED);
                }
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

}
