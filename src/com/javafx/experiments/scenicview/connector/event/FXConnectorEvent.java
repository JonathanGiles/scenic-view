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
package com.javafx.experiments.scenicview.connector.event;

import java.io.Serializable;

import com.javafx.experiments.scenicview.connector.StageID;

public class FXConnectorEvent implements Serializable {

    public enum SVEventType {
        EVENT_LOG, MOUSE_POSITION, WINDOW_DETAILS, NODE_SELECTED, NODE_ADDED, NODE_REMOVED, NODE_COUNT, SCENE_DETAILS, ROOT_UPDATED, DETAILS, DETAIL_UPDATED, ANIMATIONS_UPDATED, SHORTCUT
    }

    /**
     * 
     */
    private static final long serialVersionUID = -2556951288718105815L;

    private final SVEventType type;
    private final StageID stageID;

    public FXConnectorEvent(final SVEventType type, final StageID id) {
        this.type = type;
        this.stageID = id;
    }

    public SVEventType getType() {
        return type;
    }

    public StageID getStageID() {
        return stageID;
    }

    @Override public String toString() {
        return "AppEvent [type=" + type + ", stageID=" + stageID + "]";
    }

}
