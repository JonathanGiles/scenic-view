package com.javafx.experiments.scenicview.utils;

import java.lang.instrument.Instrumentation;
import java.rmi.RemoteException;
import java.util.*;

import javafx.application.Platform;
import javafx.stage.*;

import com.javafx.experiments.scenicview.connector.*;
import com.javafx.experiments.scenicview.connector.details.DetailPaneType;
import com.javafx.experiments.scenicview.connector.event.AppEventDispatcher;
import com.javafx.experiments.scenicview.connector.node.SVNode;
import com.javafx.experiments.scenicview.connector.remote.*;

public class AgentTest {

    public static boolean first = true;

    private static RemoteApplicationImpl application;

    public static void agentmain(final String agentArgs, final Instrumentation instrumentation) {

        if (first)
            System.out.println("Launching agent server on:" + agentArgs);
        try {
            final String[] args = agentArgs.split(":");

            final int port = Integer.parseInt(args[0]);
            final int serverPort = Integer.parseInt(args[1]);
            final int appID = Integer.parseInt(args[2]);
            final AppControllerImpl acontroller = new AppControllerImpl(appID, args[2]);

            final RemoteApplication application = new RemoteApplication() {

                final List<StageControllerImpl> finded = new ArrayList<StageControllerImpl>();
                final List<StageControllerImpl> controller = new ArrayList<StageControllerImpl>();

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
                            /**
                             * Move from finded to controllers
                             */
                            for (int i = 0; i < finded.size(); i++) {
                                if (finded.get(i).getID().equals(id)) {
                                    controller.add(finded.get(i));
                                    break;
                                }
                            }
                            getSC(id).setEventDispatcher(dispatcher);
                        }
                    });

                }

                @Override public StageID[] getStageIDs() throws RemoteException {
                    finded.clear();
                    @SuppressWarnings("deprecation") final Iterator<Window> it = Window.impl_getWindows();
                    while (it.hasNext()) {
                        final Window window = it.next();
                        if (ConnectorUtils.acceptWindow(window)) {
                            if (first)
                                System.out.println("Local JavaFX Stage found:" + ((Stage) window).getTitle());
                            final StageControllerImpl scontroller = new StageControllerImpl((Stage) window, acontroller);
                            scontroller.setRemote(true);
                            finded.add(scontroller);
                        }
                    }

                    final StageID[] ids = new StageID[finded.size()];
                    for (int i = 0; i < ids.length; i++) {
                        ids[i] = finded.get(i).getID();
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
                                controller.clear();
                            } else {
                                final StageController c = getSC(id, true);
                                if (c != null)
                                    c.close();

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

                @Override public void pauseAnimation(final StageID id, final int animationID) throws RemoteException {
                    Platform.runLater(new Runnable() {

                        @Override public void run() {
                            getSC(id).pauseAnimation(animationID);
                        }
                    });
                }

                private StageController getSC(final StageID id) {
                    return getSC(id, false);
                }

                private StageController getSC(final StageID id, final boolean remove) {
                    for (int i = 0; i < controller.size(); i++) {
                        if (controller.get(i).getID().equals(id)) {
                            if (remove) {
                                return controller.remove(i);
                            } else {
                                return controller.get(i);
                            }
                        }
                    }
                    return null;
                }

                @Override public void close() throws RemoteException {
                    AgentTest.application.close();
                }

            };
            first = false;
            AgentTest.application = new RemoteApplicationImpl(application, port, serverPort);
        } catch (final RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
