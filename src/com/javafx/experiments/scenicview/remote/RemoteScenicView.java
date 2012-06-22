package com.javafx.experiments.scenicview.remote;

import java.rmi.*;

import com.javafx.experiments.scenicview.connector.event.AppEvent;

public interface RemoteScenicView extends Remote {

    public void dispatchEvent(AppEvent event) throws RemoteException;

    public void onAgentStarted(int port) throws RemoteException;

}
