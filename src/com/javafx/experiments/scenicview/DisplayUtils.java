/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.javafx.experiments.scenicview;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;

import javafx.geometry.Bounds;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.transform.*;

/**
 * 
 * @author aim
 */
public class DisplayUtils {
    public static DecimalFormat DFMT = new DecimalFormat("0.0#");

    static final Image CLEAR_IMAGE = getUIImage("clear.gif");
    
    static final DecimalFormat df = new DecimalFormat("0.0");
    
    public static String format(final double value) {
        return df.format(value);
    }

    public static String nodeClass(final Node node) {
        @SuppressWarnings("rawtypes") Class cls = node.getClass();
        String name = cls.getSimpleName();
        while (name.isEmpty()) {
            cls = cls.getSuperclass();
            name = cls.getSimpleName();
        }
        return name;
    }
    
    public static String nodeDetail(final Node node, final boolean showId) {
        return nodeClass(node) + ((showId && node.getId() != null) ? " \"" + node.getId() + "\"" : "");
    }
    
    public static Image getUIImage(final String image) {
        return new Image(ScenicView.class.getResource("images/ui/"+image).toString());
    }
    
    public static URL getNodeIcon(final String node) {
        return ScenicView.class.getResource("images/nodeicons/"+node+".png");
    }    
    
    public static boolean isNodeVisible(final Node node) {
        if (node == null) {
            return true;
        } else {
            return node.isVisible() && isNodeVisible(node.getParent());
        }
    }

    public static String boundsToString(final Bounds b) {
        return boundsToString(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
    }

    public static String boundsToString(final double minx, final double miny, final double width, final double height) {
        return DFMT.format(minx) + "," + DFMT.format(miny) + "  " + DFMT.format(width) + " x " + DFMT.format(height);
    }

    public static String transformToString(final Transform tx) {
        if (tx instanceof Translate) {
            final Translate tr = (Translate) tx;
            return "Translate(" + tr.getX() + "," + tr.getY() + "," + tr.getZ() + ")";
        } else if (tx instanceof Rotate) {
            final Rotate r = (Rotate) tx;
            return "Rotate(" + r.getAngle() + ")";
        } else if (tx instanceof Scale) {
            final Scale s = (Scale) tx;
            return "Scale(" + s.getX() + "x" + s.getY() + "x" + s.getZ() + ")";
        } else if (tx instanceof Shear) {
            final Shear s = (Shear) tx;
            return "Shear(" + s.getX() + "x" + s.getY() + ")";
        } else if (tx instanceof Affine) {
            // Affine a = (Affine)tx;
            return "Affine()";
        }
        return "-";
    }

    public static String formatSize(final double size) {
        if (size == Region.USE_COMPUTED_SIZE) {
            return "USE_COMPUTED_SIZE";
        } else if (size == Region.USE_PREF_SIZE) {
            return "USE_PREF_SIZE";
        }
        return DFMT.format(size);
    }

    public static int getBranchCount(final Node node) {
        int c = 1;
        if (node instanceof Parent) {
            final Parent p = (Parent) node;
            final List<Node> children = p.getChildrenUnmodifiable();
            for (int i = 0; i < children.size(); i++) {
                final Node child = children.get(i);
                c += getBranchCount(child);
            }
        }
        return c;
    }

}
