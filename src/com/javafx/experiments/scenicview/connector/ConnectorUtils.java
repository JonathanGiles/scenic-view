package com.javafx.experiments.scenicview.connector;

import java.text.*;
import java.util.List;

import javafx.geometry.Bounds;
import javafx.scene.*;
import javafx.scene.layout.Region;

import com.javafx.experiments.scenicview.connector.node.SVNode;

public class ConnectorUtils {

    private static DecimalFormat DFMT = new DecimalFormat("0.0#");

    static final DecimalFormat df = new DecimalFormat("0.0");

    private ConnectorUtils() {
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

    public static String boundsToString(final double minx, final double miny, final double width, final double height) {
        return DFMT.format(minx) + " - " + DFMT.format(miny) + "  " + DFMT.format(width) + " x " + DFMT.format(height);
    }

    public static String boundsToString(final Bounds b) {
        return boundsToString(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
    }

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

    public static String formatSize(final double size) {
        if (size == Region.USE_COMPUTED_SIZE) {
            return "USE_COMPUTED_SIZE";
        } else if (size == Region.USE_PREF_SIZE) {
            return "USE_PREF_SIZE";
        }
        return DFMT.format(size);
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

}
