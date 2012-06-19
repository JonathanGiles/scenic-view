package com.javafx.experiments.scenicview.utils;

import java.lang.instrument.Instrumentation;
import java.util.Iterator;

import javafx.application.Platform;
import javafx.stage.*;

import com.javafx.experiments.scenicview.ScenicView;

public class AgentTest {
    public static void agentmain(final String agentArgs, 
        final Instrumentation instrumentation) {
      System.out.println("Launching scenicViews!!");
      final Iterator<Window> it = Window.impl_getWindows();
      while (it.hasNext()) {
        final Window window = it.next();
        if(window instanceof Stage) {
            System.out.println("Launching scenicView for:"+((Stage) window).getTitle());
            //JavaFXAppFinder.stages.add((Stage) window);
            Platform.runLater(new Runnable() {
                
                @Override public void run() {
                    ScenicView.show(window.getScene());
                }
            });
            
        }
    }
    }
 }
