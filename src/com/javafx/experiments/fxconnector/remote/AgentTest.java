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
package com.javafx.experiments.fxconnector.remote;

import java.lang.instrument.Instrumentation;
import java.rmi.RemoteException;
import java.util.*;

import javafx.application.Platform;
import javafx.stage.*;

import com.javafx.experiments.fxconnector.*;
import com.javafx.experiments.fxconnector.details.DetailPaneType;
import com.javafx.experiments.fxconnector.event.FXConnectorEventDispatcher;
import com.javafx.experiments.fxconnector.node.SVNode;

public class AgentTest {

    private static boolean debug = false;

    private static RemoteApplicationImpl application;

    static void debug(final String log) {
        if (debug) {
            System.out.println(log);
        }
    }

    public static void agentmain(final String agentArgs, final Instrumentation instrumentation) {
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

                @Override public void setEventDispatcher(final StageID id, final FXConnectorEventDispatcher dispatcher) throws RemoteException {
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
                            debug("Setting selected node:" + (value != null ? (" id:" + value.getNodeId() + " class:" + value.getClass()) : ""));
                            final StageController sc = getSC(id);
                            if (sc != null)
                                sc.setSelectedNode(value);
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
            debug = false;
            AgentTest.application = new RemoteApplicationImpl(application, port, serverPort);
        } catch (final RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
