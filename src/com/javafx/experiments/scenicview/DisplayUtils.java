/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.javafx.experiments.scenicview;

import java.lang.reflect.Method;
import java.net.URL;
import java.text.*;
import java.util.*;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.transform.*;

import com.javafx.experiments.scenicview.connector.node.SVNode;

/**
 * 
 * @author aim
 */
public class DisplayUtils {

    private static final String CUSTOM_NODE_IMAGE = DisplayUtils.getNodeIcon("CustomNode").toString();
    private static final Map<String, Image> loadedImages = new HashMap<String, Image>();

    public static DecimalFormat DFMT = new DecimalFormat("0.0#");

    static final Image CLEAR_IMAGE = getUIImage("clear.gif");

    static final DecimalFormat df = new DecimalFormat("0.0");

    public static String format(final double value) {
        return df.format(value);
    }

    public static double parse(final String data) {
        try {
            return df.parse(data).doubleValue();
        } catch (final ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return 10;
        }
    }

    public static String nodeClass(final Object node) {
        @SuppressWarnings("rawtypes") Class cls = node.getClass();
        String name = cls.getSimpleName();
        while (name.isEmpty()) {
            cls = cls.getSuperclass();
            name = cls.getSimpleName();
        }
        return name;
    }

    public static String nodeDetail(final SVNode node, final boolean showId) {
        return node.getNodeClass() + ((showId && node.getId() != null) ? " \"" + node.getId() + "\"" : "");
    }

    public static Image getUIImage(final String image) {
        return new Image(ScenicView.class.getResource("images/ui/" + image).toString());
    }

    public static URL getNodeIcon(final String node) {
        return ScenicView.class.getResource("images/nodeicons/" + node + ".png");
    }

    public static Image getIcon(final SVNode svNode) {
        if (svNode.getIcon() != null)
            return svNode.getIcon();
        final URL resource = DisplayUtils.getNodeIcon(svNode.getNodeClass());
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

    public static String boundsToString(final Bounds b) {
        return boundsToString(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
    }

    public static String boundsToString(final double minx, final double miny, final double width, final double height) {
        return DFMT.format(minx) + " - " + DFMT.format(miny) + "  " + DFMT.format(width) + " x " + DFMT.format(height);
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

    @SuppressWarnings("rawtypes") public static void fillProperties(final Node target, final Map<ObservableValue, String> properties) {
        // Using reflection, locate all properties and their corresponding
        // property references
        properties.clear();
        for (final Method method : target.getClass().getMethods()) {
            if (method.getName().endsWith("Property")) {
                try {
                    final Class returnType = method.getReturnType();
                    if (ObservableValue.class.isAssignableFrom(returnType)) {
                        // we've got a winner
                        final String propertyName = method.getName().substring(0, method.getName().lastIndexOf("Property"));
                        // Request access
                        method.setAccessible(true);
                        final ObservableValue property = (ObservableValue) method.invoke(target);
                        // System.out.println("propertyName="+propertyName+".");
                        properties.put(property, propertyName);
                    }
                } catch (final Exception e) {
                    System.err.println("Failed to get property " + method.getName());
                    e.printStackTrace();
                }
            }
        }
    }

}
