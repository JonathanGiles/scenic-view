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
package org.fxconnector.remote;

import java.lang.instrument.Instrumentation;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.fxconnector.AppControllerImpl;
import org.fxconnector.Configuration;
import org.fxconnector.ConnectorUtils;
import org.fxconnector.StageController;
import org.fxconnector.StageControllerImpl;
import org.fxconnector.StageID;
import org.fxconnector.details.DetailPaneType;
import org.fxconnector.event.FXConnectorEventDispatcher;
import org.fxconnector.node.SVNode;
import org.scenicview.utils.ExceptionLogger;

public class RuntimeAttach {
    
    private static boolean debug = true;
    private static RemoteApplicationImpl application;
    
    public static void agentmain(final String agentArgs, final Instrumentation instrumentation) {
        init(agentArgs, instrumentation);
    }
    
    private static void init(final String agentArgs, final Instrumentation instrumentation) {
        /**
         * Do it first to see first trace, this should be change if any other
         * boolean argument is included in the future
         */
        debug = agentArgs.indexOf("true") != -1;
        debug("Launching agent server on:" + agentArgs);
        try {
            final String[] args = agentArgs.split(":");

            final int port = Integer.parseInt(args[0]);
            final int serverPort = Integer.parseInt(args[1]);
            final int appID = Integer.parseInt(args[2]);
            debug = Boolean.parseBoolean(args[3]);
            final AppControllerImpl acontroller = new AppControllerImpl(appID, args[2]);

            final RemoteApplication application = new RemoteApplication() {
                final List<StageControllerImpl> finded = new ArrayList<>();
                final List<StageControllerImpl> controller = new ArrayList<>();

                @Override public void update(final StageID id) {
                    Platform.runLater(() -> getSC(id).update());
                }

                @Override public void configurationUpdated(final StageID id, final Configuration configuration) throws RemoteException {
                    Platform.runLater(() -> getSC(id).configurationUpdated(configuration));
                }

                @Override public void setEventDispatcher(final StageID id, final FXConnectorEventDispatcher dispatcher) throws RemoteException {
                    Platform.runLater(() -> {
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
                    });
                }

                @Override public StageID[] getStageIDs() throws RemoteException {
                    finded.clear();
                    @SuppressWarnings("deprecation") final Iterator<Window> it = Window.impl_getWindows();
                    while (it.hasNext()) {
                        final Window window = it.next();
                        if (ConnectorUtils.acceptWindow(window)) {
                            debug("Local JavaFX Stage found:" + ((Stage) window).getTitle());
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
                    Platform.runLater(() -> {
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
                            if (c != null) {
                                c.close();
                            }
                        }
                    });
                }

                @Override public void setSelectedNode(final StageID id, final SVNode value) throws RemoteException {
                    Platform.runLater(() -> {
                        debug("Setting selected node:" + (value != null ? (" id:" + value.getNodeId() + " class:" + value.getClass()) : ""));
                        final StageController sc = getSC(id);
                        if (sc != null)
                            sc.setSelectedNode(value);
                    });
                }
                
                @Override public void removeSelectedNode(final StageID id) throws RemoteException {
                    Platform.runLater(() -> {
                        final StageController sc = getSC(id);
                        if (sc != null)
                            sc.removeSelectedNode();
                    });
                }

                @Override public void setDetail(final StageID id, final DetailPaneType detailType, final int detailID, final String value) {
                    Platform.runLater(() -> getSC(id).setDetail(detailType, detailID, value));
                }

                @Override public void animationsEnabled(final StageID id, final boolean enabled) throws RemoteException {
                    Platform.runLater(() -> getSC(id).animationsEnabled(enabled));
                }

                @Override public void updateAnimations(final StageID id) throws RemoteException {
                    Platform.runLater(() -> getSC(id).updateAnimations());
                }

                @Override public void pauseAnimation(final StageID id, final int animationID) throws RemoteException {
                    Platform.runLater(() -> getSC(id).pauseAnimation(animationID));
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
                    RuntimeAttach.application.close();
                }
            };
            debug = false;
            RuntimeAttach.application = new RemoteApplicationImpl(application, port, serverPort);
        } catch (final RemoteException e) {
            ExceptionLogger.submitException(e);
        }
    }
    
    private static void debug(String msg) {
        if (debug) {
            System.out.println(msg);
        }
    }
}
