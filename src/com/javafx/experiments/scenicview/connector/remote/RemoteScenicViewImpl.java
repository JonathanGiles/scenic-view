package com.javafx.experiments.scenicview.connector.remote;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import com.javafx.experiments.scenicview.connector.event.AppEvent;

public class RemoteScenicViewImpl extends UnicastRemoteObject implements RemoteScenicView {

    RemoteApplication application;

    public RemoteScenicViewImpl() throws RemoteException {
        super();
        RMIUtils.bindScenicView(this);
    }

    @Override public void dispatchEvent(final AppEvent event) {
        System.out.println(event.getType());
    }

    @Override public void onAgentStarted(final int port) {
        System.out.println("Agent started on port:" + port);
        RMIUtils.findApplication(port, new Observer() {

            @Override public void update(final Observable o, final Object obj) {
                application = (RemoteApplication) obj;
                try {
                    application.sendInfo("Info to aggent");
                } catch (final RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    public static void main(final String[] args) throws RemoteException, InterruptedException {
        final RemoteScenicViewImpl impl = new RemoteScenicViewImpl();
        // while(impl.application==null) {
        // Thread.sleep(1000);
        // }

    }

}
