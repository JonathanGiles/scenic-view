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
package org.fxconnector.node;

import java.util.List;
import java.util.stream.Collectors;

import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;

import org.fxconnector.ConnectorUtils;
import org.fxconnector.helper.ChildrenGetter;

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
        return ChildrenGetter.getChildren(node)
                .stream()
                .map(childNode -> new SVRealNodeAdapter(childNode, collapseControls, collapseContentControls))
                .collect(Collectors.toList());
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
