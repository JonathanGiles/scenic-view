package com.javafx.experiments.scenicview.connector.details;

import java.text.DecimalFormat;

import javafx.beans.value.ObservableValue;
import javafx.scene.Node;

import com.javafx.experiments.scenicview.connector.PropertyTracker;
import com.javafx.experiments.scenicview.connector.details.Detail.LabelType;
import com.javafx.experiments.scenicview.connector.details.Detail.ValueType;

abstract class DetailPaneInfo {

    private Object target;
    public static DecimalFormat f = new DecimalFormat("0.0#");

    PropertyTracker tracker = new PropertyTracker() {

        @Override protected void updateDetail(final String propertyName, @SuppressWarnings("rawtypes") final ObservableValue property) {
            DetailPaneInfo.this.updateDetail(propertyName);
        }

    };

    public DetailPaneInfo() {
        // TODO Auto-generated constructor stub
    }

    abstract boolean targetMatches(Object target);

    public void setTarget(final Object value) {
        if (doSetTarget(value)) {
            updateAllDetails();
        }
    }

    protected boolean doSetTarget(final Object value) {
        if (target == value)
            return false;

        final Object old = target;
        if (old != null) {
            tracker.clear();
        }
        target = value;
        if (target != null) {
            tracker.setTarget(target);
        }
        return true;
    }

    public Object getTarget() {
        return target;
    }

    void setShowCSSProperties(final boolean show) {
    }

    protected String getPaneName() {
        return getTargetClass().getSimpleName() + " Details";
    }

    public abstract Class<? extends Node> getTargetClass();

    protected Detail addDetail(final String property, final String label) {
        return addDetail(property, label, ValueType.NORMAL);
    }

    protected Detail addDetail(final String property, final String label, final ValueType type) {
        final Detail detail = new Detail();
        detail.setProperty(property);
        detail.setLabel(label);
        detail.setValueType(type);
        return detail;
    }

    protected Detail addDetail(final String property, final String label, final LabelType type) {
        final Detail detail = new Detail();
        detail.setProperty(property);
        detail.setLabel(label);
        detail.setLabelType(type);
        return detail;
    }

    protected abstract void updateAllDetails();

    protected abstract void updateDetail(final String propertyName);

    protected abstract void createDetails();
}
