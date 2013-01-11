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
package org.scenicview.details;

import java.util.Map;

import javafx.geometry.*;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 * 
 */
public class ConstraintsDisplay extends GridPane {
    @SuppressWarnings("rawtypes") private Map propMap;

    public ConstraintsDisplay() {
        super();
        getStyleClass().add("constraints-display");
        setVgap(2);
        setHgap(4);
        setSnapToPixel(true);
        setPropertiesMap(null);
    }

    public boolean isShowingConstraints() {
        return getChildren().size() > 1;
    }

    public void setPropertiesMap(@SuppressWarnings("rawtypes") final Map value) {
        getChildren().clear();

        propMap = value;

        if (propMap != null) {
            final Object keys[] = propMap.keySet().toArray();
            int row = 0;
            for (int i = 0; i < keys.length; i++) {
                if (keys[i] instanceof String) {
                    final String propkey = (String) keys[i];
                    if (propkey.contains("pane-") || propkey.contains("box-")) {
                        final Object keyvalue = propMap.get(propkey);
                        final Label label = new Label(propkey + ":");
                        GridPane.setConstraints(label, 0, row);
                        GridPane.setValignment(label, VPos.TOP);
                        GridPane.setHalignment(label, HPos.RIGHT);
                        getChildren().add(label);
                        if (propkey.endsWith("margin")) {
                            final InsetsDisplay marginDisplay = new InsetsDisplay();
                            marginDisplay.setInsetsTarget((Insets) keyvalue);
                            GridPane.setConstraints(marginDisplay, 1, row++);
                            GridPane.setHalignment(marginDisplay, HPos.LEFT);
                            getChildren().add(marginDisplay);
                        } else {
                            final Label valueLabel = new Label(keyvalue.toString());
                            GridPane.setConstraints(valueLabel, 1, row++);
                            GridPane.setHalignment(valueLabel, HPos.LEFT);
                            getChildren().add(valueLabel);
                        }
                    }
                }
            }
        } else {
            final Text novalue = new Text("-");
            GridPane.setConstraints(novalue, 0, 0);
            getChildren().add(novalue);
        }

        // FIXME without this we have ghost text appearing where the layout
        // constraints should be
        if (getChildren().isEmpty()) {
            getChildren().add(new Rectangle(1, 1, Color.TRANSPARENT));
        }

        requestLayout();
    }
}
