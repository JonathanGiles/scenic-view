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

import javafx.scene.Node;
import javafx.scene.shape.Shape;

import org.fxconnector.StageID;
import org.fxconnector.details.Detail.ValueType;
import org.fxconnector.event.FXConnectorEventDispatcher;

/**
 * 
 */
class ShapeDetailPaneInfo extends DetailPaneInfo {

    Detail fillDetail;

    ShapeDetailPaneInfo(final FXConnectorEventDispatcher dispatcher, final StageID stageID) {
        super(dispatcher, stageID, DetailPaneType.SHAPE);
        System.out.println("Booya");
    }

    @Override Class<? extends Node> getTargetClass() {
        return Shape.class;
    }

    @Override public boolean targetMatches(final Object candidate) {
        System.out.println("Candidate: " + candidate);
        return candidate instanceof Shape;
    }

    @Override protected void createDetails() {
        fillDetail = addDetail("fill", "fill:", ValueType.COLOR);
    }

    @Override protected void updateDetail(final String propertyName) {
        final boolean all = propertyName.equals("*") ? true : false;

        final Shape shapeNode = (Shape) getTarget();
        if (all || propertyName.equals("fill")) {
            fillDetail.setValue(shapeNode != null ? shapeNode.getFill().toString() : "-");
            fillDetail.setIsDefault(shapeNode == null || shapeNode.getFill() == null);
            fillDetail.setSimpleProperty((shapeNode != null) ? shapeNode.fillProperty() : null);
            if (!all)
                fillDetail.updated();
            if (!all)
                return;
        }
        if (all)
            sendAllDetails();
    }
}
