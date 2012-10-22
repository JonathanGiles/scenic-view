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
package com.javafx.experiments.scenicview.connector.node;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import javafx.scene.Node;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;

public class SVDummyNode extends SVNodeImpl implements SVNode, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5879997163440845764L;
    private String name;
    private final List<SVNode> childrens = new ArrayList<SVNode>();
    private transient Image icon;
    private int nodeID;
    private byte[] imageInByte;
    private NodeType nodeType;

    public enum NodeType {
        VMS_ROOT, VM, STAGE, SUBWINDOWS_ROOT, SUBWINDOW
    }

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
                e.printStackTrace();
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
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("deprecation") private static javafx.scene.image.Image convertToFxImage(final java.awt.image.BufferedImage awtImage) {
        if (Image.impl_isExternalFormatSupported(BufferedImage.class)) {
            return javafx.scene.image.Image.impl_fromExternalImage(awtImage);
        } else {
            return null;
        }
    }

    @SuppressWarnings("deprecation") private static java.awt.image.BufferedImage convertToAwtImage(final javafx.scene.image.Image fxImage) {
        if (Image.impl_isExternalFormatSupported(BufferedImage.class)) {
            final java.awt.image.BufferedImage awtImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            return (BufferedImage) fxImage.impl_toExternalImage(awtImage);
        } else {
            return null;
        }
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

    public NodeType getNodeType() {
        return nodeType;
    }

}
