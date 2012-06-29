/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.javafx.experiments.scenicview;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;

import javafx.scene.image.Image;
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

    static final Image CLEAR_IMAGE = getUIImage("clear_left.png");

    public static Image getUIImage(final String image) {
        return new Image(ScenicView.class.getResource("images/ui/" + image).toString());
    }

    private static URL getNodeIcon(final String node) {
        return ScenicView.class.getResource("images/nodeicons/" + node + ".png");
    }

    public static Image getIcon(final SVNode svNode) {
        if (svNode.getIcon() != null)
            return svNode.getIcon();
        Image image = loadedImages.get(svNode.getNodeClass());
        if (image == null) {
            final URL resource = DisplayUtils.getNodeIcon(svNode.getNodeClass());
            String url;
            if (resource != null) {
                url = resource.toString();
            } else {
                url = CUSTOM_NODE_IMAGE;
            }
            image = new Image(url);
            loadedImages.put(svNode.getNodeClass(), image);
        }
        return image;
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
