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
package com.javafx.experiments.scenicview.connector.remote;

import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicInteger;

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

    public static final void unbindApplication(final int port) throws AccessException, RemoteException, NotBoundException {
        // create the registry and bind the name and object.
        localRegistry.unbind(REMOTE_AGENT);
        UnicastRemoteObject.unexportObject(localRegistry, true);
    }

    public static final void unbindScenicView(final int port) throws AccessException, RemoteException, NotBoundException {
        // create the registry and bind the name and object.
        final Registry registry = LocateRegistry.getRegistry(port);
        registry.unbind(REMOTE_CONNECTOR);
    }

    public static final void findScenicView(final int port, final Observer observer) {
        new Thread(REMOTE_CONNECTOR + ".Finder") {
            @Override public void run() {

                RemoteConnector scenicView = null;

                while (scenicView == null) {
                    try {
                        AgentTest.debug("Finding " + REMOTE_CONNECTOR + " connection for agent...");
                        scenicView = findScenicView("127.0.0.1", port);
                        if (scenicView == null) {
                            sleep(50);
                        }
                    } catch (final Exception e) {

                    }
                }
                observer.update(null, scenicView);
            }
        }.start();
    }

    public static final void findApplication(final int port, final Observer observer) {
        final Thread remoteBrowserFinder = new Thread(REMOTE_AGENT + ".Finder") {

            @Override public void run() {

                RemoteApplication application = null;

                while (application == null) {
                    try {
                        application = RMIUtils.findApplication("127.0.0.1", port);
                        if (application != null) {
                            sleep(50);
                        }
                    } catch (final Exception e) {

                    }
                }
                observer.update(null, application);
            }
        };
        remoteBrowserFinder.start();
    }

    public static int getClientPort() {
        return rmiPort.incrementAndGet();
    }

}
