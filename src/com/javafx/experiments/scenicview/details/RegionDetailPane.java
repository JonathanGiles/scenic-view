/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.javafx.experiments.scenicview.details;

import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.text.TextAlignment;

import com.javafx.experiments.scenicview.connector.ConnectorUtils;

/**
 * 
 * @author aim
 */
public class RegionDetailPane extends DetailPane {
    Detail snapToPixelDetail;
    Detail insetsDetail;
    Detail paddingDetail;
    Detail minSizeOverrideDetail;
    Detail prefSizeOverrideDetail;
    Detail maxSizeOverrideDetail;

    @Override public Class<? extends Node> getTargetClass() {
        return Region.class;
    }

    @Override public boolean targetMatches(final Object candidate) {
        return candidate instanceof Region;
    }

    @Override protected void createDetails() {
        int row = 0;
        snapToPixelDetail = addDetail("snapToPixel", "snapToPixel:", row++);
        paddingDetail = addDetail("padding", "padding:", new InsetsDisplay(), row++);
        insetsDetail = addDetail("insets", "insets:\n(includes padding)", new InsetsDisplay(), row++);
        insetsDetail.label.setTextAlignment(TextAlignment.RIGHT);
        minSizeOverrideDetail = addDetail("minWidth", "minWidth/Height:", row++);
        prefSizeOverrideDetail = addDetail("prefWidth", "prefWidth/Height:", row++);
        maxSizeOverrideDetail = addDetail("maxWidth ", "maxWidth/Height:", row++);
    }

    @Override protected void updateAllDetails() {
        updateDetail("*");
    }

    @Override protected void updateDetail(final String propertyName) {
        final boolean all = propertyName.equals("*") ? true : false;

        final Region region = (Region) getTarget();
        if (all || propertyName.equals("snapToPixel")) {
            snapToPixelDetail.valueLabel.setText(region != null ? Boolean.toString(region.isSnapToPixel()) : "-");
            snapToPixelDetail.setIsDefault(region == null || region.isSnapToPixel());
            snapToPixelDetail.setSimpleProperty(region != null ? region.snapToPixelProperty() : null);
            if (!all)
                snapToPixelDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("insets")) {
            ((InsetsDisplay) insetsDetail.valueNode).setInsetsTarget(region != null ? region.getInsets() : null);
            insetsDetail.setIsDefault(region == null);
            if (!all)
                insetsDetail.updated();
            if (!all)
                return;
        }
        if (all || propertyName.equals("padding")) {
            ((InsetsDisplay) paddingDetail.valueNode).setInsetsTarget(region != null ? region.getPadding() : null);
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
                minSizeOverrideDetail.valueLabel.setText(ConnectorUtils.formatSize(minw) + " x " + ConnectorUtils.formatSize(minh));
                minSizeOverrideDetail.setIsDefault(minw == Region.USE_COMPUTED_SIZE && minh == Region.USE_COMPUTED_SIZE);
                minSizeOverrideDetail.setSimpleSizeProperty(region.minWidthProperty(), region.minHeightProperty());
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
            if (region != null) {
                final double prefw = region.getPrefWidth();
                final double prefh = region.getPrefHeight();
                prefSizeOverrideDetail.valueLabel.setText(ConnectorUtils.formatSize(prefw) + " x " + ConnectorUtils.formatSize(prefh));
                prefSizeOverrideDetail.setIsDefault(prefw == Region.USE_COMPUTED_SIZE && prefh == Region.USE_COMPUTED_SIZE);
                prefSizeOverrideDetail.setSimpleSizeProperty(region.prefWidthProperty(), region.prefHeightProperty());
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
            if (region != null) {
                final double maxw = region.getMaxWidth();
                final double maxh = region.getMaxHeight();
                maxSizeOverrideDetail.valueLabel.setText(ConnectorUtils.formatSize(maxw) + " x " + ConnectorUtils.formatSize(maxh));
                maxSizeOverrideDetail.setIsDefault(maxw == Region.USE_COMPUTED_SIZE && maxh == Region.USE_COMPUTED_SIZE);
                maxSizeOverrideDetail.setSimpleSizeProperty(region.maxWidthProperty(), region.maxHeightProperty());
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
