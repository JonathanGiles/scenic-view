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
package org.scenicview.control;

import javafx.beans.binding.*;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

import org.scenicview.DisplayUtils;

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
