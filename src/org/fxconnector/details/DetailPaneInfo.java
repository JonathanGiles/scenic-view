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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.value.ObservableValue;
import javafx.scene.Node;

import org.fxconnector.PropertyTracker;
import org.fxconnector.StageID;
import org.fxconnector.details.Detail.LabelType;
import org.fxconnector.details.Detail.ValueType;
import org.fxconnector.event.DetailsEvent;
import org.fxconnector.event.FXConnectorEvent.SVEventType;
import org.fxconnector.event.FXConnectorEventDispatcher;

abstract class DetailPaneInfo {

    private Object target;
    static DecimalFormat f = new DecimalFormat("0.0#");

    PropertyTracker tracker = new PropertyTracker() {
        @Override protected void updateDetail(final String propertyName, @SuppressWarnings("rawtypes") final ObservableValue property) {
            if (propertyName == null) return;
            DetailPaneInfo.this.updateDetail(propertyName);
        }

    };
    private final FXConnectorEventDispatcher dispatcher;
    private final DetailPaneType type;
    private int id;
    private final StageID stageID;
    protected final List<Detail> details = new ArrayList<Detail>();

    DetailPaneInfo(final FXConnectorEventDispatcher dispatcher, final StageID stageID, final DetailPaneType type) {
        this.dispatcher = dispatcher;
        this.stageID = stageID;
        this.type = type;
        createDetails();
    }

    abstract boolean targetMatches(Object target);

    void setTarget(final Object value) {
        if (doSetTarget(value)) {
            updateAllDetails();
        }
    }

    final void clear() {
        doSetTarget(null);
        final List<Detail> empty = Collections.emptyList();
        dispatcher.dispatchEvent(new DetailsEvent(SVEventType.DETAILS, stageID, type, getPaneName(), empty));
    }

    protected final boolean doSetTarget(final Object value) {
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

    final Object getTarget() {
        return target;
    }

    void setShowCSSProperties(final boolean show) {
    }

    protected String getPaneName() {
        return getTargetClass().getSimpleName() + " Details";
    }

    abstract Class<? extends Node> getTargetClass();

    protected final Detail addDetail(final String property, final String label) {
        return addDetail(property, label, ValueType.NORMAL);
    }

    protected final Detail addDetail(final String property, final String label, final ValueType type) {
        final Detail detail = new Detail(dispatcher, stageID, this.type, id++);
        detail.setProperty(property);
        detail.setLabel(label);
        detail.setValueType(type);
        detail.setDetailName(getPaneName());
        details.add(detail);
        return detail;
    }

    protected final Detail addDetail(final String property, final String label, final LabelType type) {
        final Detail detail = new Detail(dispatcher, stageID, this.type, id++);
        detail.setProperty(property);
        detail.setLabel(label);
        detail.setLabelType(type);
        detail.setDetailName(getPaneName());
        details.add(detail);
        return detail;
    }

    final void sendAllDetails() {
        dispatcher.dispatchEvent(new DetailsEvent(SVEventType.DETAILS, stageID, type, getPaneName(), details));
    }

    protected void updateAllDetails() {
        updateDetail("*");
    }

    protected abstract void updateDetail(final String propertyName);

    protected abstract void createDetails();

    final DetailPaneType getType() {
        return type;
    }

    final void setDetail(final int detailID, final String value) {
        for (int i = 0; i < details.size(); i++) {
            final Detail d = details.get(i);
            if (d.getDetailID() == detailID && d.serializer != null) {
                d.serializer.setValue(value);
                break;
            }
        }
    }
}
