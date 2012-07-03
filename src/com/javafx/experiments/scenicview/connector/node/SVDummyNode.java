package com.javafx.experiments.scenicview.connector.node;

import java.io.Serializable;
import java.util.*;

import javafx.scene.Node;
import javafx.scene.image.Image;

public class SVDummyNode extends SVNodeImpl implements SVNode, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5879997163440845764L;
    private String name;
    private final List<SVNode> childrens = new ArrayList<SVNode>();
    private transient Image icon;
    private int nodeID;

    public SVDummyNode() {
        super();
    }

    public SVDummyNode(final String name, final String nodeClass, final int nodeID) {
        super(nodeClass, null);
        this.name = name;
        this.nodeID = nodeID;
    }

    @Override public String getId() {
        return name;
    }

    @Override public String getExtendedId() {
        return name;
    }

    @Override public SVNode getParent() {
        return null;
    }

    @Override public List<SVNode> getChildren() {
        return childrens;
    }

    @Override public boolean equals(final SVNode node) {
        /**
         * Only equal to another dummyNode
         */
        if (node instanceof SVDummyNode) {
            return getNodeId() == node.getNodeId();
        }
        return false;
    }

    @Override @Deprecated public Node getImpl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override public int getNodeId() {
        return nodeID;
    }

    @Override public boolean isVisible() {
        return true;
    }

    @Override public boolean isMouseTransparent() {
        return false;
    }

    @Override public boolean isFocused() {
        return false;
    }

    @Override public String toString() {
        return name;
    }

    @Override public boolean isRealNode() {
        return false;
    }

    @Override public boolean isExpanded() {
        return true;
    }

    @Override public Image getIcon() {
        return icon;
    }

    public void setIcon(final Image icon) {
        this.icon = icon;
    }

    @Override public int hashCode() {
        return nodeID;
    }

    @Override public boolean equals(final Object obj) {
        return equals((SVNode) obj);
    }
}
