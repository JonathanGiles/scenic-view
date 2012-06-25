package com.javafx.experiments.scenicview.utils;

import java.lang.instrument.Instrumentation;
import java.util.*;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.*;

import com.javafx.experiments.scenicview.*;
import com.javafx.experiments.scenicview.connector.StageController;
import com.javafx.experiments.scenicview.dialog.StageSelectionBox;
import com.javafx.experiments.scenicview.helper.*;
import com.javafx.experiments.scenicview.helper.WindowChecker.WindowFilter;

/**
 * 
 * @author Jonathan Giles
 */
public class SVInstrumentationAgent implements WindowFilter {
    private static final int WAIT_TIME = 1000 * 1; // 1 second
    private static final int MAX_WAIT_TIME = WAIT_TIME * 60 * 5; // 5 minutes

    public static void premain(final String agentArgs, final Instrumentation inst) {
        new SVInstrumentationAgent();
    }

    private SVInstrumentationAgent() {
        System.out.println("Starting Scenic View Instrumentation Agent");

        final WindowChecker agentThread = new WindowChecker(this) {

            @Override protected void onWindowsFound(final List<Window> windowList) {

                if (windowList.isEmpty())
                    return;

                finish();

                /**
                 * Wait till we have the scene and the root node
                 */
                boolean initialized = false;
                while (!initialized) {
                    initialized = true;
                    try {
                        Thread.sleep(WAIT_TIME);
                    } catch (final InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    for (int i = 0; i < windowList.size(); i++) {
                        if (windowList.get(i).getScene() == null || windowList.get(i).getScene().getRoot() == null) {
                            initialized = false;
                        }
                    }
                }

                // if
                if (windowList.size() == 0) {
                    return;
                } else if (windowList.size() == 1) {
                    // go straight into scenic view with this window
                    loadScenicView((Stage) windowList.get(0));
                } else {
                    // show the Scenic View stage selection dialog
                    final List<StageController> empty = Collections.emptyList();
                    StageSelectionBox.make("Stage selection", null, empty);
                }

            }
        };
        agentThread.verbose();
        agentThread.setMaxWaitTime(MAX_WAIT_TIME);
        agentThread.start();
    }

    private void loadScenicView(final Stage stage) {
        loadScenicView(stage.getScene());
    }

    private void loadScenicView(final Scene scene) {
        Platform.runLater(new Runnable() {
            @Override public void run() {
                ScenicView.show(scene);
            }
        });
    }

    @Override public boolean accept(final Window window) {
        if (window instanceof Stage) {
            return true;
        } else {
            System.out.println("Scenic View only supports Stages right now, but found a " + window.getClass());
            return false;
        }
    }
}
