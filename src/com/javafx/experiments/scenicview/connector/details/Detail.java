package com.javafx.experiments.scenicview.connector.details;

import java.io.Serializable;

import javafx.beans.property.*;
import javafx.beans.value.WritableValue;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;

import com.javafx.experiments.scenicview.details.DetailPane;

class Detail implements Serializable {

    public enum LabelType {
        NORMAL, LAYOUT_BOUNDS, BOUNDS_PARENT, BASELINE
    }

    public enum ValueType {
        NORMAL, INSECTS, CONSTRAINTS, GRID_CONSTRAINTS
    };

    private boolean isDefault;
    private String property;
    private String label;
    private String value;
    private String reason;
    private LabelType labelType = LabelType.NORMAL;
    private ValueType valueType = ValueType.NORMAL;
    private boolean editable;
    transient WritableValue<String> serializer;

    public Detail() {

    }

    public void setIsDefault(final boolean isDefault) {
        this.isDefault = isDefault;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public final void updated() {

    }

    public void setSimpleSizeProperty(final DoubleProperty x, final DoubleProperty y) {
        if (x != null) {
            if (x.isBound() && y.isBound()) {
                unavailableEdition(DetailPane.STATUS_BOUND);
            } else {
                setSerializer(new SizeSerializer(x, y));
            }
        } else {
            setReason(DetailPane.STATUS_NOT_SUPPORTED);
            setSerializer(null);
        }
    }

    public void setSerializer(final WritableValue<String> serializer) {
        this.serializer = serializer;
        this.editable = serializer != null;
    }

    public final void setReason(final String reason) {
        this.reason = reason;
    }

    @SuppressWarnings("rawtypes") public void setEnumProperty(final Property property, final Class<? extends Enum> enumClass) {
        setSimpleProperty(property, enumClass);
    }

    public void setSimpleProperty(@SuppressWarnings("rawtypes") final Property property) {
        setSimpleProperty(property, null);
    }

    private void setSimpleProperty(@SuppressWarnings("rawtypes") final Property property, @SuppressWarnings({ "rawtypes" }) final Class<? extends Enum> enumClass) {
        if (property != null) {
            if (property.isBound()) {
                unavailableEdition(DetailPane.STATUS_BOUND);
            } else {
                final SimpleSerializer s = new SimpleSerializer(property);
                s.setEnumClass(enumClass);
                setSerializer(s);
            }
        } else {
            unavailableEdition(DetailPane.STATUS_NOT_SUPPORTED);
        }
    }

    void unavailableEdition(final String reason) {
        setReason(reason);
        setSerializer(null);
    }

    public void setConstraints(final ObservableList rowCol) {
        // TODO Auto-generated method stub

    }

    public void setPropertiesMap(final ObservableMap map) {
        // TODO Auto-generated method stub

    }

    public void add(final Text text, final int i, final int rowIndex) {
        // TODO Auto-generated method stub

    }

    public void addSize(final double minWidth, final int rowIndex, final int i) {
        // TODO Auto-generated method stub

    }

    public void addObject(final Priority hgrow, final int rowIndex, final int i) {
        // TODO Auto-generated method stub

    }

    public void addObject(final VPos valignment, final int rowIndex, final int i) {
        // TODO Auto-generated method stub

    }

    public void addObject(final HPos halignment, final int rowIndex, final int i) {
        // TODO Auto-generated method stub

    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(final boolean isDefault) {
        this.isDefault = isDefault;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(final String property) {
        this.property = property;
    }

    public LabelType getLabelType() {
        return labelType;
    }

    public void setLabelType(final LabelType labelType) {
        this.labelType = labelType;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public void setValueType(final ValueType valueType) {
        this.valueType = valueType;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    public String getReason() {
        return reason;
    }

    public boolean isEditable() {
        return editable;
    }

}
