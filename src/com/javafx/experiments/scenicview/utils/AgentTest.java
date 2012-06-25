package com.javafx.experiments.scenicview.utils;

import java.lang.instrument.Instrumentation;
import java.rmi.RemoteException;
import java.util.Iterator;

import javafx.stage.*;

import com.javafx.experiments.scenicview.connector.StageController;
import com.javafx.experiments.scenicview.connector.event.MousePosEvent;
import com.javafx.experiments.scenicview.connector.remote.RemoteApplicationImpl;

public class AgentTest {
    public static void agentmain(final String agentArgs, final Instrumentation instrumentation) {
        try {
            RemoteApplicationImpl.main(new String[0]);
        } catch (final RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Launching scenicViews with RMI!!");
        @SuppressWarnings("deprecation") final Iterator<Window> it = Window.impl_getWindows();
        while (it.hasNext()) {
            final Window window = it.next();
            if (window instanceof Stage) {
                System.out.println("Launching scenicView for:" + ((Stage) window).getTitle());
                // JavaFXAppFinder.stages.add((Stage) window);
                // Platform.runLater(new Runnable() {
                //
                // @Override public void run() {
                // ScenicView.show(window.getScene());
                // }
                // });
                try {
                    RemoteApplicationImpl.scenicView.dispatchEvent(new MousePosEvent(StageController.STAGE_ID, "454x454"));
                } catch (final RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
