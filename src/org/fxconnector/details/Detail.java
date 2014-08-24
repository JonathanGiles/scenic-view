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
package org.fxconnector.details;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.value.WritableValue;
import javafx.collections.ObservableList;
import javafx.scene.layout.Region;

import org.fxconnector.StageID;
import org.fxconnector.event.DetailsEvent;
import org.fxconnector.event.FXConnectorEvent.SVEventType;
import org.fxconnector.event.FXConnectorEventDispatcher;

public class Detail implements Serializable {

    public static final String EMPTY_DETAIL = "---";

    /**
     * 
     */
    private static final long serialVersionUID = 835749512117709621L;

    private static final String STATUS_NOT_SET = "Value can not be changed ";
    public static final String STATUS_NOT_SUPPORTED = STATUS_NOT_SET + "(Not supported yet)";
    public static final String STATUS_BOUND = STATUS_NOT_SET + "(Bound property)";
    public static final String STATUS_EXCEPTION = STATUS_NOT_SET + "an exception has ocurred:";
    public static final String STATUS_READ_ONLY = STATUS_NOT_SET + "(Read-Only property)";

    /**
     * Represents the left-hand side of the two columns in the detail grid
     */
    public enum LabelType {
        NORMAL, LAYOUT_BOUNDS, BOUNDS_PARENT, BASELINE
    }

    /**
     * Represents the right-hand side of the two columns in the detail grid
     */
    public enum ValueType {
        NORMAL, INSETS, CONSTRAINTS, GRID_CONSTRAINTS, COLOR
    };

    public enum EditionType {
        NONE_BOUND, NONE, TEXT, COMBO, SLIDER, COLOR_PICKER
    }

    private boolean isDefault;
    private String property;
    private String label;
    private String value;
    private String reason;
    private LabelType labelType = LabelType.NORMAL;
    private ValueType valueType = ValueType.NORMAL;
    private EditionType editionType = EditionType.NONE;
    transient WritableValue<String> serializer;

    private transient final FXConnectorEventDispatcher dispatcher;
    private final DetailPaneType detailType;
    private final int detailID;
    private final StageID stageID;
    private transient final List<Detail> details;
    private static transient final DecimalFormat f = new DecimalFormat("0.0#");
    private String detailName;
    private String[] validItems;
    private double maxValue;
    private double minValue;
    private String realValue;
    private boolean hasGridConstraints;
    private final List<GridConstraintsDetail> gridConstraintsDetails = new ArrayList<>();

    public Detail(final FXConnectorEventDispatcher dispatcher, final StageID stageID, final DetailPaneType detailType, final int detailID) {
        this.dispatcher = dispatcher;
        this.stageID = stageID;
        this.detailType = detailType;
        this.detailID = detailID;
        this.details = new ArrayList<>(1);
        details.add(this);

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
        dispatcher.dispatchEvent(new DetailsEvent(SVEventType.DETAIL_UPDATED, stageID, detailType, detailName, details));
    }

    public void setSimpleSizeProperty(final DoubleProperty x, final DoubleProperty y) {
        if (x != null) {
            if (x.isBound() && y.isBound()) {
                unavailableEdition(STATUS_BOUND, EditionType.NONE_BOUND);
            } else {
                setSerializer(new SizeSerializer(x, y));
            }
        } else {
            setReason(STATUS_NOT_SUPPORTED);
            setSerializer(null);
        }
    }

    void setSerializer(final WritableValue<String> serializer) {
        setSerializer(serializer, EditionType.NONE);
    }

    void setSerializer(final WritableValue<String> serializer, final EditionType defaultEditionType) {
        this.serializer = serializer;
        this.editionType = defaultEditionType;
        if (serializer != null) {
            realValue = serializer.getValue();
            // Probably this should be an interface...
            if (serializer instanceof SimpleSerializer) {
                final org.fxconnector.details.SimpleSerializer.EditionType type = ((SimpleSerializer) serializer).getEditionType();
                switch (type) {
                    case COMBO: {
                        editionType = EditionType.COMBO;
                        validItems = ((SimpleSerializer) serializer).getValidValues();
                        break;
                    }
                    case SLIDER: {
                        editionType = EditionType.SLIDER;
                        maxValue = ((SimpleSerializer) serializer).getMaxValue();
                        minValue = ((SimpleSerializer) serializer).getMinValue();
                        break;
                    }
                    case COLOR_PICKER: {
                        valueType = ValueType.COLOR;
                        editionType = EditionType.COLOR_PICKER;
                        break;
                    }
                    default: {
                        editionType = EditionType.TEXT;
                        break;
                    }
                }
            } else {
                editionType = EditionType.TEXT;
            }
        }
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
                unavailableEdition(STATUS_BOUND, EditionType.NONE_BOUND);
            } else {
                final SimpleSerializer s = new SimpleSerializer(property);
                s.setEnumClass(enumClass);
                setSerializer(s);
            }
        } else {
            unavailableEdition(STATUS_NOT_SUPPORTED);
        }
    }

    void unavailableEdition(final String reason) {
        unavailableEdition(reason, EditionType.NONE);
    }

    void unavailableEdition(final String reason, final EditionType defaultEditionType) {
        setReason(reason);
        setSerializer(null, defaultEditionType);
    }

    public void setConstraints(@SuppressWarnings("rawtypes") final ObservableList rowCol) {
        hasGridConstraints = (rowCol != null && rowCol.size() != 0);
        gridConstraintsDetails.clear();
    }

    public void add(final String text, final int colIndex, final int rowIndex) {
        gridConstraintsDetails.add(new GridConstraintsDetail(text, colIndex, rowIndex));
    }

    public void addSize(final double v, final int rowIndex, final int colIndex) {
        add(v != Region.USE_COMPUTED_SIZE ? f.format(v) : "-", colIndex, rowIndex);
    }

    public void addObject(final Object v, final int rowIndex, final int colIndex) {
        add(v != null ? v.toString() : "-", colIndex, rowIndex);
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

    public DetailPaneType getDetailType() {
        return detailType;
    }

    public String getDetailName() {
        return detailName;
    }

    public void setDetailName(final String detailName) {
        this.detailName = detailName;
    }

    public EditionType getEditionType() {
        return editionType;
    }

    public String[] getValidItems() {
        return validItems;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public double getMinValue() {
        return minValue;
    }

    public String getRealValue() {
        return realValue;
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + detailID;
        result = prime * result + ((detailType == null) ? 0 : detailType.hashCode());
        result = prime * result + ((stageID == null) ? 0 : stageID.hashCode());
        return result;
    }

    @Override public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Detail other = (Detail) obj;
        if (detailID != other.detailID)
            return false;
        if (detailType != other.detailType)
            return false;
        if (stageID == null) {
            if (other.stageID != null)
                return false;
        } else if (!stageID.equals(other.stageID))
            return false;
        return true;
    }

    public int getDetailID() {
        return detailID;
    }

    public boolean hasGridConstraints() {
        return hasGridConstraints;
    }

    public List<GridConstraintsDetail> getGridConstraintsDetails() {
        return gridConstraintsDetails;
    }

    public static boolean isEditionSupported(final EditionType editionType) {
        return editionType != EditionType.NONE && editionType != EditionType.NONE_BOUND;
    }

}
