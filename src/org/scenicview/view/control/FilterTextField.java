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
package org.scenicview.view.control;

import javafx.beans.binding.*;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

import org.scenicview.view.DisplayUtils;

/**
 *
 */
public class FilterTextField extends Region {
    private final TextField textField;

    private final ImageView clearButton;
    private final double clearButtonWidth;
    private final double clearButtonHeight;

    public FilterTextField() {
        this.textField = new TextField();

        this.clearButton = new ImageView();
        this.clearButton.imageProperty().bind(new ObjectBinding<Image>() {
            {
                super.bind(clearButton.hoverProperty());
            }

            @Override protected Image computeValue() {
                if (clearButton.isHover()) {
                    return DisplayUtils.CLEAR_HOVER_IMAGE;
                } else {
                    return DisplayUtils.CLEAR_IMAGE;
                }
            }
        });
        this.clearButton.opacityProperty().bind(new DoubleBinding() {
            {
                super.bind(textField.textProperty());
            }

            @Override protected double computeValue() {
                if (textField.getText() == null || textField.getText().isEmpty()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });

        this.clearButtonWidth = clearButton.getImage().getWidth();
        this.clearButtonHeight = clearButton.getImage().getHeight();

        getChildren().addAll(textField, clearButton);
    }

    public void setOnButtonClick(final Runnable onButtonClick) {
        if (onButtonClick != null) {
            this.clearButton.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override public void handle(final MouseEvent t) {
                    onButtonClick.run();
                }
            });
        }
    }

    public TextField getTextField() {
        return textField;
    }

    public void setText(final String text) {
        this.textField.setText(text);
    }

    public String getText() {
        return this.textField.getText();
    }

    public void setPromptText(final String text) {
        this.textField.setPromptText(text);
    }

    @Override protected void layoutChildren() {
        textField.resize(getWidth(), getHeight());

        final double y = getHeight() / 2 - clearButtonHeight / 2;
        clearButton.resizeRelocate(getWidth() - clearButtonWidth - 5, y, clearButtonWidth, clearButtonHeight);
    }

    @Override protected double computePrefHeight(final double width) {
        return textField.prefHeight(width);
    }

    @Override protected double computePrefWidth(final double height) {
        return textField.prefWidth(height);
    }

}
