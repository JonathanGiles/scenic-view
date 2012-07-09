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

                @Override public void update(final StageID id) {
                    Platform.runLater(new Runnable() {

                        @Override public void run() {
                            getSC(id).update();
                        }
                    });

                }

                @Override public void configurationUpdated(final StageID id, final Configuration configuration) throws RemoteException {
                    Platform.runLater(new Runnable() {

                        @Override public void run() {
                            getSC(id).configurationUpdated(configuration);
                        }
                    });

                }

                @Override public void setEventDispatcher(final StageID id, final AppEventDispatcher dispatcher) throws RemoteException {
                    Platform.runLater(new Runnable() {

                        @Override public void run() {
                            getSC(id).setEventDispatcher(dispatcher);
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

                @Override public void close(final StageID id) throws RemoteException {
                    Platform.runLater(new Runnable() {

                        @Override public void run() {
                            /**
                             * Special for closing the server
                             */
                            if (id == null) {
                                for (int i = 0; i < controller.size(); i++) {
                                    controller.get(i).close();
                                }
                            } else {
                                getSC(id).close();
                            }
                        }
                    });
                }

                @Override public void setSelectedNode(final StageID id, final SVNode value) throws RemoteException {
                    Platform.runLater(new Runnable() {

                        @Override public void run() {
                            System.out.println("Setting selected node:" + (value != null ? (" id:" + value.getNodeId() + " class:" + value.getClass()) : ""));
                            getSC(id).setSelectedNode(value);
                        }
                    });
                }

                @Override public void setDetail(final StageID id, final DetailPaneType detailType, final int detailID, final String value) {
                    Platform.runLater(new Runnable() {

                        @Override public void run() {
                            getSC(id).setDetail(detailType, detailID, value);
                        }
                    });
                }

                @Override public void animationsEnabled(final StageID id, final boolean enabled) throws RemoteException {
                    Platform.runLater(new Runnable() {

                        @Override public void run() {
                            getSC(id).animationsEnabled(enabled);
                        }
                    });
                }

                @Override public void updateAnimations(final StageID id) throws RemoteException {
                    Platform.runLater(new Runnable() {

                        @Override public void run() {
                            getSC(id).updateAnimations();
                        }
                    });
                }

                private StageController getSC(final StageID id) {
                    for (int i = 0; i < controller.size(); i++) {
                        if (controller.get(i).getID().equals(id))
                            return controller.get(i);
                    }
                    return null;
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
