/*
 * Scenic View, 
 * Copyright (C) 2012 Jonathan Giles, Ander Ruiz, Amy Fowler 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import org.fxconnector.remote.FXConnectorFactory;
import org.scenicview.model.attach.AttachHandlerFactory;
import org.scenicview.model.update.LocalUpdateStrategy;
import org.scenicview.model.update.RemoteVMsUpdateStrategy;
import org.scenicview.utils.ExceptionLogger;
import org.scenicview.utils.Logger;
import org.scenicview.view.ScenicViewGui;

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
        Logger.setEnabled(debug);
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
        if (target == null) {
            return;
        }
        
        startup();

        final Stage stage = new Stage();
        
        // workaround for RT-10714
        stage.setWidth(1024);
        stage.setHeight(768);
        stage.setTitle("Scenic View v" + ScenicViewGui.VERSION);
        
        final List<AppController> controllers = new ArrayList<>();
        final AppController aController = new AppControllerImpl();
        final boolean sceneRoot = target.getScene().getRoot() == target;
        final StageControllerImpl sController = new StageControllerImpl(target, aController, sceneRoot);

        aController.getStages().add(sController);
        controllers.add(aController);
        
        final LocalUpdateStrategy updateStrategy = new LocalUpdateStrategy(controllers);
        ScenicViewGui.show(new ScenicViewGui(updateStrategy, stage), stage);
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
            while (true) {
                try {
                    Platform.runLater(() -> {
                        try {
                            new ScenicView().start(new Stage());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    break;
                } catch (IllegalStateException e) {
                    // FX runtime hasn't been initialized yet. Actual initialization occurs in method LauncherImpl.startup()
                    // which must be called only once and under normal circumstances called as result of launching instrumented
                    // application, so we have to wait while FX runtime will be initialized.
                    try { Thread.sleep(500); } catch (InterruptedException ex) {}
                }
            }
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
            Logger.print("Error: JavaFX not found");
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
        Logger.print("Platform running");
        Logger.print("Launching ScenicView v" + ScenicViewGui.VERSION);
        ScenicViewGui view = new ScenicViewGui(strategy, stage);
        ScenicViewGui.show(view, stage);

        Logger.print("Startup done");
        while (view == null) {
            try {
                Thread.sleep(500);
            } catch (final InterruptedException e) {
                ExceptionLogger.submitException(e);
            }
        }

        Logger.print("Creating server");
        try {
            strategy.setFXConnector(FXConnectorFactory.getConnector());
        } catch (final RemoteException e1) {
            ExceptionLogger.submitException(e1);
        }
        Logger.print("Server done");
    }
}
