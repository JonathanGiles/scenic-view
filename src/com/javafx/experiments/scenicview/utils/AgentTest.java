package com.javafx.experiments.scenicview.utils;

import java.lang.instrument.Instrumentation;
import java.rmi.RemoteException;
import java.util.*;

import javafx.application.Platform;
import javafx.stage.*;

import com.javafx.experiments.scenicview.ScenicView;
import com.javafx.experiments.scenicview.connector.*;
import com.javafx.experiments.scenicview.connector.details.DetailPaneType;
import com.javafx.experiments.scenicview.connector.event.AppEventDispatcher;
import com.javafx.experiments.scenicview.connector.node.SVNode;
import com.javafx.experiments.scenicview.connector.remote.*;

public class AgentTest {

    public static boolean first = true;

    public static void agentmain(final String agentArgs, final Instrumentation instrumentation) {

        if (first)
            System.out.println("Launching agent server on:" + agentArgs);
        try {
            final String[] args = agentArgs.split(":");

            final int port = Integer.parseInt(args[0]);
            final int serverPort = Integer.parseInt(args[1]);
            final int appID = Integer.parseInt(args[2]);
            final AppControllerImpl acontroller = new AppControllerImpl(appID, args[2]);
            final List<StageControllerImpl> controller = new ArrayList<StageControllerImpl>();
            @SuppressWarnings("deprecation") final Iterator<Window> it = Window.impl_getWindows();
            while (it.hasNext()) {
                final Window window = it.next();
                if (window instanceof Stage && !(window.getScene().getRoot() instanceof ScenicView)) {
                    if (first)
                        System.out.println("Local JavaFX Stage found:" + ((Stage) window).getTitle());
                    final StageControllerImpl scontroller = new StageControllerImpl((Stage) window, acontroller);
                    scontroller.setRemote(true);
                    controller.add(scontroller);
                }
            }
            final RemoteApplication application = new RemoteApplication() {

                @Override public void update() {
                    Platform.runLater(new Runnable() {

                        @Override public void run() {
                            for (int i = 0; i < controller.size(); i++) {
                                controller.get(i).update();
                            }
                        }
                    });

                }

                @Override public void configurationUpdated(final Configuration configuration) throws RemoteException {
                    Platform.runLater(new Runnable() {

                        @Override public void run() {
                            System.out.println(configuration);
                            for (int i = 0; i < controller.size(); i++) {
                                controller.get(i).configurationUpdated(configuration);
                            }
                        }
                    });

                }

                @Override public void setEventDispatcher(final AppEventDispatcher dispatcher) throws RemoteException {
                    Platform.runLater(new Runnable() {

                        @Override public void run() {
                            for (int i = 0; i < controller.size(); i++) {
                                controller.get(i).setEventDispatcher(dispatcher);
                            }
                        }
                    });

                }

                @Override public int[] getStageIDs() throws RemoteException {
                    final int[] ids = new int[controller.size()];
                    for (int i = 0; i < ids.length; i++) {
                        ids[i] = controller.get(i).getID().getStageID();
                    }
                    return ids;
                }

                @Override public String[] getStageNames() throws RemoteException {
                    final String[] ids = new String[controller.size()];
                    for (int i = 0; i < ids.length; i++) {
                        ids[i] = controller.get(i).getID().getName();
                    }
                    return ids;
                }

                @Override public void close() throws RemoteException {
                    Platform.runLater(new Runnable() {

                        @Override public void run() {
                            for (int i = 0; i < controller.size(); i++) {
                                controller.get(i).close();
                            }
                        }
                    });
                }

                @Override public void setSelectedNode(final SVNode value) throws RemoteException {
                    Platform.runLater(new Runnable() {

                        @Override public void run() {
                            System.out.println("Setting selected node:" + value + value != null ? (" id:" + value.getNodeId() + " class:" + value.getClass()) : "");
                            for (int i = 0; i < controller.size(); i++) {
                                controller.get(i).setSelectedNode(value);
                            }
                        }
                    });
                }

                @Override public void setDetail(final StageID id, final DetailPaneType detailType, final int detailID, final String value) {
                    Platform.runLater(new Runnable() {

                        @Override public void run() {
                            for (int i = 0; i < controller.size(); i++) {
                                if (controller.get(i).getID().equals(id))
                                    controller.get(i).setDetail(detailType, detailID, value);
                            }
                        }
                    });
                }

                @Override public void animationsEnabled(final boolean enabled) throws RemoteException {
                    Platform.runLater(new Runnable() {

                        @Override public void run() {
                            for (int i = 0; i < controller.size(); i++) {
                                controller.get(i).animationsEnabled(enabled);
                            }
                        }
                    });
                }

                @Override public void updateAnimations() throws RemoteException {
                    Platform.runLater(new Runnable() {

                        @Override public void run() {
                            for (int i = 0; i < controller.size(); i++) {
                                controller.get(i).updateAnimations();
                            }
                        }
                    });
                }

            };
            first = false;
            new RemoteApplicationImpl(application, port, serverPort);
        } catch (final RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
