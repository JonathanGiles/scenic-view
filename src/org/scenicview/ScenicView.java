/*
 * Copyright (c) 2012 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.scenicview;

import java.lang.instrument.Instrumentation;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.fxconnector.AppController;
import org.fxconnector.AppControllerImpl;
import org.fxconnector.StageControllerImpl;
import org.fxconnector.remote.FXConnector;
import org.fxconnector.remote.FXConnectorFactory;
import org.scenicview.update.DummyUpdateStrategy;
import org.scenicview.update.RemoteVMsUpdateStrategy;
import org.scenicview.utils.ExceptionLogger;
import org.scenicview.utils.ScenicViewDebug;
import org.scenicview.utils.attach.AttachHandlerFactory;

import com.sun.javafx.tk.Toolkit;

/**
 * This is the entry point for all different versions of Scenic View.
 */
public class ScenicView extends Application {

    /**************************************************************************
     *
     * fields
     * 
     *************************************************************************/

    public static final String JDK_PATH_KEY = "jdkPath";

    private static boolean debug = true;

    /**************************************************************************
     *
     * general-purpose code
     * 
     *************************************************************************/

    private static void activateDebug() {
        org.fxconnector.Debugger.setDebug(debug);
        ScenicViewDebug.setDebug(debug);
    }

    private static void startup() {
        activateDebug();
    }

    /**************************************************************************
     *
     * Scenic View 'hardcoded show(..)' start point
     * 
     *************************************************************************/

    public static void show(final Scene target) {
        show(target.getRoot());
    }

    public static void show(final Parent target) {
        startup();

        final Stage stage = new Stage();
        // workaround for RT-10714
        stage.setWidth(1024);
        stage.setHeight(768);
        stage.setTitle("Scenic View v" + ScenicViewGui.VERSION);
        final DummyUpdateStrategy updateStrategy = new DummyUpdateStrategy(buildAppController(target));
        ScenicViewGui.show(new ScenicViewGui(updateStrategy, stage), stage);
    }

    private static List<AppController> buildAppController(final Parent target) {
        final List<AppController> controllers = new ArrayList<AppController>();
        if (target != null) {
            final AppController aController = new AppControllerImpl();
            final boolean sceneRoot = target.getScene().getRoot() == target;
            final StageControllerImpl sController = new StageControllerImpl(target, aController, sceneRoot);

            aController.getStages().add(sController);
            controllers.add(aController);
        }
        return controllers;
    }

    /**************************************************************************
     *
     * runtime discovery start point
     * (Also refer to RuntimeAttach class)
     * 
     *************************************************************************/
    public static void premain(final String agentArgs, final Instrumentation instrumentation) {
        // we start up a new thread to take care of initialising Scenic View
        // so that we don't block the loading of the actual application.
        @SuppressWarnings("unused")
        Thread scenicViewBootThread = new Thread(() -> {
            Toolkit tk = Toolkit.getToolkit();   
            Platform.runLater(() -> {
                try {
                    new ScenicView().start(new Stage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }, "scenic-view-boot");
        scenicViewBootThread.setDaemon(true);
        scenicViewBootThread.start();
    }

    public static void main(final String[] args) {
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-debug")) {
                    debug = true;
                }
            }
        }
        launch(args);
    }

    @Override public void start(final Stage stage) throws Exception {
        // This mode is only available when we are in the commercial Scenic View,
        // so we must start up the license checker and validate

        // Test if we can load a class from jfxrt.jar
        try {
            Class.forName("javafx.beans.property.SimpleBooleanProperty").newInstance();
        } catch (final Exception e) {
            // Fatal error - JavaFX should be on the classpath for all users
            // of Java 8.0 and above (which is what Scenic View 8.0 and above
            // targets.
            ScenicViewDebug.print("Error: JavaFX not found");
            System.exit(-1);
        }

        AttachHandlerFactory.initAttachAPI(stage);
//        System.setProperty(FXConnector.SCENIC_VIEW_VM, "true");
        startup();

        setUserAgentStylesheet(STYLESHEET_MODENA);

        final RemoteVMsUpdateStrategy strategy = new RemoteVMsUpdateStrategy();

        // workaround for RT-10714
        stage.setWidth(1024);
        stage.setHeight(768);
        stage.setTitle("Scenic View v" + ScenicViewGui.VERSION);
        org.fxconnector.Debugger.debug("Platform running");
        org.fxconnector.Debugger.debug("Launching ScenicView v" + ScenicViewGui.VERSION);
        ScenicViewGui view = new ScenicViewGui(strategy, stage);
        ScenicViewGui.show(view, stage);

        org.fxconnector.Debugger.debug("Startup done");
        while (view == null) {
            try {
                Thread.sleep(500);
            } catch (final InterruptedException e) {
                ExceptionLogger.submitException(e);
            }
        }

        org.fxconnector.Debugger.debug("Creating server");
        try {
            strategy.setFXConnector(FXConnectorFactory.getConnector());
        } catch (final RemoteException e1) {
            ExceptionLogger.submitException(e1);
        }
        org.fxconnector.Debugger.debug("Server done");
    }
}
