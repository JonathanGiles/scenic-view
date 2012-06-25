package com.javafx.experiments.scenicview.connector.remote;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import com.javafx.experiments.scenicview.connector.Configuration;
import com.javafx.experiments.scenicview.connector.event.*;

public class RemoteApplicationImpl extends UnicastRemoteObject implements RemoteApplication {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    RemoteApplication application;
    private RemoteScenicView scenicView;

    public RemoteApplicationImpl(final RemoteApplication application, final int port) throws RemoteException {
        this.application = application;
        try {
            RMIUtils.bindApplication(this, port);
        } catch (final Exception e) {
            throw new RemoteException("Error starting agent", e);
        }

        RMIUtils.findScenicView(new Observer() {

            @Override public void update(final Observable o, final Object obj) {
                scenicView = (RemoteScenicView) obj;
            }
        });
        while (scenicView == null) {
            try {
                Thread.sleep(1000);
            } catch (final InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        System.out.println("ScenicView found:" + scenicView);
        application.setEventDispatcher(new AppEventDispatcher() {

            @Override public void dispatchEvent(final AppEvent appEvent) {
                try {
                    if (scenicView != null)
                        scenicView.dispatchEvent(appEvent);
                    else {
                        System.out.println("Cannot dispatch event:" + appEvent);
                    }
                } catch (final RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    try {
                        close();
                        scenicView = null;
                        RMIUtils.unbindApplication(port);

                    } catch (final Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
            }
        });
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

    @Override public void configurationUpdated(final Configuration configuration) throws RemoteException {
        application.configurationUpdated(configuration);
    }

    @Override public void update() throws RemoteException {
        application.update();
    }

    @Override public void setEventDispatcher(final AppEventDispatcher dispatcher) throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override public int[] getStageIDs() throws RemoteException {
        return application.getStageIDs();
    }

    @Override public void close() throws RemoteException {
        application.close();
    }

}
