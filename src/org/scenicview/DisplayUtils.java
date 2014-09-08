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
package org.scenicview;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.*;

import javafx.scene.image.Image;

import org.fxconnector.node.SVNode;

/**
 * 
 */
public class DisplayUtils {

    private static final String CUSTOM_NODE_IMAGE = DisplayUtils.getNodeIcon("CustomNode").toString();
    private static final Map<String, Image> loadedImages = new HashMap<>();

    public static DecimalFormat DFMT = new DecimalFormat("0.0#");
    private static Level wLevel;
    private static Level wpLevel;

    public static final Image TRANSPARENT_PIXEL_IMAGE = getUIImage("transparent.png");
    public static final Image CLEAR_IMAGE = getUIImage("search-clear.png");
    public static final Image CLEAR_HOVER_IMAGE = getUIImage("search-clear-over.png");

    public static Image getUIImage(final String image) {
        return new Image(ScenicViewGui.class.getResource("images/ui/" + image).toString());
    }

    private static URL getNodeIcon(final String node) {
        return ScenicViewGui.class.getResource("images/nodeicons/" + node + ".png");
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

    public static void showWebView(final boolean show) {
        if (show) {
            /**
             * Ugly patch to remove the visual trace of the WebPane
             */
            final Logger webLogger = java.util.logging.Logger.getLogger("com.sun.webpane");
            final Logger webPltLogger = java.util.logging.Logger.getLogger("webcore.platform.api.SharedBufferInputStream");
            wLevel = webLogger.getLevel();
            wpLevel = webPltLogger.getLevel();
            webLogger.setLevel(Level.SEVERE);
            webPltLogger.setLevel(Level.SEVERE);
        } else {
            Logger.getLogger("com.sun.webpane").setLevel(wLevel);
            Logger.getLogger("webcore.platform.api.SharedBufferInputStream").setLevel(wpLevel);
        }
    }

}
