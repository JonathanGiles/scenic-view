package com.javafx.experiments.scenicview;

import static com.javafx.experiments.scenicview.DisplayUtils.nodeClass;

import java.net.URL;
import java.util.*;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.image.Image;

// Need this to prevent the tree from using the node as the display!
class NodeData implements NodeInfo {

    private static final String CUSTOM_NODE_IMAGE = DisplayUtils.getNodeIcon("CustomNode").toString();
    private static final Map<String, Image> loadedImages = new HashMap<String, Image>();

    public Node node;
    /**
     * Flag to indicate that this node is not valid for the filters but it's
     * kept only to indicate the structure of lower accepted nodes + Node
     * (Invalid) + Node (Invalid) + Node (Valid for filter)
     */
    public boolean invalidForFilter;
    ObservableList<Node> children;
    private final boolean showID;

    public NodeData(final Node node, final boolean showId) {
        this.node = node;
        this.showID = showId;
    }

    @Override public String toString() {
        return DisplayUtils.nodeDetail(node, showID);
    }

    @Override public boolean isVisible() {
        return isNodeVisible(node);
    }

    private boolean isNodeVisible(final Node node) {
        return DisplayUtils.isNodeVisible(node);
    }

    @Override public boolean isMouseTransparent() {
        return isMouseTransparent(node);
    }

    private boolean isMouseTransparent(final Node node) {
        if (node == null) {
            return false;
        } else {
            return node.isMouseTransparent() || isMouseTransparent(node.getParent());
        }
    }

    Image getIcon() {
        final URL resource = DisplayUtils.getNodeIcon(nodeClass(node));
        String url;
        if (resource != null) {
            url = resource.toString();
        } else {
            url = CUSTOM_NODE_IMAGE;
        }
        Image image = loadedImages.get(url);
        if (image == null) {
            image = new Image(url);
            loadedImages.put(url, image);
        }
        return image;
    }

    @Override public Node getNode() {
        return node;
    }

    @Override public boolean isInvalidForFilter() {
        return invalidForFilter;
    }
}