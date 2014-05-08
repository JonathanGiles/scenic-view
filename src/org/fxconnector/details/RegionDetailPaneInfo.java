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

import org.fxconnector.*;
import org.fxconnector.details.Detail.ValueType;
import org.fxconnector.event.FXConnectorEventDispatcher;

import org.fxconnector.StageID;
import org.fxconnector.ConnectorUtils;
import javafx.scene.Node;
import javafx.scene.layout.Region;

import org.fxconnector.details.Detail.ValueType;
import org.fxconnector.event.FXConnectorEventDispatcher;

/**
 * 
 */
class RegionDetailPaneInfo extends DetailPaneInfo {
    RegionDetailPaneInfo(final FXConnectorEventDispatcher dispatcher, final StageID stageID) {
        super(dispatcher, stageID, DetailPaneType.REGION);
    }

    Detail snapToPixelDetail;
    Detail insetsDetail;
    Detail paddingDetail;
    Detail minSizeOverrideDetail;
    Detail prefSizeOverrideDetail;
    Detail maxSizeOverrideDetail;

    @Override Class<? extends Node> getTargetClass() {
        return Region.class;
    }

    @Override public boolean targetMatches(final Object candidate) {
        return candidate instanceof Region;
    }

    @Override protected void createDetails() {
        snapToPixelDetail = addDetail("snapToPixel", "snapToPixel:");
        paddingDetail = addDetail("padding", "padding:", ValueType.INSETS);
        insetsDetail = addDetail("insets", "insets:\n(includes padding)", ValueType.INSETS);
        // insetsDetail.label.setTextAlignment(TextAlignment.RIGHT);
        minSizeOverrideDetail = addDetail("minWidth", "minWidth/Height:");
        prefSizeOverrideDetail = addDetail("prefWidth", "prefWidth/Height:");
        maxSizeOverrideDetail = addDetail("maxWidth ", "maxWidth/Height:");
    }

    @Override protected void updateDetail(final String propertyName) {
        final boolean all = propertyName.equals("*") ? true : false;

        final Region region = (Region) getTarget();
        if (all || propertyName.equals("snapToPixel")) {
            snapToPixelDetail.setValue(region != null ? Boolean.toString(region.isSnapToPixel()) : "-");
            snapToPixelDetail.setIsDefault(region == null || region.isSnapToPixel());
            snapToPixelDetail.setSimpleProperty(region != null ? region.snapToPixelProperty() : null);
            if (!all)
                snapToPixelDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("insets")) {
            insetsDetail.setValue(region != null ? ConnectorUtils.serializeInsets(region.getInsets()) : null);
            insetsDetail.setIsDefault(region == null);
            if (!all)
                insetsDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("padding")) {
            paddingDetail.setValue(region != null ? ConnectorUtils.serializeInsets(region.getPadding()) : null);
            paddingDetail.setIsDefault(region == null);
            if (!all)
                paddingDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("minWidth") || propertyName.equals("minHeight")) {
            if (region != null) {
                final double minw = region.getMinWidth();
                final double minh = region.getMinHeight();
                minSizeOverrideDetail.setValue(ConnectorUtils.formatSize(minw) + " x " + ConnectorUtils.formatSize(minh));
                minSizeOverrideDetail.setIsDefault(minw == Region.USE_COMPUTED_SIZE && minh == Region.USE_COMPUTED_SIZE);
                minSizeOverrideDetail.setSimpleSizeProperty(region.minWidthProperty(), region.minHeightProperty());
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
            if (region != null) {
                final double prefw = region.getPrefWidth();
                final double prefh = region.getPrefHeight();
                prefSizeOverrideDetail.setValue(ConnectorUtils.formatSize(prefw) + " x " + ConnectorUtils.formatSize(prefh));
                prefSizeOverrideDetail.setIsDefault(prefw == Region.USE_COMPUTED_SIZE && prefh == Region.USE_COMPUTED_SIZE);
                prefSizeOverrideDetail.setSimpleSizeProperty(region.prefWidthProperty(), region.prefHeightProperty());
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
            if (region != null) {
                final double maxw = region.getMaxWidth();
                final double maxh = region.getMaxHeight();
                maxSizeOverrideDetail.setValue(ConnectorUtils.formatSize(maxw) + " x " + ConnectorUtils.formatSize(maxh));
                maxSizeOverrideDetail.setIsDefault(maxw == Region.USE_COMPUTED_SIZE && maxh == Region.USE_COMPUTED_SIZE);
                maxSizeOverrideDetail.setSimpleSizeProperty(region.maxWidthProperty(), region.maxHeightProperty());
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
