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

import org.fxconnector.StageID;
import org.fxconnector.event.FXConnectorEventDispatcher;

public class AllDetails {

    private Node target;

    final DetailPaneInfo[] details;

    public AllDetails(final FXConnectorEventDispatcher dispatcher, final StageID stageID) {
        details = new DetailPaneInfo[] { 
            new NodeDetailPaneInfo(dispatcher, stageID),
            new ShapeDetailPaneInfo(dispatcher, stageID),
            new ParentDetailPaneInfo(dispatcher, stageID), 
            new RegionDetailPaneInfo(dispatcher, stageID), 
            new GridPaneDetailPaneInfo(dispatcher, stageID), 
            new ControlDetailPaneInfo(dispatcher, stageID), 
            new TextDetailPaneInfo(dispatcher, stageID), 
            new LabeledDetailPaneInfo(dispatcher, stageID), 
            new FullPropertiesDetailPaneInfo(dispatcher, stageID)
        };
    }

    public void setTarget(final Node value) {
        if (target == value)
            return;

        target = value;
        updatePanes();
    }

    public void setShowCSSProperties(final boolean show) {
        for (final DetailPaneInfo detail : details) {
            detail.setShowCSSProperties(show);
        }

    }

    private void updatePanes() {
        for (final DetailPaneInfo detail : details) {
            if (detail.targetMatches(target)) {
                detail.setTarget(target);
            } else {
                detail.clear();
            }
        }
    }

    public void setDetail(final DetailPaneType detailType, final int detailID, final String value) {
        for (final DetailPaneInfo detail : details) {
            if (detail.getType() == detailType) {
                detail.setDetail(detailID, value);
                break;
            }
        }
    }
}
