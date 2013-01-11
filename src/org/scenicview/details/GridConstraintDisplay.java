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

import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

class GridConstraintDisplay extends GridPane {
    protected Node[] labels = { new Label("index"), new Label("min"), new Label("pref"), new Label("max"), new Label("percent"), new Label("grow"), new Label("alignment"), new Label("fill") };

    public GridConstraintDisplay() {
        getStyleClass().add("gridpane-constraint-display");
        setHgap(4);
        setVgap(1);
        setSnapToPixel(true);
        final ColumnConstraints cc = new ColumnConstraints();
        cc.setHalignment(HPos.CENTER);
        for (int i = 0; i < 8; i++) {
            getColumnConstraints().add(cc);
        }
    }

    protected void setConstraints(final boolean hasConstraints) {
        final Node children[] = getChildren().toArray(new Node[0]);
        for (final Node child : children) {
            GridPane.clearConstraints(child);
            getChildren().remove(child);
        }
        if (!hasConstraints) {
            addRow(0, new Label("no constraints set"));
        } else {
            addRow(0, labels);
        }
    }

    protected void addObject(final Object v, final int rowIndex, final int colIndex) {
        add(new Text(v != null ? v.toString() : "-"), colIndex, rowIndex);
    }

}