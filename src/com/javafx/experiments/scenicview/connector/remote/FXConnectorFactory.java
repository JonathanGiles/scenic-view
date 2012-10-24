package com.javafx.experiments.scenicview.connector.remote;

import java.rmi.RemoteException;

public class FXConnectorFactory {

    static FXConnector connector;

    private FXConnectorFactory() {
    }

    public static void setDebug(final boolean debug) {
        RemoteConnectorImpl.setDebug(debug);
    }

    public static void debug(final String debug) {
        RemoteConnectorImpl.debug(debug);
    }

    public static synchronized FXConnector getConnector() throws RemoteException {
        if (connector == null) {
            connector = new RemoteConnectorImpl();
        }
        return connector;
    }

}
