package com.javafx.experiments.scenicview.remote;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMIBrowserObject extends UnicastRemoteObject {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    int thisPort;

    String thisAddress;

    Registry registry; // rmi registry for lookup the remote objects.

    public RMIBrowserObject() throws RemoteException {

    }

}
