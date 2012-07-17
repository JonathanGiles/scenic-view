package com.javafx.experiments.scenicview.utils;

import java.lang.instrument.Instrumentation;

import javafx.stage.Stage;

import com.javafx.experiments.scenicview.ScenicView;
import com.javafx.experiments.scenicview.update.LocalVMUpdateStrategy;
import com.sun.javafx.application.PlatformImpl;

/**
 * 
 * @author Jonathan Giles
 */
public class SVInstrumentationAgent {

    public static void premain(final String agentArgs, final Instrumentation inst) {
        new SVInstrumentationAgent();
    }

    private SVInstrumentationAgent() {
        System.out.println("Starting Scenic View Instrumentation Agent");
        PlatformImpl.startup(new Runnable() {

            @Override public void run() {
                final Stage stage = new Stage();
                // workaround for RT-10714
                stage.setWidth(640);
                stage.setHeight(800);
                stage.setTitle("Scenic View v" + ScenicView.VERSION);
                final ScenicView view = new ScenicView(new LocalVMUpdateStrategy(), stage);
                ScenicView.show(view, stage);
            }
        });
    }

}
