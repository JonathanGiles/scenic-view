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
package org.scenicview.tabs.details;

import static org.scenicview.DisplayUtils.DFMT;
import javafx.geometry.*;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;

/**
 * 
 */
public class InsetsDisplay extends Region {
    private static double THICKNESS = 18;

    private Insets insets;
    private final Rectangle outsideRect;
    // private Rectangle insideRect;
    private final Text topText;
    private final Text leftText;
    private final Text bottomText;
    private final Text rightText;

    public InsetsDisplay() {
        getStyleClass().add("insets-display");

        outsideRect = new Rectangle();
        outsideRect.setFill(null);
        outsideRect.setStroke(Color.rgb(240, 240, 240));
        outsideRect.setStrokeWidth(THICKNESS);
        outsideRect.setStrokeType(StrokeType.INSIDE);

        topText = new Text("-");
        topText.setManaged(false);
        bottomText = new Text("-");
        bottomText.setManaged(false);
        rightText = new Text("-");
        rightText.setManaged(false);
        leftText = new Text("-");
        leftText.setManaged(false);

        getChildren().addAll(outsideRect, topText, bottomText, leftText, rightText);
    }

    @Override protected double computePrefWidth(final double height) {
        return THICKNESS * 3.5;
    }

    @Override protected double computePrefHeight(final double width) {
        return THICKNESS * 2.5;
    }

    @Override protected double computeMaxWidth(final double height) {
        return prefWidth(height);
    }

    @Override protected double computeMaxHeight(final double width) {
        return prefHeight(width);
    }

    @Override public double getBaselineOffset() {
        return topText.getLayoutY();
    }

    @Override protected void layoutChildren() {
        final double left = getPadding().getLeft();
        final double right = getPadding().getRight();
        final double top = getPadding().getTop();
        final double bottom = getPadding().getBottom();

        final double insideWidth = getWidth() - left - right;
        final double insideHeight = getHeight() - top - bottom;

        outsideRect.setWidth(insideWidth);
        outsideRect.setHeight(insideHeight);
        outsideRect.relocate(left, top);

        layoutInArea(topText, left, top, insideWidth, THICKNESS, 0, HPos.CENTER, VPos.CENTER);
        layoutInArea(bottomText, left, top + insideHeight - THICKNESS, insideWidth, THICKNESS, 0, HPos.CENTER, VPos.CENTER);
        layoutInArea(leftText, left, top, THICKNESS, insideHeight, 0, HPos.CENTER, VPos.CENTER);
        layoutInArea(rightText, left + insideWidth - THICKNESS, top, THICKNESS, insideHeight, 0, HPos.CENTER, VPos.CENTER);

    }

    public Insets getInsetsTarget() {
        return insets;
    }

    public void setInsetsTarget(final Insets value) {
        if ((value != null && !value.equals(insets)) || (value == null && insets != null)) {
            storeInsets(value);
        }
    }

    protected void storeInsets(final Insets value) {
        insets = value;
        topText.setText(insets != null ? DFMT.format(insets.getTop()) : "-");
        bottomText.setText(insets != null ? DFMT.format(insets.getBottom()) : "-");
        leftText.setText(insets != null ? DFMT.format(insets.getLeft()) : "-");
        rightText.setText(insets != null ? DFMT.format(insets.getRight()) : "-");
        requestLayout();
    }

}
