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
package org.fxconnector;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javafx.animation.Animation;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Shear;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.fxconnector.helper.ChildrenGetter;
import org.fxconnector.node.SVNode;
import org.scenicview.utils.ExceptionLogger;

public class ConnectorUtils {

    private static DecimalFormat DFMT = new DecimalFormat("0.0#");

    static final DecimalFormat df = new DecimalFormat("0.0");

    static final Map<Class<?>, String> classNames = new ConcurrentHashMap<>();

    private ConnectorUtils() {
    }

    public static int getBranchCount(final Node node) {
        if (!isNormalNode(node))
            return 0;
        int c = 1;
        final List<Node> children = ChildrenGetter.getChildren(node);
        for (int i = 0; i < children.size(); i++) {
            final Node child = children.get(i);
            c += getBranchCount(child);
        }
        return c;
    }

    public final static boolean isNormalNode(final Node node) {
        return (node.getId() == null || !node.getId().startsWith(StageController.FX_CONNECTOR_BASE_ID));
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
            ExceptionLogger.submitException(e);
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
        final String value = classNames.get(node.getClass());
        if (value == null) {
            @SuppressWarnings("rawtypes") Class cls = node.getClass();
            String name = cls.getSimpleName();
            while (name.isEmpty()) {
                cls = cls.getSuperclass();
                name = cls.getSimpleName();
            }
            classNames.put(node.getClass(), name);
            return name;
        }
        return value;
    }

    public static String nodeDetail(final SVNode node, final boolean showId) {
        return node.getNodeClass() + ((showId && node.getId() != null) ? " \"" + node.getId() + "\"" : "");
    }

    public static String serializeInsets(final Insets insets) {
        return insets.getTop() + "|" + insets.getLeft() + "|" + insets.getRight() + "|" + insets.getBottom();
    }

    public static Insets deserializeInsets(final String value) {
        if (value == null)
            return new Insets(0);
        double top, left, right, bottom;
        int pos = 0;
        int next = 0;
        next = value.indexOf('|', pos);
        top = Double.parseDouble(value.substring(pos, next));
        pos = next + 1;
        next = value.indexOf('|', pos);
        left = Double.parseDouble(value.substring(pos, next));
        pos = next + 1;
        next = value.indexOf('|', pos);
        right = Double.parseDouble(value.substring(pos, next));
        pos = next + 1;
        bottom = Double.parseDouble(value.substring(pos));
        return new Insets(top, right, bottom, left);
    }

    public static String serializePropertyMap(@SuppressWarnings("rawtypes") final Map propMap) {
        if (propMap == null)
            return "";
        final StringBuilder sb = new StringBuilder();
        final Object keys[] = propMap.keySet().toArray();
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] instanceof String) {
                final String propkey = (String) keys[i];
                if (propkey.contains("pane-") || propkey.contains("box-")) {
                    final Object keyvalue = propMap.get(propkey);
                    if (sb.length() > 0) {
                        sb.append(':');
                    }
                    sb.append(propkey).append('=');

                    if (propkey.endsWith("margin")) {
                        sb.append(serializeInsets((Insets) keyvalue));
                    } else {
                        sb.append(keyvalue.toString());
                    }
                }
            }
        }
        return sb.toString();
    }

    public static Map<String, Object> deserializeMap(final String value) {
        if (value.length() == 0)
            return Collections.emptyMap();
        int pos = 0;
        final Map<String, Object> map = new HashMap<>();
        do {
            final int next = value.indexOf(':', pos);
            String temp;
            if (next == -1) {
                temp = value.substring(pos);
                pos = value.length();
            } else {
                temp = value.substring(pos, next);
                pos = next + 1;
            }
            final String label = temp.substring(0, temp.indexOf('='));
            final String realValue = temp.substring(temp.indexOf('=') + 1);

            if (label.endsWith("margin")) {
                map.put(label, deserializeInsets(realValue));
            } else {
                map.put(label, realValue);
            }
        } while (pos < value.length());
        return map;
    }

    public static List<Animation> getAnimations() {
        final List<Animation> animationList = new ArrayList<>();
        
        // FIXME disabled as JavaFX 8.0 has removed the AnimationPulseReceiver class
        
//        final AbstractMasterTimer timer = ToolkitAccessor.getMasterTimer();
//        try {
//            final Field field = AbstractMasterTimer.class.getDeclaredField("receivers");
//            field.setAccessible(true);
//            @SuppressWarnings("unchecked") final PulseReceiver[] object = (PulseReceiver[]) field.get(timer);
//            for (PulseReceiver pulseReceiver : object) {
//                if (pulseReceiver instanceof AnimationPulseReceiver) {
//                    final Field field2 = AnimationPulseReceiver.class.getDeclaredField("animation");
//                    field2.setAccessible(true);
//                    final Animation animation = (Animation) field2.get(pulseReceiver);
//                    animationList.add(animation);
//                }
//            }
//        } catch (final Exception e) {
//            ScenicViewExceptionLogger.submitException(e);
//        }
        return animationList;
    }

    public static boolean acceptWindow(final Window window) {
        if (window instanceof Stage) {
            final Node root = window.getScene() != null ? window.getScene().getRoot() : null;
            if (root != null && (root.getId() == null || !root.getId().startsWith(StageController.FX_CONNECTOR_BASE_ID))) {
                return true;
            }
        }
        return false;
    }

    public static int getAnimationUniqueID(final Animation animation) {
        return animation.hashCode();
    }

    public static int getNodeUniqueID(final Node node) {
        return node.hashCode();
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

    public static boolean isMouseTransparent(final Node node) {
        if (node == null) {
            return false;
        } else {
            return node.isMouseTransparent() || isMouseTransparent(node.getParent());
        }
    }

    public static boolean isNodeVisible(final Node node) {
        if (node == null) {
            return true;
        } else {
            return node.isVisible() && isNodeVisible(node.getParent());
        }
    }

    public static final boolean isNormalNode(final SVNode node) {
        return (node.getId() == null || !node.getId().startsWith(StageController.FX_CONNECTOR_BASE_ID));
    }
}
