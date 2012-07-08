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
