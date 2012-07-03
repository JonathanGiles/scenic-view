package com.javafx.experiments.scenicview.connector.details;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.*;

import javafx.beans.property.*;
import javafx.beans.value.WritableValue;
import javafx.collections.ObservableList;
import javafx.scene.layout.Region;

import com.javafx.experiments.scenicview.connector.StageID;
import com.javafx.experiments.scenicview.connector.event.AppEvent.SVEventType;
import com.javafx.experiments.scenicview.connector.event.*;
import com.javafx.experiments.scenicview.details.GDetailPane;

public class Detail implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 835749512117709621L;

    public enum LabelType {
        NORMAL, LAYOUT_BOUNDS, BOUNDS_PARENT, BASELINE
    }

    public enum ValueType {
        NORMAL, INSETS, CONSTRAINTS, GRID_CONSTRAINTS
    };

    public enum EditionType {
        NONE, TEXT, COMBO, SLIDER
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

    private transient final AppEventDispatcher dispatcher;
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
    private final List<GridConstraintsDetail> gridConstraintsDetails = new ArrayList<GridConstraintsDetail>();

    public Detail(final AppEventDispatcher dispatcher, final StageID stageID, final DetailPaneType detailType, final int detailID) {
        this.dispatcher = dispatcher;
        this.stageID = stageID;
        this.detailType = detailType;
        this.detailID = detailID;
        this.details = new ArrayList<Detail>(1);
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
                unavailableEdition(GDetailPane.STATUS_BOUND);
            } else {
                setSerializer(new SizeSerializer(x, y));
            }
        } else {
            setReason(GDetailPane.STATUS_NOT_SUPPORTED);
            setSerializer(null);
        }
    }

    public void setSerializer(final WritableValue<String> serializer) {
        this.serializer = serializer;
        this.editionType = EditionType.NONE;
        if (serializer != null) {
            realValue = serializer.getValue();
            // Probably this should be an interface...
            if (serializer instanceof SimpleSerializer) {
                final com.javafx.experiments.scenicview.connector.details.SimpleSerializer.EditionType type = ((SimpleSerializer) serializer).getEditionType();
                switch (type) {
                case COMBO:
                    editionType = EditionType.COMBO;
                    validItems = ((SimpleSerializer) serializer).getValidValues();

                    break;
                case SLIDER:
                    editionType = EditionType.SLIDER;
                    maxValue = ((SimpleSerializer) serializer).getMaxValue();
                    minValue = ((SimpleSerializer) serializer).getMinValue();

                    break;
                default:
                    editionType = EditionType.TEXT;
                    break;
                }

            } else {
                editionType = EditionType.TEXT;

            }
        } else {
            editionType = EditionType.NONE;
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
                unavailableEdition(GDetailPane.STATUS_BOUND);
            } else {
                final SimpleSerializer s = new SimpleSerializer(property);
                s.setEnumClass(enumClass);
                setSerializer(s);
            }
        } else {
            unavailableEdition(GDetailPane.STATUS_NOT_SUPPORTED);
        }
    }

    void unavailableEdition(final String reason) {
        setReason(reason);
        setSerializer(null);
    }

    public void setConstraints(final ObservableList rowCol) {
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

}
