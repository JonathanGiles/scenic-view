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
package org.fxconnector;

import java.util.*;

import org.fxconnector.helper.ChildrenGetter;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

class SCUtils {

    private SCUtils() {
    }

    static void removeScenicViewComponents(final Node target) {
        /**
         * We should any component associated with ScenicView on close
         */
        if (target instanceof Group) {
            final List<Node> nodes = ((Group) target).getChildren();
            for (final Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
                final Node node = iterator.next();
                if (!isNormalNode(node)) {
                    iterator.remove();
                }
            }
        }
        if (target instanceof Pane) {
            final List<Node> nodes = ((Pane) target).getChildren();
            for (final Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
                final Node node = iterator.next();
                if (!isNormalNode(node)) {
                    iterator.remove();
                }
            }
        }
    }

    static void updateRect(final Parent overlayParent, final Node node, final Bounds bounds, final double tx, final double ty, final Rectangle rect) {
        final Bounds b = toSceneBounds(overlayParent, node, bounds, tx, ty);
        rect.setX(b.getMinX());
        rect.setY(b.getMinY());
        rect.setWidth(b.getMaxX() - b.getMinX());
        rect.setHeight(b.getMaxY() - b.getMinY());
    }

    static Bounds toSceneBounds(final Parent overlayParent, final Node node, final Bounds bounds, final double tx, final double ty) {
        final Parent parent = node.getParent();
        if (parent != null) {
            // need to translate position
            final Point2D pt = overlayParent.sceneToLocal(node.getParent().localToScene(bounds.getMinX(), bounds.getMinY()));
            return new BoundingBox(pt.getX() + tx, pt.getY() + ty, bounds.getWidth(), bounds.getHeight());
        } else {
            // selected node is root
            return new BoundingBox(bounds.getMinX() + tx + 1, bounds.getMinY() + ty + 1, bounds.getWidth() - 2, bounds.getHeight() - 2);
        }
    }

    static void addToNode(final Parent parent, final Node node) {
        if (parent instanceof Group) {
            ((Group) parent).getChildren().add(node);
        } else if (parent instanceof Pane) { // instanceof Pane
            ((Pane) parent).getChildren().add(node);
        } else if (parent != null) {
            addToNode(findFertileParent(parent), node);
        }
    }

    static void removeFromNode(final Parent parent, final Node node) {
        if (parent instanceof Group) {
            ((Group) parent).getChildren().remove(node);
        } else if (parent instanceof Pane) { // instanceof Pane
            ((Pane) parent).getChildren().remove(node);
        } else if (parent != null) {
            removeFromNode(findFertileParent(parent), node);
        }
    }

    static Node findNode(final Node target, final int nodeUniqueID) {
        if (!isNormalNode(target)) {
            return null;
        }
        
        final List<Node> children = ChildrenGetter.getChildren(target);
        for (int i = children.size() - 1; i >= 0; i--) {
            final Node node = children.get(i);
            final Node child = findNode(node, nodeUniqueID);
            if (child != null)
                return child;
        }
        if (ConnectorUtils.getNodeUniqueID(target) == nodeUniqueID) {
            return target;
        }

        return null;
    }

    static final boolean isNormalNode(final Node node) {
        return ConnectorUtils.isNormalNode(node);
    }

    static Node getHoveredNode(final Configuration configuration, final Node target, final double x, final double y) {
        if (!SCUtils.isNormalNode(target)) {
            return null;
        }
        
        final List<Node> childrens = ChildrenGetter.getChildren(target);
        for (int i = childrens.size() - 1; i >= 0; i--) {
            final Node node = childrens.get(i);
            final Node hovered = getHoveredNode(configuration, node, x, y);
            if (hovered != null)
                return hovered;
        }
        final Point2D localPoint = target.sceneToLocal(x, y);
        if (target.contains(localPoint) && ((!configuration.isIgnoreMouseTransparent() || !ConnectorUtils.isMouseTransparent(target)) && ConnectorUtils.isNodeVisible(target))) {
            return target;
        }

        return null;
    }

    static Parent findFertileParent(final Parent p) {
        Parent fertile = (p instanceof Group || p instanceof Pane) ? p : null;
        if (fertile == null) {
            for (final Node child : p.getChildrenUnmodifiable()) {
                if (child instanceof Parent) {
                    fertile = findFertileParent((Parent) child);
                }
            }
        }
        return fertile; // could be null!
    }

}
