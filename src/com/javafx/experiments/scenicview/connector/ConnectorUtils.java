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
package com.javafx.experiments.scenicview.connector;

import java.lang.reflect.Field;
import java.text.*;
import java.util.*;

import javafx.animation.Animation;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.layout.Region;
import javafx.scene.transform.*;
import javafx.stage.*;

import com.javafx.experiments.scenicview.connector.node.SVNode;
import com.sun.scenario.ToolkitAccessor;
import com.sun.scenario.animation.AbstractMasterTimer;
import com.sun.scenario.animation.shared.*;

public class ConnectorUtils {

    private static DecimalFormat DFMT = new DecimalFormat("0.0#");

    static final DecimalFormat df = new DecimalFormat("0.0");

    private ConnectorUtils() {
    }

    public static int getBranchCount(final Node node) {
        if (!isNormalNode(node))
            return 0;
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

    public final static boolean isNormalNode(final Node node) {
        return (node.getId() == null || !node.getId().startsWith(StageController.SCENIC_VIEW_BASE_ID));
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
        final Map<String, Object> map = new HashMap<String, Object>();
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
        final List<Animation> animationList = new ArrayList<Animation>();
        final AbstractMasterTimer timer = ToolkitAccessor.getMasterTimer();
        try {
            final Field field = AbstractMasterTimer.class.getDeclaredField("receiverList");
            field.setAccessible(true);
            @SuppressWarnings("unchecked") final List<PulseReceiver> object = (List<PulseReceiver>) field.get(timer);
            for (final Iterator<PulseReceiver> iterator = object.iterator(); iterator.hasNext();) {
                final PulseReceiver pulseReceiver = iterator.next();
                if (pulseReceiver instanceof AnimationPulseReceiver) {
                    final Field field2 = AnimationPulseReceiver.class.getDeclaredField("animation");
                    field2.setAccessible(true);
                    final Animation animation = (Animation) field2.get(pulseReceiver);
                    animationList.add(animation);
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return animationList;
    }

    public static boolean acceptWindow(final Window window) {
        if (window instanceof Stage) {
            final Node root = window.getScene() != null ? window.getScene().getRoot() : null;
            if (root != null && (root.getId() == null || !root.getId().startsWith(StageController.SCENIC_VIEW_BASE_ID))) {
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

}
