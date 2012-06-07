package com.javafx.experiments.scenicview;

import java.lang.reflect.Method;

import javafx.beans.property.*;
import javafx.beans.value.WritableValue;
import javafx.scene.paint.Color;

@SuppressWarnings("rawtypes")
public class SimpleSerializer implements WritableValue<String> {

    enum EditionType {
        TEXT_FIELD, COMBO, SLIDER
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
        } else {
            editionType = EditionType.TEXT_FIELD;
        }
    }

    public void setEnumClass(final Class<? extends Enum> enumClass) {
        this.enumClass = enumClass;
        if(enumClass != null) {
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
                }
                else if(property.getValue() instanceof Color) {
                    property.setValue(Color.valueOf(value));
                }
                else {
                    ScenicView.setStatusText(DetailPane.STATUS_NOT_SUPPORTED, 10000);
                }
            }
        } catch (final Exception e) {
            ScenicView.setStatusText(DetailPane.STATUS_EXCEPTION + e.getMessage(), 10000);
            e.printStackTrace();
        }
    }

}
