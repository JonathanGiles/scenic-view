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
package com.javafx.experiments.scenicview;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.*;

import javafx.scene.image.Image;

import com.javafx.experiments.scenicview.connector.node.SVNode;

/**
 * 
 */
public class DisplayUtils {

    private static final String CUSTOM_NODE_IMAGE = DisplayUtils.getNodeIcon("CustomNode").toString();
    private static final Map<String, Image> loadedImages = new HashMap<String, Image>();

    public static DecimalFormat DFMT = new DecimalFormat("0.0#");
    private static Level wLevel;
    private static Level wpLevel;

    public static final Image TRANSPARENT_PIXEL_IMAGE = getUIImage("transparent.png");
    public static final Image CLEAR_IMAGE = getUIImage("search-clear.png");
    public static final Image CLEAR_HOVER_IMAGE = getUIImage("search-clear-over.png");

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
