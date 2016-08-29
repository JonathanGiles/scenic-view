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

import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.scenicview.utils.Logger;

class RMIUtils {

    private static AtomicInteger rmiPort = new AtomicInteger(7557);
    private static final String REMOTE_CONNECTOR = "RemoteConnector";
    private static final String REMOTE_AGENT = "AgentServer";
    static Registry localRegistry;

    private RMIUtils() {
    }

    private static final RemoteConnector findScenicView(final String serverAdress, final int serverPort) throws Exception {
        final Registry registry = LocateRegistry.getRegistry(serverAdress, (new Integer(serverPort)).intValue());
        // look up the remote object
        return (RemoteConnector) (registry.lookup(REMOTE_CONNECTOR));
    }

    private static final RemoteApplication findApplication(final String serverAdress, final int serverPort) throws Exception {
        final Registry registry = LocateRegistry.getRegistry(serverAdress, (new Integer(serverPort)).intValue());
        // look up the remote object
        return (RemoteApplication) (registry.lookup(REMOTE_AGENT));
    }

    public static final void bindScenicView(final RemoteConnector view, final int port) throws AccessException, RemoteException {
        // create the registry and bind the name and object.
        final Registry registry = LocateRegistry.createRegistry(port);
        registry.rebind(REMOTE_CONNECTOR, view);
    }

    public static final void bindApplication(final RemoteApplication application, final int port) throws AccessException, RemoteException {
        // create the registry and bind the name and object.
        localRegistry = LocateRegistry.createRegistry(port);
        localRegistry.rebind(REMOTE_AGENT, application);
    }

    public static final void unbindApplication(final int port) throws AccessException, RemoteException {
        try {
            localRegistry.unbind(REMOTE_AGENT);
            UnicastRemoteObject.unexportObject(localRegistry, true);
        } catch (NotBoundException e) {
            // we don't care
        }
    }

    public static final void unbindScenicView(final int port) throws AccessException, RemoteException, NotBoundException {
        // create the registry and bind the name and object.
        final Registry registry = LocateRegistry.getRegistry(port);
        registry.unbind(REMOTE_CONNECTOR);
    }

    public static final void findScenicView(final int port, final Consumer<RemoteConnector> consumer) {
        new Thread(REMOTE_CONNECTOR + ".Finder") {
            @Override public void run() {
                RemoteConnector scenicView = null;

                while (scenicView == null) {
                    try {
                        Logger.print("Finding " + REMOTE_CONNECTOR + " connection for agent...");
                        scenicView = findScenicView("127.0.0.1", port);
                        if (scenicView == null) {
                            sleep(50);
                        }
                    } catch (final Exception e) {

                    }
                }
                consumer.accept(scenicView);
            }
        }.start();
    }

    public static final void findApplication(final int port, final Consumer<RemoteApplication> consumer) {
        final Thread remoteBrowserFinder = new Thread(REMOTE_AGENT + ".Finder") {
            @Override public void run() {
                RemoteApplication application = null;

                while (application == null) {
                    try {
                        application = findApplication("127.0.0.1", port);
                        if (application != null) {
                            sleep(50);
                        }
                    } catch (final Exception e) {

                    }
                }
                consumer.accept(application);
            }
        };
        remoteBrowserFinder.start();
    }

    public static int getClientPort() {
        return rmiPort.incrementAndGet();
    }

}
