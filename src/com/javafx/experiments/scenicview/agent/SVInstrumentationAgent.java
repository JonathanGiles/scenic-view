package com.javafx.experiments.scenicview.agent;

import com.javafx.experiments.scenicview.ScenicView;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 *
 * @author Jonathan Giles
 */
public class SVInstrumentationAgent implements Runnable {
    private static final int WAIT_TIME = 1000 * 1;                  // 1 second
    private static final int MAX_WAIT_TIME = WAIT_TIME * 60 * 5;    // 5 minutes
    
    public static void premain(String agentArgs, Instrumentation inst) {
        new SVInstrumentationAgent();
    }
    
    private SVInstrumentationAgent() {
        System.out.println("Starting Scenic View Instrumentation Agent");
        
        final Thread agentThread = new Thread(this);
        agentThread.start();
    }

    public void run() {
        // Keep iterating until we have a any windows.
        // If we past the maximum wait time, we'll exit
        int currentWait = 0;
        Iterator<Window> windows = Window.impl_getWindows();
        while (! windows.hasNext()) {
            try {
                System.out.println("No JavaFX applications found - sleeping for " + WAIT_TIME / 1000 + " seconds");
                Thread.sleep(WAIT_TIME);
                currentWait += WAIT_TIME;

                if (currentWait >= MAX_WAIT_TIME) {
                    break;
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

            windows = Window.impl_getWindows();
        }

        // if we have no windows here, we will just quit
        if (! windows.hasNext()) {
            System.out.println("No JavaFX applications found - quiting");
            return;
        }

        // we will build up a more useful List of Windows from the iterator
        List<Stage> windowList = new ArrayList<Stage>();
        while (windows.hasNext()) {
            Window window = windows.next();
            if (window instanceof Stage) {
                windowList.add((Stage)window);
            } else {
                System.out.println("Scenic View only supports Stages right now, but found a " + window.getClass());
            }
        }

        // if
        if (windowList.size() == 0) {
            return;
        } else if (windowList.size() == 1) {
            // go straight into scenic view with this window
            loadScenicView(windowList.get(0));
        } else {
            // show the Scenic View stage selection dialog
        }
    }
    
    private void loadScenicView(Stage stage) {
        loadScenicView(stage.getScene());
    }
    
    private void loadScenicView(final Scene scene) {
        Platform.runLater(new Runnable() {
            @Override public void run() {
                ScenicView.show(scene);
            }
        });
    }
}
