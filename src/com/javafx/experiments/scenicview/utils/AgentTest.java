package com.javafx.experiments.scenicview.utils;

import java.lang.instrument.Instrumentation;
import java.rmi.RemoteException;
import java.util.Iterator;

import javafx.stage.*;

import com.javafx.experiments.scenicview.connector.StageID;
import com.javafx.experiments.scenicview.connector.event.MousePosEvent;
import com.javafx.experiments.scenicview.connector.remote.RemoteApplicationImpl;

public class AgentTest {
    public static void agentmain(final String agentArgs, final Instrumentation instrumentation) {

        System.out.println("Launching agent server on:" + agentArgs);
        try {
            RemoteApplicationImpl.main(new String[] { agentArgs });
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
                try {
                    RemoteApplicationImpl.scenicView.dispatchEvent(new MousePosEvent(new StageID(0, 0), "454x454"));
                } catch (final RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
