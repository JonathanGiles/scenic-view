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
package com.javafx.experiments.scenicview.connector.helper;

import java.io.File;
import java.net.URL;
import java.util.*;

import javafx.application.Platform;
import javafx.scene.Scene;

public class StyleSheetRefresher extends WorkerThread {
    final Scene scene;
    long lastModified = System.currentTimeMillis();

    public StyleSheetRefresher(final Scene scene) {
        super("StyleSheetRefresher", 2000);
        this.scene = scene;
        start();
    }

    /**
     * Stylesheets can not be refreshed in they are not outside
     * 
     * @param scene
     * @return
     */
    public static boolean canStylesBeRefreshed(final Scene scene) {
        final List<String> sheets = scene.getStylesheets();
        for (final String sheet : sheets) {
            if (sheet.startsWith("file"))
                return true;
        }
        return false;
    }

    @Override public void work() {
        /**
         * Do not allow previous modifications to force a refresh
         */

        try {
            final List<String> sheets = scene.getStylesheets();
            boolean needsRefresh = false;
            for (final String sheet : sheets) {
                if (sheet.startsWith("file")) {
                    final File file = new File(new URL(sheet).getFile());
                    if (file.lastModified() > lastModified) {
                        lastModified = file.lastModified();
                        needsRefresh = true;
                    }
                }
            }
            if (needsRefresh) {
                final List<String> styleSheets = new ArrayList<String>(sheets);
                Platform.runLater(new Runnable() {

                    @Override public void run() {
                        scene.getStylesheets().clear();
                        scene.getStylesheets().addAll(styleSheets);
                    }

                });
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public Scene getScene() {
        return scene;
    }
}
