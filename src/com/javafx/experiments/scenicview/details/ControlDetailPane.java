/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.javafx.experiments.scenicview.details;

import static com.javafx.experiments.scenicview.DisplayUtils.formatSize;
import javafx.scene.Node;
import javafx.scene.control.Control;

/**
 * 
 * @author aim
 */
public class ControlDetailPane extends DetailPane {
    Detail minSizeOverrideDetail;
    Detail prefSizeOverrideDetail;
    Detail maxSizeOverrideDetail;

    @Override public Class<? extends Node> getTargetClass() {
        return Control.class;
    }

    @Override public boolean targetMatches(final Object candidate) {
        return candidate instanceof Control;
    }

    @Override protected void createDetails() {
        int row = 0;
        minSizeOverrideDetail = addDetail("minWidth", "minWidth/Height:", row++);
        prefSizeOverrideDetail = addDetail("prefWidth", "prefWidth/Height:", row++);
        maxSizeOverrideDetail = addDetail("prefWidth", "maxWidth/Height:", row++);
    }

    @Override protected void updateAllDetails() {
        updateDetail("*");
    }

    @Override protected void updateDetail(final String propertyName) {
        final boolean all = propertyName.equals("*") ? true : false;

        final Control control = (Control) getTarget();
        if (all || propertyName.equals("minWidth") || propertyName.equals("minHeight")) {
            if (control != null) {
                final double minw = control.getMinWidth();
                final double minh = control.getMinHeight();
                minSizeOverrideDetail.valueLabel.setText(formatSize(minw) + " x " + formatSize(minh));
                minSizeOverrideDetail.setIsDefault(minw == Control.USE_COMPUTED_SIZE && minh == Control.USE_COMPUTED_SIZE);
                minSizeOverrideDetail.setSimpleSizeProperty(control.minWidthProperty(), control.minHeightProperty());
            } else {
                minSizeOverrideDetail.valueLabel.setText("-");
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
                prefSizeOverrideDetail.valueLabel.setText(formatSize(prefw) + " x " + formatSize(prefh));
                prefSizeOverrideDetail.setIsDefault(prefw == Control.USE_COMPUTED_SIZE && prefh == Control.USE_COMPUTED_SIZE);
                prefSizeOverrideDetail.setSimpleSizeProperty(control.prefWidthProperty(), control.prefHeightProperty());
            } else {
                prefSizeOverrideDetail.valueLabel.setText("-");
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
                maxSizeOverrideDetail.valueLabel.setText(formatSize(maxw) + " x " + formatSize(maxh));
                maxSizeOverrideDetail.setIsDefault(maxw == Control.USE_COMPUTED_SIZE && maxh == Control.USE_COMPUTED_SIZE);
                maxSizeOverrideDetail.setSimpleSizeProperty(control.maxWidthProperty(), control.maxHeightProperty());
            } else {
                maxSizeOverrideDetail.valueLabel.setText("-");
                maxSizeOverrideDetail.setIsDefault(true);
                maxSizeOverrideDetail.setSimpleSizeProperty(null, null);
            }
            if (!all)
                maxSizeOverrideDetail.updated();
        }
    }

}
