package com.javafx.experiments.scenicview.connector.remote;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import javafx.application.Platform;

import com.javafx.experiments.scenicview.ScenicView;
import com.javafx.experiments.scenicview.connector.*;
import com.javafx.experiments.scenicview.connector.event.*;
import com.javafx.experiments.scenicview.connector.node.SVNode;

public class RemoteScenicViewImpl extends UnicastRemoteObject implements RemoteScenicView {

    ScenicView view;

    Map<Integer, String> vmInfo = new HashMap<Integer, String>();
    AppEventDispatcher dispatcher;

    public RemoteScenicViewImpl(final ScenicView view) throws RemoteException {
        super();
        this.view = view;
        RMIUtils.bindScenicView(this);
    }

    @Override public void dispatchEvent(final AppEvent event) {
        System.out.println(event.getType());
        if (dispatcher != null) {
            Platform.runLater(new Runnable() {

                @Override public void run() {
                    dispatcher.dispatchEvent(event);
                }
            });

        }
    }

    @Override public void onAgentStarted(final int port) {
        System.out.println("Agent started on port:" + port);
        RMIUtils.findApplication(port, new Observer() {

            @Override public void update(final Observable o, final Object obj) {
                final RemoteApplication application = (RemoteApplication) obj;
                try {
                    final int[] ids = application.getStageIDs();
                    final AppControllerImpl impl = new AppControllerImpl(port, vmInfo.get(port));
                    for (int i = 0; i < ids.length; i++) {
                        final int cont = i;
                        impl.getStages().add(new StageController() {

                            @Override public StageID getID() {
                                // TODO Auto-generated method stub
                                return new StageID(port, ids[cont]);
                            }

                            @Override public void update() {
                                try {
                                    application.update();
                                } catch (final RemoteException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }

                            @Override public void configurationUpdated(final Configuration configuration) {
                                try {
                                    application.configurationUpdated(configuration);
                                } catch (final RemoteException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }

                            @Override public void close() {
                                // TODO Auto-generated method stub

                            }

                            @Override public void setEventDispatcher(final AppEventDispatcher dispatcher) {
                                RemoteScenicViewImpl.this.dispatcher = dispatcher;
                                update();
                            }

                            @Override public void setSelectedNode(final SVNode value) {
                                // TODO Auto-generated method stub

                            }

                            @Override public AppController getAppController() {
                                return impl;
                            }
                        });
                    }
                    if (!impl.getStages().isEmpty())
                        view.addNewApp(impl);
                } catch (final RemoteException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

            }
        });
    }

    public void addVMInfo(final int port, final String id) {
        vmInfo.put(port, id);
    }

}
