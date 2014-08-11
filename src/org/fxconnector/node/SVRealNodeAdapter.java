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
package org.fxconnector.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Accordion;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;

import org.fxconnector.ConnectorUtils;

class SVRealNodeAdapter extends SVNodeImpl implements SVNode {

    /**
     * 
     */
    private static final long serialVersionUID = 4958550915826454155L;
    private final Node node;
    private final boolean collapseControls;
    private final boolean collapseContentControls;

    public SVRealNodeAdapter(final Node node) {
        this(node, true, true);
    }

    public SVRealNodeAdapter(final Node node, final boolean collapseControls, final boolean collapseContentControls) {
        super(ConnectorUtils.nodeClass(node), node.getClass().getName());
        this.node = node;
        this.collapseControls = collapseControls;
        this.collapseContentControls = collapseContentControls;
        boolean mustBeExpanded = !(node instanceof Control) || !collapseControls;
        if (!mustBeExpanded && !collapseContentControls) {
            mustBeExpanded = node instanceof TabPane || node instanceof SplitPane || node instanceof ScrollPane || node instanceof Accordion || node instanceof TitledPane;
        }
        setExpanded(mustBeExpanded);
    }

    @Override public String getId() {
        return node.getId();
    }

    @Override public SVNode getParent() {
        if (node.getParent() != null) {
            /**
             * This should be improved
             */
            return new SVRealNodeAdapter(node.getParent(), collapseControls, collapseContentControls);
        }
        return null;
    }

    @Override public List<SVNode> getChildren() {
        /**
         * This should be improved
         */
        if (node instanceof Parent) {
            final List<SVNode> nodes = new ArrayList<SVNode>();
            final List<Node> realNodes = ((Parent) node).getChildrenUnmodifiable();
            for (int i = 0; i < realNodes.size(); i++) {
                nodes.add(new SVRealNodeAdapter(realNodes.get(i), collapseControls, collapseContentControls));
            }
            return nodes;
        }
        return Collections.emptyList();
    }

    /**
     * This must be removed in the future
     */
    @Override public boolean equals(final Object node) {
        if (node instanceof SVNode) {
            return equals((SVNode) node);
        } else {
            return this.node == node;
        }
    }

    @Override public boolean equals(final SVNode node) {
        if (node instanceof SVDummyNode) {
            return false;
        }
        if (node instanceof SVRealNodeAdapter) {
            return node.getImpl() == this.node;
        } else {
            return node != null && getNodeId() == node.getNodeId();
        }
    }

    @Override public Node getImpl() {
        return node;
    }

    @Override public int getNodeId() {
        return ConnectorUtils.getNodeUniqueID(node);
    }

    @Override public boolean isVisible() {
        return ConnectorUtils.isNodeVisible(node);
    }

    @Override public boolean isMouseTransparent() {
        return ConnectorUtils.isMouseTransparent(node);
    }

    @Override public boolean isFocused() {
        return node.isFocused();
    }

    @Override public String toString() {
        return ConnectorUtils.nodeDetail(this, showID);
    }

    @Override public String getExtendedId() {
        return ConnectorUtils.nodeDetail(this, true);
    }

    @Override public boolean isRealNode() {
        return true;
    }

    @Override public int hashCode() {
        return getNodeId();
    }

    @Override public NodeType getNodeType() {
        return NodeType.REAL_NODE;
    }
}
