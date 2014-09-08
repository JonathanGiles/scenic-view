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

import javafx.scene.Node;
import javafx.scene.image.Image;

public interface SVNode {

    String getId();

    String getNodeClassName();

    String getNodeClass();

    String getExtendedId();

    SVNode getParent();

    List<SVNode> getChildren();

    boolean equals(SVNode node);

    Node getImpl();

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

    public NodeType getNodeType();

}
