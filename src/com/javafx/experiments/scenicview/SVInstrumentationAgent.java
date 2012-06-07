package com.javafx.experiments.scenicview;

import java.lang.instrument.Instrumentation;
import java.util.Iterator;
import javafx.stage.Window;

/**
 *
 * @author Jonathan Giles
 */
public class SVInstrumentationAgent {
    
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("hit with " + agentArgs + " and " + inst);
        System.out.println("Windows");
        Iterator<Window> windows = Window.impl_getWindows();
        while (windows.hasNext()) {
            Window window = windows.next();
            System.out.println("\tWindow: " + windows);
        }
    }
}
