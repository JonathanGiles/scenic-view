/*
 * Scenic View, 
 * Copyright (C) 2012 Jonathan Giles, Ander Ruiz, Amy Fowler 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fxconnector.details;

import javafx.scene.Node;
import javafx.scene.control.Control;

import org.fxconnector.ConnectorUtils;
import org.fxconnector.StageID;
import org.fxconnector.event.FXConnectorEventDispatcher;

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
