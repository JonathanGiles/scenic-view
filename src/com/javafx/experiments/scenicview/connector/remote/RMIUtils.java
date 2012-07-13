package com.javafx.experiments.scenicview.connector.remote;

import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicInteger;

import com.javafx.experiments.scenicview.utils.AgentTest;

public class RMIUtils {

    private static AtomicInteger rmiPort = new AtomicInteger(7557);
    static Registry localRegistry;

    private RMIUtils() {
    }

    private static final RemoteScenicView findScenicView(final String serverAdress, final int serverPort) throws Exception {
        final Registry registry = LocateRegistry.getRegistry(serverAdress, (new Integer(serverPort)).intValue());
        // look up the remote object
        return (RemoteScenicView) (registry.lookup("ScenicView"));
    }

    private static final RemoteApplication findApplication(final String serverAdress, final int serverPort) throws Exception {
        final Registry registry = LocateRegistry.getRegistry(serverAdress, (new Integer(serverPort)).intValue());
        // look up the remote object
        return (RemoteApplication) (registry.lookup("AgentServer"));
    }

    public static final void bindScenicView(final RemoteScenicView view, final int port) throws AccessException, RemoteException {
        // create the registry and bind the name and object.
        final Registry registry = LocateRegistry.createRegistry(port);
        registry.rebind("ScenicView", view);
    }

    public static final void bindApplication(final RemoteApplication application, final int port) throws AccessException, RemoteException {
        // create the registry and bind the name and object.
        localRegistry = LocateRegistry.createRegistry(port);
        localRegistry.rebind("AgentServer", application);
    }

    public static final void unbindApplication(final int port) throws AccessException, RemoteException, NotBoundException {
        // create the registry and bind the name and object.
        localRegistry.unbind("AgentServer");
        UnicastRemoteObject.unexportObject(localRegistry, true);
    }

    public static final void unbindScenicView(final int port) throws AccessException, RemoteException, NotBoundException {
        // create the registry and bind the name and object.
        final Registry registry = LocateRegistry.getRegistry(port);
        registry.unbind("ScenicView");
    }

    public static final void findScenicView(final int port, final Observer observer) {
        new Thread("ScenicView.Finder") {
            @Override public void run() {

                RemoteScenicView scenicView = null;

                while (scenicView == null) {
                    try {
                        if (AgentTest.first)
                            System.out.println("Finding RemoteScenicView connection for agent...");
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
        final Thread remoteBrowserFinder = new Thread("ScenicView.Finder") {

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
