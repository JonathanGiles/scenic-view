package com.javafx.experiments.scenicview;

import java.io.File;
import java.net.URL;
import java.util.*;

import javafx.scene.Scene;

public class StyleSheetRefresher extends Thread {
    final Scene scene;
    private boolean running = true;

    public StyleSheetRefresher(final Scene scene) {
        super("StyleSheetRefresher");
        this.scene = scene;
        start();
    }

    public void finish() {
        this.running = false;
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

    @Override public void run() {
        /**
         * Do not allow previous modifications to force a refresh
         */
        long lastModified = System.currentTimeMillis();
        while (running) {
            try {
                Thread.sleep(2000);
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
                    scene.getStylesheets().clear();
                    scene.getStylesheets().addAll(styleSheets);
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }
}
