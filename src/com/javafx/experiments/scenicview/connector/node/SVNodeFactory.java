package com.javafx.experiments.scenicview.connector.node;

import java.util.List;

import javafx.scene.Node;

import com.javafx.experiments.scenicview.connector.Configuration;

public class SVNodeFactory {

    private SVNodeFactory() {
        // TODO Auto-generated constructor stub
    }

    public static SVNode createNode(final Node node, final Configuration configuration, final boolean remote) {
        if (remote) {
            /**
             * This may sound strange but if the node has a parent we create an
             * SVNode for the parent a we get the correct node latter and if it
             * has not then we create a normal node
             */
            final Node parent = node.getParent();
            if (parent != null) {
                final SVRemoteNodeAdapter svparent = new SVRemoteNodeAdapter(parent, configuration.isCollapseControls(), configuration.isCollapseContentControls());
                final List<SVNode> childrens = svparent.getChildren();
                for (int i = 0; i < childrens.size(); i++) {
                    if (childrens.get(i).equals(node)) {
                        return childrens.get(i);
                    }
                }
                throw new RuntimeException("Error while creating node");

            } else {
                return new SVRemoteNodeAdapter(node, configuration.isCollapseControls(), configuration.isCollapseContentControls());
            }
        } else {
            return new SVRealNodeAdapter(node, configuration.isCollapseControls(), configuration.isCollapseContentControls());
        }
    }

}
