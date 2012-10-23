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
package com.javafx.experiments.scenicview.connector.details;

import java.lang.reflect.Method;

import javafx.beans.property.*;
import javafx.beans.value.WritableValue;
import javafx.scene.paint.Color;

@SuppressWarnings("rawtypes")
public class SimpleSerializer implements WritableValue<String> {

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
                e.printStackTrace();
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
