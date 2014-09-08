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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.scenicview.utils.ExceptionLogger;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.image.Image;

public class SVDummyNode extends SVNodeImpl implements SVNode, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5879997163440845764L;
    private String name;
    private final List<SVNode> childrens = new ArrayList<>();
    private transient Image icon;
    private int nodeID;
    private byte[] imageInByte;
    private NodeType nodeType;

    public SVDummyNode() {
        super();
    }

    public SVDummyNode(final String name, final String nodeClass, final int nodeID, final NodeType nodeType) {
        super(nodeClass, null);
        this.name = name;
        this.nodeID = nodeID;
        this.nodeType = nodeType;
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
            return nodeID == node.getNodeId() && nodeType == ((SVDummyNode) node).nodeType;
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
        if (icon == null && imageInByte != null) {
            try {
                final BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageInByte));
                icon = convertToFxImage(image);
                imageInByte = null;
            } catch (final Exception e) {
                ExceptionLogger.submitException(e);
            }
        }
        return icon;
    }

    public void setIcon(final Image icon) {
        this.icon = icon;
    }

    public void setRemote(final boolean remote) {
        if (remote && icon != null) {
            try {
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(convertToAwtImage(icon), "png", baos);
                baos.flush();
                imageInByte = baos.toByteArray();
                baos.close();
            } catch (final Exception e) {
                ExceptionLogger.submitException(e);
            }
        }
    }

    private static javafx.scene.image.Image convertToFxImage(final java.awt.image.BufferedImage awtImage) {
        return SwingFXUtils.toFXImage(awtImage, null);
    }

    private static java.awt.image.BufferedImage convertToAwtImage(final javafx.scene.image.Image fxImage) {
        return SwingFXUtils.fromFXImage(fxImage, null);
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + nodeID;
        result = prime * result + ((nodeType == null) ? 0 : nodeType.hashCode());
        return result;
    }

    @Override public boolean equals(final Object obj) {
        return equals((SVNode) obj);
    }

    @Override public NodeType getNodeType() {
        return nodeType;
    }

}
