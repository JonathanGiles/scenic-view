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
package com.javafx.experiments.fxconnector.details;

import javafx.scene.Node;
import javafx.scene.control.Control;

import com.javafx.experiments.fxconnector.*;
import com.javafx.experiments.fxconnector.event.FXConnectorEventDispatcher;

/**
 * 
 */
class ControlDetailPaneInfo extends DetailPaneInfo {

    ControlDetailPaneInfo(final FXConnectorEventDispatcher dispatcher, final StageID stageID) {
        super(dispatcher, stageID, DetailPaneType.CONTROL);
    }

    Detail minSizeOverrideDetail;
    Detail prefSizeOverrideDetail;
    Detail maxSizeOverrideDetail;

    @Override Class<? extends Node> getTargetClass() {
        return Control.class;
    }

    @Override public boolean targetMatches(final Object candidate) {
        return candidate instanceof Control;
    }

    @Override protected void createDetails() {
        minSizeOverrideDetail = addDetail("minWidth", "minWidth/Height:");
        prefSizeOverrideDetail = addDetail("prefWidth", "prefWidth/Height:");
        maxSizeOverrideDetail = addDetail("prefWidth", "maxWidth/Height:");
    }

    @Override protected void updateDetail(final String propertyName) {
        final boolean all = propertyName.equals("*") ? true : false;

        final Control control = (Control) getTarget();
        if (all || propertyName.equals("minWidth") || propertyName.equals("minHeight")) {
            if (control != null) {
                final double minw = control.getMinWidth();
                final double minh = control.getMinHeight();
                minSizeOverrideDetail.setValue(ConnectorUtils.formatSize(minw) + " x " + ConnectorUtils.formatSize(minh));
                minSizeOverrideDetail.setIsDefault(minw == Control.USE_COMPUTED_SIZE && minh == Control.USE_COMPUTED_SIZE);
                minSizeOverrideDetail.setSimpleSizeProperty(control.minWidthProperty(), control.minHeightProperty());
            } else {
                minSizeOverrideDetail.setValue("-");
                minSizeOverrideDetail.setIsDefault(true);
                minSizeOverrideDetail.setSimpleSizeProperty(null, null);
            }
            if (!all)
                minSizeOverrideDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("prefWidth") || propertyName.equals("prefHeight")) {
            if (control != null) {
                final double prefw = control.getPrefWidth();
                final double prefh = control.getPrefHeight();
                prefSizeOverrideDetail.setValue(ConnectorUtils.formatSize(prefw) + " x " + ConnectorUtils.formatSize(prefh));
                prefSizeOverrideDetail.setIsDefault(prefw == Control.USE_COMPUTED_SIZE && prefh == Control.USE_COMPUTED_SIZE);
                prefSizeOverrideDetail.setSimpleSizeProperty(control.prefWidthProperty(), control.prefHeightProperty());
            } else {
                prefSizeOverrideDetail.setValue("-");
                prefSizeOverrideDetail.setIsDefault(true);
                prefSizeOverrideDetail.setSimpleSizeProperty(null, null);
            }
            if (!all)
                prefSizeOverrideDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("maxWidth") || propertyName.equals("maxHeight")) {
            if (control != null) {
                final double maxw = control.getMaxWidth();
                final double maxh = control.getMaxHeight();
                maxSizeOverrideDetail.setValue(ConnectorUtils.formatSize(maxw) + " x " + ConnectorUtils.formatSize(maxh));
                maxSizeOverrideDetail.setIsDefault(maxw == Control.USE_COMPUTED_SIZE && maxh == Control.USE_COMPUTED_SIZE);
                maxSizeOverrideDetail.setSimpleSizeProperty(control.maxWidthProperty(), control.maxHeightProperty());
            } else {
                maxSizeOverrideDetail.setValue("-");
                maxSizeOverrideDetail.setIsDefault(true);
                maxSizeOverrideDetail.setSimpleSizeProperty(null, null);
            }
            if (!all)
                maxSizeOverrideDetail.updated();
        }
        if (all)
            sendAllDetails();
    }

}
