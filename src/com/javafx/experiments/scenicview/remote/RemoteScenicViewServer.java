package com.javafx.experiments.scenicview.remote;

import java.rmi.RemoteException;

import com.javafx.experiments.scenicview.connector.StageController;
import com.javafx.experiments.scenicview.connector.event.MousePosEvent;

public class RemoteScenicViewServer {

    RemoteScenicView scenicView;
    RemoteApplication application;

    public RemoteScenicViewServer() throws RemoteException {

        scenicView = new RemoteScenicViewImpl();
        final Thread remoteBrowserFinder = new Thread() {
            @Override public void run() {

                while (scenicView == null) {
                    try {
                        scenicView = new RemoteScenicViewFinder("127.0.0.1", 7654).getRemoteBrowser();

                        sleep(10000);
                    } catch (final Exception e) {

                    }
                }
                try {
                    scenicView.dispatchEvent(new MousePosEvent(StageController.STAGE_ID, "300x300"));
                } catch (final RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        };
        remoteBrowserFinder.start();
    }

}
