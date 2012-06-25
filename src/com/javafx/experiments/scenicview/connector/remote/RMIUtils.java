package com.javafx.experiments.scenicview.connector.remote;

import java.rmi.*;
import java.rmi.registry.*;
import java.util.Observer;

public class RMIUtils {

    private static final int SV_SERVER_PORT = 7557;
    private static int clientPort = 7558;

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

    public static final void bindScenicView(final RemoteScenicView view) throws AccessException, RemoteException {
        // create the registry and bind the name and object.
        final Registry registry = LocateRegistry.createRegistry(SV_SERVER_PORT);
        registry.rebind("ScenicView", view);
    }

    public static final void bindApplication(final RemoteApplication application, final int port) throws AccessException, RemoteException {
        // create the registry and bind the name and object.
        final Registry registry = LocateRegistry.createRegistry(port);
        registry.rebind("AgentServer", application);
    }

    public static final void unbindApplication(final int port) throws AccessException, RemoteException, NotBoundException {
        // create the registry and bind the name and object.
        final Registry registry = LocateRegistry.getRegistry(port);
        registry.unbind("AgentServer");
    }

    public static final void findScenicView(final Observer observer) {
        new Thread("ScenicView.Finder") {
            @Override public void run() {

                RemoteScenicView scenicView = null;

                while (scenicView == null) {
                    try {
                        System.out.println("Finding ScenicView...");
                        scenicView = findScenicView("127.0.0.1", SV_SERVER_PORT);
                        sleep(1000);
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
                        sleep(1000);
                    } catch (final Exception e) {

                    }
                }
                observer.update(null, application);
            }
        };
        remoteBrowserFinder.start();
    }

    public static int getClientPort() {
        return clientPort++;
    }

}
