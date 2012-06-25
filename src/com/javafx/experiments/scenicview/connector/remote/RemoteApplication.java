package com.javafx.experiments.scenicview.connector.remote;

import java.rmi.*;

public interface RemoteApplication extends Remote {

    public void sendInfo(String info) throws RemoteException;

}
