package com.javafx.experiments.scenicview.connector.remote;

import java.rmi.*;

import com.javafx.experiments.scenicview.connector.*;
import com.javafx.experiments.scenicview.connector.details.DetailPaneType;
import com.javafx.experiments.scenicview.connector.event.AppEventDispatcher;
import com.javafx.experiments.scenicview.connector.node.SVNode;

public interface RemoteApplication extends Remote {

    public void configurationUpdated(final StageID id, Configuration configuration) throws RemoteException;

    public void update(final StageID id) throws RemoteException;

    public void setEventDispatcher(AppEventDispatcher dispatcher) throws RemoteException;

    public int[] getStageIDs() throws RemoteException;

    public String[] getStageNames() throws RemoteException;

    public void close(final StageID id) throws RemoteException;

    public void setSelectedNode(final StageID id, SVNode value) throws RemoteException;

    public void setDetail(StageID id, DetailPaneType detailType, int detailID, String value) throws RemoteException;

    public void animationsEnabled(final StageID id, boolean enabled) throws RemoteException;

    public void updateAnimations(final StageID id) throws RemoteException;

}
