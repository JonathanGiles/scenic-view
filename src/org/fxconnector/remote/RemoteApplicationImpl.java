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
import org.scenicview.utils.ExceptionLogger;
import org.scenicview.utils.Logger;

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

            Logger.print("RemoteConnector found:" + scenicView);

            try {
                scenicView.onAgentStarted(port);
            } catch (final RemoteException e) {
                ExceptionLogger.submitException(e);
            }
            
            try {
                Thread.sleep(3000);
            } catch (final InterruptedException e) {
                ExceptionLogger.submitException(e);
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
            ExceptionLogger.submitException(e);
        } catch (final RemoteException e) {
            ExceptionLogger.submitException(e);
        }
    }

    @Override public void configurationUpdated(final StageID id, final Configuration configuration) throws RemoteException {
        application.configurationUpdated(id, configuration);
    }

    @Override public void update(final StageID id) throws RemoteException {
        application.update(id);
    }

    @Override public void setEventDispatcher(final StageID id, final FXConnectorEventDispatcher dispatcher) throws RemoteException {
        Logger.print("Remote application setEventDispatcher!!!");
        application.setEventDispatcher(id, appEvent -> {
            if (scenicView != null) {
                RemoteApplicationImpl.this.dispatcher.addEvent(appEvent);
            } else {
                Logger.print("Cannot dispatch event:" + appEvent);
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
                    ExceptionLogger.submitException(e);
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
                        ExceptionLogger.submitException(e);
                        try {
                            close(event.getStageID());
                            scenicView = null;
                            // UnicastRemoteObject.unexportObject(application, true);
                            RMIUtils.unbindApplication(port);
                            running = false;
                        } catch (final Exception e1) {
                            ExceptionLogger.submitException(e1);
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
