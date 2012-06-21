package com.javafx.experiments.scenicview.remote;

import java.rmi.*;

public interface RemoteScenicView extends Remote {

    public void dispatchEvent(RemoteEvent event) throws RemoteException ;
    
    public void onAgentStarted(int port) throws RemoteException ;
    
}
