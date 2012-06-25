package com.javafx.experiments.scenicview.connector.node;

import java.util.List;

import javafx.scene.Node;
import javafx.scene.image.Image;

public interface SVNode {

    String getId();

    String getNodeClass();

    String getExtendedId();

    SVNode getParent();

    List<SVNode> getChildren();

    boolean equals(SVNode node);

    /**
     * @deprecated
     * @return
     */
    @Deprecated Node getImpl();

    int getNodeId();

    boolean isVisible();

    boolean isMouseTransparent();

    boolean isFocused();

    boolean isRealNode();

    /**
     * I'm not sure about this three methods...
     * 
     */

    void setInvalidForFilter(boolean invalid);

    boolean isInvalidForFilter();

    void setShowId(boolean showId);

    boolean isExpanded();

    void setExpanded(boolean expanded);

    Image getIcon();

}
