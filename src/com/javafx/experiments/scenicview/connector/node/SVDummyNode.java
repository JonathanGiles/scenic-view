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
            final java.awt.image.BufferedImage awtImage = new BufferedImage((int) fxImage.getWidth(), (int) fxImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            return (BufferedImage) fxImage.impl_toExternalImage(awtImage);
        } else {
            return null;
        }
    }

    @Override public int hashCode() {
        return nodeID;
    }

    @Override public boolean equals(final Object obj) {
        return equals((SVNode) obj);
    }

}
