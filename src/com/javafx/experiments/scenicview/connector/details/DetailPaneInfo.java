package com.javafx.experiments.scenicview.connector.details;

import java.text.DecimalFormat;
import java.util.*;

import javafx.beans.value.ObservableValue;
import javafx.scene.Node;

import com.javafx.experiments.scenicview.connector.*;
import com.javafx.experiments.scenicview.connector.details.Detail.LabelType;
import com.javafx.experiments.scenicview.connector.details.Detail.ValueType;
import com.javafx.experiments.scenicview.connector.event.AppEvent.SVEventType;
import com.javafx.experiments.scenicview.connector.event.*;

abstract class DetailPaneInfo {

    private Object target;
    static DecimalFormat f = new DecimalFormat("0.0#");

    PropertyTracker tracker = new PropertyTracker() {

        @Override protected void updateDetail(final String propertyName, @SuppressWarnings("rawtypes") final ObservableValue property) {
            DetailPaneInfo.this.updateDetail(propertyName);
        }

    };
    private final AppEventDispatcher dispatcher;
    private final DetailPaneType type;
    private int id;
    private final StageID stageID;
    protected final List<Detail> details = new ArrayList<Detail>();

    public DetailPaneInfo(final AppEventDispatcher dispatcher, final StageID stageID, final DetailPaneType type) {
        this.dispatcher = dispatcher;
        this.stageID = stageID;
        this.type = type;
        createDetails();
    }

    abstract boolean targetMatches(Object target);

    public void setTarget(final Object value) {
        if (doSetTarget(value)) {
            updateAllDetails();
        }
    }

    void clear() {
        final List<Detail> empty = Collections.emptyList();
        dispatcher.dispatchEvent(new DetailsEvent(SVEventType.DETAILS, stageID, type, getPaneName(), empty));
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
        final Detail detail = new Detail(dispatcher, stageID, this.type, id++);
        detail.setProperty(property);
        detail.setLabel(label);
        detail.setValueType(type);
        detail.setDetailName(getPaneName());
        details.add(detail);
        return detail;
    }

    protected Detail addDetail(final String property, final String label, final LabelType type) {
        final Detail detail = new Detail(dispatcher, stageID, this.type, id++);
        detail.setProperty(property);
        detail.setLabel(label);
        detail.setLabelType(type);
        detail.setDetailName(getPaneName());
        details.add(detail);
        return detail;
    }

    void sendAllDetails() {
        dispatcher.dispatchEvent(new DetailsEvent(SVEventType.DETAILS, stageID, type, getPaneName(), details));
    }

    protected void updateAllDetails() {
        updateDetail("*");
    }

    protected abstract void updateDetail(final String propertyName);

    protected abstract void createDetails();

    public DetailPaneType getType() {
        return type;
    }

    public void setDetail(final int detailID, final String value) {
        for (int i = 0; i < details.size(); i++) {
            final Detail d = details.get(i);
            if (d.getDetailID() == detailID && d.serializer != null) {
                System.out.println("Setting " + d.getLabel());
                d.serializer.setValue(value);
                break;
            }
        }
    }
}
