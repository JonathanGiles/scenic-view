package com.javafx.experiments.scenicview.connector.remote;

import java.rmi.*;

import com.javafx.experiments.scenicview.connector.Configuration;
import com.javafx.experiments.scenicview.connector.event.AppEventDispatcher;

public interface RemoteApplication extends Remote {

    public void configurationUpdated(Configuration configuration) throws RemoteException;

    public void update() throws RemoteException;

    public void setEventDispatcher(AppEventDispatcher dispatcher) throws RemoteException;

    public int[] getStageIDs() throws RemoteException;

    public void close() throws RemoteException;

}
