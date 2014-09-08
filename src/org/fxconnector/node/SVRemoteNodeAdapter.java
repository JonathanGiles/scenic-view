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

import java.io.Serializable;
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

class SVRemoteNodeAdapter extends SVNodeImpl implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -5972848763525174505L;
    private String id;
    private int nodeId;
    private boolean visible;
    private boolean mouseTransparent;
    private boolean focused;
    private List<SVNode> nodes;
    private SVRemoteNodeAdapter parent;

    public SVRemoteNodeAdapter() {
        super();
    }

    public SVRemoteNodeAdapter(final Node node, final boolean collapseControls, final boolean collapseContentControls) {
        this(node, collapseControls, collapseContentControls, true, null);
    }

    public SVRemoteNodeAdapter(final Node node, final boolean collapseControls, final boolean collapseContentControls, final boolean fillChildren, final SVRemoteNodeAdapter parent) {
        super(ConnectorUtils.nodeClass(node), node.getClass().getName());
        boolean mustBeExpanded = !(node instanceof Control) || !collapseControls;
        if (!mustBeExpanded && !collapseContentControls) {
            mustBeExpanded = node instanceof TabPane || node instanceof SplitPane || node instanceof ScrollPane || node instanceof Accordion || node instanceof TitledPane;
        }
        setExpanded(mustBeExpanded);
        this.id = node.getId();
        this.nodeId = ConnectorUtils.getNodeUniqueID(node);
        this.focused = node.isFocused();
        if (node.getParent() != null && parent == null) {
            this.parent = new SVRemoteNodeAdapter(node.getParent(), collapseControls, collapseContentControls, false, null);
        } else if (parent != null) {
            this.parent = parent;
        }
        /**
         * Check visibility and mouse transparency after calculating the parent
         */
        this.mouseTransparent = node.isMouseTransparent() || (this.parent != null && this.parent.isMouseTransparent());
        this.visible = node.isVisible() && (this.parent == null || this.parent.isVisible());

        /**
         * TODO This should be improved
         */
        if (fillChildren) {
            nodes = ChildrenGetter.getChildren(node)
                      .stream()
                      .map(childNode -> new SVRemoteNodeAdapter(childNode, collapseControls, collapseContentControls, true, this))
                      .collect(Collectors.toList());
        }
    }

    @Override public String getId() {
        return id;
    }

    @Override public String getExtendedId() {
        return ConnectorUtils.nodeDetail(this, true);
    }

    @Override public SVNode getParent() {
        return parent;
    }

    @Override public List<SVNode> getChildren() {
        return nodes;
    }

    @Override public boolean equals(final SVNode node) {
        if (node instanceof SVDummyNode) {
            return false;
        }
        return node != null && node.getNodeId() == getNodeId();
    }

    /**
     * This must be removed in the future
     */
    @Override public boolean equals(final Object node) {
        if (node instanceof SVNode) {
            return equals((SVNode) node);
        } else if (node instanceof Node) {
            return getNodeId() == ConnectorUtils.getNodeUniqueID((Node) node);
        }
        return false;
    }

    @Override @Deprecated public Node getImpl() {
        return null;
    }

    @Override public int getNodeId() {
        return nodeId;
    }

    @Override public boolean isVisible() {
        return visible;
    }

    @Override public boolean isMouseTransparent() {
        return mouseTransparent;
    }

    @Override public boolean isFocused() {
        // TODO Auto-generated method stub
        return focused;
    }

    @Override public boolean isRealNode() {
        return true;
    }

    @Override public String toString() {
        return ConnectorUtils.nodeDetail(this, showID);
    }

    @Override public int hashCode() {
        return nodeId;
    }

    @Override public NodeType getNodeType() {
        return NodeType.REMOTE_NODE;
    }

}
