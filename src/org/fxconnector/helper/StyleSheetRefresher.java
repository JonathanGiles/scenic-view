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
package org.fxconnector.helper;

import org.fxconnector.remote.util.ScenicViewExceptionLogger;
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
                final List<String> styleSheets = new ArrayList<>(sheets);
                Platform.runLater(new Runnable() {

                    @Override public void run() {
                        scene.getStylesheets().clear();
                        scene.getStylesheets().addAll(styleSheets);
                    }

                });
            }
        } catch (final Exception e) {
            ScenicViewExceptionLogger.submitException(e);
        }
    }

    public Scene getScene() {
        return scene;
    }
}
