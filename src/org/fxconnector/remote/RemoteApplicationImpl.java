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
package org.fxconnector.remote;

import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;

import org.fxconnector.Configuration;
import org.fxconnector.StageID;
import org.fxconnector.event.FXConnectorEvent;
import org.fxconnector.event.FXConnectorEventDispatcher;
import org.fxconnector.details.DetailPaneType;
import org.fxconnector.node.SVNode;
import org.fxconnector.remote.util.ScenicViewExceptionLogger;

class RemoteApplicationImpl extends UnicastRemoteObject implements RemoteApplication {

    private static final long serialVersionUID = 1L;
    RemoteApplication application;
    private RemoteConnector scenicView;
    private final int port;
    RemoteDispatcher dispatcher;

    RemoteApplicationImpl(final RemoteApplication application, final int port, final int serverPort) throws RemoteException {
        this.application = application;
        this.port = port;
        try {
            RMIUtils.bindApplication(this, port);
        } catch (final Exception e) {
            throw new RemoteException("Error starting agent", e);
        }

        RMIUtils.findScenicView(serverPort, scenicView -> {
            this.scenicView = scenicView;
            
            dispatcher = new RemoteDispatcher();
            dispatcher.start();

            org.fxconnector.Debugger.debug("RemoteConnector found:" + scenicView);

            try {
                scenicView.onAgentStarted(port);
            } catch (final RemoteException e) {
               ScenicViewExceptionLogger.submitException(e);
            }
            
            try {
                Thread.sleep(3000);
            } catch (final InterruptedException e) {
                ScenicViewExceptionLogger.submitException(e);
            }
        });
    }

    @Override public void close() {
        try {
            RMIUtils.unbindApplication(port);
            UnicastRemoteObject.unexportObject(this, true);
            if (dispatcher != null) {
                dispatcher.running = false;
            }
        } catch (final AccessException e) {
           ScenicViewExceptionLogger.submitException(e);
        } catch (final RemoteException e) {
           ScenicViewExceptionLogger.submitException(e);
        }
    }

    @Override public void configurationUpdated(final StageID id, final Configuration configuration) throws RemoteException {
        application.configurationUpdated(id, configuration);
    }

    @Override public void update(final StageID id) throws RemoteException {
        application.update(id);
    }

    @Override public void setEventDispatcher(final StageID id, final FXConnectorEventDispatcher dispatcher) throws RemoteException {
        org.fxconnector.Debugger.debug("Remote application setEventDispatcher!!!");
        application.setEventDispatcher(id, appEvent -> {
            if (scenicView != null) {
                RemoteApplicationImpl.this.dispatcher.addEvent(appEvent);
            } else {
                org.fxconnector.Debugger.debug("Cannot dispatch event:" + appEvent);
            }
        });
    }

    @Override public StageID[] getStageIDs() throws RemoteException {
        return application.getStageIDs();
    }

    @Override public void close(final StageID id) throws RemoteException {
        application.close(id);
    }

    @Override public void setSelectedNode(final StageID id, final SVNode value) throws RemoteException {
        application.setSelectedNode(id, value);
    }
    
    @Override public void removeSelectedNode(final StageID id) throws RemoteException {
        application.removeSelectedNode(id);
    }

    @Override public void setDetail(final StageID id, final DetailPaneType detailType, final int detailID, final String value) throws RemoteException {
        application.setDetail(id, detailType, detailID, value);
    }

    @Override public void animationsEnabled(final StageID id, final boolean enabled) throws RemoteException {
        application.animationsEnabled(id, enabled);
    }

    @Override public void updateAnimations(final StageID id) throws RemoteException {
        application.updateAnimations(id);
    }

    @Override public void pauseAnimation(final StageID id, final int animationID) throws RemoteException {
        application.pauseAnimation(id, animationID);
    }

    // This is what pushes the events to Scenic View
    class RemoteDispatcher extends Thread {
        boolean running = true;
        List<FXConnectorEvent> events = new LinkedList<>();
        
        {
            // we don't want to keep the application running needlessly
            setDaemon(true);
        }

        @Override public void run() {
            while (running) {
                try {
                    Thread.sleep(60); // roughly synced with pulse
                } catch (final InterruptedException e) {
                    ScenicViewExceptionLogger.submitException(e);
                }

                FXConnectorEvent event = null;
                while (!events.isEmpty()) {
                    try {
                        synchronized (events) {
                            event = events.remove(0);
                        }
                        if (scenicView != null) {
                            scenicView.dispatchEvent(event);
                        }
                    } catch (final RemoteException e) {
                        ScenicViewExceptionLogger.submitException(e);
                        try {
                            close(event.getStageID());
                            scenicView = null;
                            // UnicastRemoteObject.unexportObject(application, true);
                            RMIUtils.unbindApplication(port);
                            running = false;
                        } catch (final Exception e1) {
                            ScenicViewExceptionLogger.submitException(e1);
                        }
                    }
                }
            }
        }

        public void addEvent(final FXConnectorEvent event) {
            synchronized (events) {
                events.add(event);
            }
        }
    }
}
