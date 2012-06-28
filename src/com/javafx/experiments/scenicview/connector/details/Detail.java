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
    private String label;
    private String value;
    private String reason;
    private final LabelType labelType = LabelType.NORMAL;
    private final ValueType valueType = ValueType.NORMAL;
    public SimpleSerializer serializer;

    public Detail() {
        // TODO Auto-generated constructor stub
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
        // this.serializer = serializer;
        // if (serializer != null && valueLabel != null) {
        // final ImageView graphic = new ImageView(DetailPane.EDIT_IMAGE);
        // valueLabel.setGraphic(graphic);
        // }

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

}
