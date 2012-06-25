package com.javafx.experiments.scenicview.connector;

import java.util.*;

import javafx.scene.Node;
import javafx.scene.image.Image;

public class SVDummyNode extends SVNodeImpl implements SVNode {

    private final String name;
    private final List<SVNode> childrens = new ArrayList<SVNode>();
    private Image icon;

    public SVDummyNode(final String name, final String nodeClass) {
        super(nodeClass);
        this.name = name;
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
        // TODO Auto-generated method stub
        return false;
    }

    @Override @Deprecated public Node getImpl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override public int getNodeId() {
        // TODO Auto-generated method stub
        return 0;
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

}
