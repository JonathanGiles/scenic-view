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
package com.javafx.experiments.scenicview.connector.node;

import java.io.Serializable;

import javafx.scene.Node;
import javafx.scene.image.Image;

public abstract class SVNodeImpl implements SVNode, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3301868461718461962L;
    boolean invalidForFilter;
    boolean showID;
    boolean expanded;
    protected String nodeClass;
    protected String nodeClassName;

    protected SVNodeImpl() {

    }

    protected SVNodeImpl(final String nodeClass, final String nodeClassName) {
        this.nodeClass = nodeClass;
        this.nodeClassName = nodeClassName;
    }

    @Override public void setInvalidForFilter(final boolean invalid) {
        this.invalidForFilter = invalid;
    }

    @Override public boolean isInvalidForFilter() {
        return invalidForFilter;
    }

    @Override public void setShowId(final boolean showID) {
        this.showID = showID;
    }

    @Override public boolean isExpanded() {
        return this.expanded;
    }

    @Override public void setExpanded(final boolean expanded) {
        this.expanded = expanded;
    }

    @Override public Image getIcon() {
        return null;
    }

    @Override public String getNodeClass() {
        return nodeClass;
    }

    @Override public final String getNodeClassName() {
        return nodeClassName;
    }

    public static boolean isNodeVisible(final Node node) {
        if (node == null) {
            return true;
        } else {
            return node.isVisible() && isNodeVisible(node.getParent());
        }
    }

    public static boolean isMouseTransparent(final Node node) {
        if (node == null) {
            return false;
        } else {
            return node.isMouseTransparent() || isMouseTransparent(node.getParent());
        }
    }

}
