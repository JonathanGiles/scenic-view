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
package org.scenicview.utils;

import java.lang.instrument.Instrumentation;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.fxconnector.AppController;
import org.fxconnector.AppControllerImpl;
import org.fxconnector.StageControllerImpl;
import org.fxconnector.remote.FXConnector;
import org.fxconnector.remote.FXConnectorFactory;
import org.scenicview.ScenicView;
import org.scenicview.license.ScenicViewLicenseManager;
import org.scenicview.update.DummyUpdateStrategy;
import org.scenicview.update.LocalVMUpdateStrategy;
import org.scenicview.update.RemoteVMsUpdateStrategy;
import org.scenicview.utils.attach.AttachHandlerFactory;

import com.sun.javafx.application.PlatformImpl;

/**
 * This is the entry point for all different versions of Scenic View.
 */
public class ScenicViewBooter extends Application {

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
        FXConnectorFactory.setDebug(debug);
        ScenicViewDebug.setDebug(debug);
    }

    private static void runLicenseCheck() {
        ScenicViewLicenseManager.start();
    }

    private static void startup() {
        activateDebug();
        runLicenseCheck();
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
        stage.setWidth(640);
        stage.setHeight(800);
        stage.setTitle("Scenic View v" + ScenicView.VERSION);
        final DummyUpdateStrategy updateStrategy = new DummyUpdateStrategy(buildAppController(target));
        ScenicView.show(new ScenicView(updateStrategy, stage), stage);
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
     * Agent start point
     * 
     *************************************************************************/

    /**
     * agentmain is invoked when an agent is started after the application is already 
     * running. Agents started with agentmain can be attached programatically using 
     * the Sun tools API (for Sun/Oracle JVMs only -- the method for introducing 
     * dynamic agents is implementation-dependent).
     */
    public static void agentmain(final String agentArgs, final Instrumentation inst) {
        agentStart();
    }

    /**
     * premain is invoked when an agent is started before the application. Agents 
     * invoked using premain are specified with the -javaagent switch.
     */
    public static void premain(final String agentArgs, final Instrumentation inst) {
        agentStart();
    }

    private static void agentStart() {
        startup();

        // we only allow thsi mode in the paid version, so check now
        if (!ScenicViewLicenseManager.isPaid()) {
            System.out.println(
                    "This startup mode is not supported in the free version of Scenic View.\n" +
                    "The only way to use Scenic View in the free version is by adding calls" +
                    " to ScenicViewBooter.show(scene / parent) into your code base.");
            System.exit(0);
        }

        ScenicViewDebug.print("Starting Scenic View via the instrumentation agent");
        PlatformImpl.startup(() -> {
            final Stage stage = new Stage();
            // workaround for RT-10714
                stage.setWidth(640);
                stage.setHeight(800);
                stage.setTitle("Scenic View v" + ScenicView.VERSION);
                final ScenicView view = new ScenicView(new LocalVMUpdateStrategy(), stage);
                ScenicView.show(view, stage);
            });
    }

    /**************************************************************************
     *
     * runtime discovery start point
     * 
     *************************************************************************/
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
        System.setProperty(FXConnector.SCENIC_VIEW_VM, "true");
        startup();

        // we only allow thsi mode in the paid version, so check now
        if (!ScenicViewLicenseManager.isPaid()) {
            System.out.println(
                    "This startup mode is not supported in the free version of Scenic View.\n" +
                    "The only way to use Scenic View in the free version is by adding calls" +
                    " to ScenicViewBooter.show(scene / parent) into your code base.");
            System.exit(0);
        }

        setUserAgentStylesheet(STYLESHEET_MODENA);

        final RemoteVMsUpdateStrategy strategy = new RemoteVMsUpdateStrategy();

        FXConnectorFactory.debug("Platform running");
        // workaround for RT-10714
        stage.setWidth(640);
        stage.setHeight(800);
        stage.setTitle("Scenic View v" + ScenicView.VERSION);
        FXConnectorFactory.debug("Launching ScenicView v" + ScenicView.VERSION);
        ScenicView view = new ScenicView(strategy, stage);
        ScenicView.show(view, stage);

        FXConnectorFactory.debug("Startup done");
        while (view == null) {
            try {
                Thread.sleep(500);
            } catch (final InterruptedException e) {
                ExceptionLogger.submitException(e);
            }
        }

        FXConnectorFactory.debug("Creating server");
        try {
            strategy.setFXConnector(FXConnectorFactory.getConnector());
        } catch (final RemoteException e1) {
            ExceptionLogger.submitException(e1);
        }
        FXConnectorFactory.debug("Server done");
    }
}
