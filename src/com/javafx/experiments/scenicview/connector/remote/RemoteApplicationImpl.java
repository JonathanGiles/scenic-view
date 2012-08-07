package com.javafx.experiments.scenicview.connector.remote;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import com.javafx.experiments.scenicview.connector.*;
import com.javafx.experiments.scenicview.connector.details.DetailPaneType;
import com.javafx.experiments.scenicview.connector.event.*;
import com.javafx.experiments.scenicview.connector.node.SVNode;
import com.javafx.experiments.scenicview.utils.AgentTest;

public class RemoteApplicationImpl extends UnicastRemoteObject implements RemoteApplication {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    RemoteApplication application;
    private RemoteScenicView scenicView;
    private final int port;

    public RemoteApplicationImpl(final RemoteApplication application, final int port, final int serverPort) throws RemoteException {
        this.application = application;
        this.port = port;
        try {
            RMIUtils.bindApplication(this, port);
        } catch (final Exception e) {
            throw new RemoteException("Error starting agent", e);
        }

        RMIUtils.findScenicView(serverPort, new Observer() {

            @Override public void update(final Observable o, final Object obj) {
                scenicView = (RemoteScenicView) obj;
            }
        });
        while (scenicView == null) {
            try {
                Thread.sleep(50);
            } catch (final InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        AgentTest.debug("ScenicView found:" + scenicView);

        try {
            scenicView.onAgentStarted(port);
        } catch (final RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            Thread.sleep(3000);
        } catch (final InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }

    @Override public void close() {
        try {

            RMIUtils.unbindApplication(port);
            UnicastRemoteObject.unexportObject(this, true);
        } catch (final AccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final NotBoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override public void configurationUpdated(final StageID id, final Configuration configuration) throws RemoteException {
        application.configurationUpdated(id, configuration);
    }

    @Override public void update(final StageID id) throws RemoteException {
        application.update(id);
    }

    @Override public void setEventDispatcher(final StageID id, final AppEventDispatcher dispatcher) throws RemoteException {
        AgentTest.debug("Remote application setEventDispatcher!!!");
        application.setEventDispatcher(id, new AppEventDispatcher() {

            @Override public void dispatchEvent(final AppEvent appEvent) {
                try {
                    if (scenicView != null)
                        scenicView.dispatchEvent(appEvent);
                    else {
                        AgentTest.debug("Cannot dispatch event:" + appEvent);
                    }
                } catch (final RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    try {
                        close(id);
                        scenicView = null;
                        // UnicastRemoteObject.unexportObject(application,
                        // true);
                        RMIUtils.unbindApplication(port);

                    } catch (final Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    @Override public StageID[] getStageIDs() throws RemoteException {
        return application.getStageIDs();
    }

    @Override public void close(final StageID id) throws RemoteException {
        application.close(id);
    }

    @Override public void setSelectedNode(final StageID id, final SVNode value) throws RemoteException {
        application.setSelectedNode(id, value);
    }

    @Override public void setDetail(final StageID id, final DetailPaneType detailType, final int detailID, final String value) throws RemoteException {
        application.setDetail(id, detailType, detailID, value);
    }

    @Override public void animationsEnabled(final StageID id, final boolean enabled) throws RemoteException {
        application.animationsEnabled(id, enabled);
    }

    @Override public void updateAnimations(final StageID id) throws RemoteException {
        application.updateAnimations(id);
    }

    @Override public void pauseAnimation(final StageID id, final int animationID) throws RemoteException {
        application.pauseAnimation(id, animationID);
    }

}
