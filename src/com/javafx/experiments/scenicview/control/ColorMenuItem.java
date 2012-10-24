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
package com.javafx.experiments.scenicview.control;

import java.lang.reflect.Field;

import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.event.*;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import com.sun.javafx.scene.control.skin.CustomColorDialog;

public class ColorMenuItem extends CustomMenuItem {

    private final Rectangle rect;
    ObjectProperty<Color> color = new SimpleObjectProperty<Color>();

    public ColorMenuItem() {
        final HBox box2 = new HBox();
        box2.setSpacing(10);
        rect = new Rectangle(16, 16);
        setColor(Color.BLACK);
        final Label label = new Label("Color");
        box2.getChildren().addAll(rect, label);
        setContent(box2);
        setOnAction(new EventHandler<ActionEvent>() {

            @Override public void handle(final ActionEvent arg0) {
                final CustomColorDialog dialog = new CustomColorDialog(null);

                dialog.setCurrentColor(color.get());
                try {
                    final Field field = CustomColorDialog.class.getDeclaredField("customColorProperty");
                    field.setAccessible(true);
                    @SuppressWarnings("unchecked") final ObjectProperty<Color> color = (ObjectProperty<Color>) field.get(dialog);
                    color.addListener(new ChangeListener<Color>() {

                        @Override public void changed(final ObservableValue<? extends Color> arg0, final Color arg1, final Color newValue) {
                            setColor(newValue);
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                dialog.show(400, 300);
            }
        });
    }

    public void setColor(final Color color) {
        rect.setFill(color);
        this.color.set(color);
    }

    public Color getColor() {
        return this.color.get();
    }

    public ObjectProperty<Color> colorProperty() {
        return this.color;
    }

}
