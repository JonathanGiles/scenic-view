package com.javafx.experiments.scenicview.connector.remote;

import java.io.*;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.application.Platform;
import javafx.stage.Stage;

import com.javafx.experiments.scenicview.ScenicView;
import com.javafx.experiments.scenicview.connector.*;
import com.javafx.experiments.scenicview.connector.details.DetailPaneType;
import com.javafx.experiments.scenicview.connector.event.*;
import com.javafx.experiments.scenicview.connector.node.SVNode;
import com.sun.javafx.application.PlatformImpl;
import com.sun.tools.attach.*;

public class RemoteScenicViewImpl extends UnicastRemoteObject implements RemoteScenicView {

    public static RemoteScenicViewImpl server;
    static ScenicView view;

    Map<Integer, String> vmInfo = new HashMap<Integer, String>();
    AppEventDispatcher dispatcher;
    final List<AppEvent> previous = new ArrayList<AppEvent>();
    List<AppController> apps;
    final AtomicInteger count = new AtomicInteger();

    public RemoteScenicViewImpl(final ScenicView view) throws RemoteException {
        super();
        this.view = view;
        RMIUtils.bindScenicView(this);
    }

    @Override public void dispatchEvent(final AppEvent event) {

        if (dispatcher != null) {
            Platform.runLater(new Runnable() {

                @Override public void run() {
                    if (!previous.isEmpty()) {
                        for (int i = 0; i < previous.size(); i++) {
                            dispatcher.dispatchEvent(previous.get(i));
                        }
                        previous.clear();
                    }
                    dispatcher.dispatchEvent(event);
                }
            });

        } else {
            previous.add(event);
        }
    }

    @Override public void onAgentStarted(final int port) {
        System.out.println("Remote agent started on port:" + port);
        RMIUtils.findApplication(port, new Observer() {

            @Override public void update(final Observable o, final Object obj) {
                final RemoteApplication application = (RemoteApplication) obj;
                try {
                    final int[] ids = application.getStageIDs();
                    final String[] names = application.getStageNames();
                    final AppControllerImpl impl = new AppControllerImpl(port, vmInfo.get(port));
                    for (int i = 0; i < ids.length; i++) {
                        System.out.println("RemoteApp connected on:" + port + " stageID:" + ids[i]);
                        final int cont = i;
                        impl.getStages().add(new StageController() {

                            StageID id = new StageID(port, ids[cont]);

                            {
                                id.setName(names[cont]);
                            }

                            @Override public StageID getID() {
                                return id;
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
                                try {
                                    application.close();
                                } catch (final RemoteException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }

                            @Override public void setEventDispatcher(final AppEventDispatcher dispatcher) {
                                RemoteScenicViewImpl.this.dispatcher = dispatcher;
                                update();
                            }

                            @Override public void setSelectedNode(final SVNode value) {
                                try {
                                    application.setSelectedNode(value);
                                } catch (final RemoteException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }

                            @Override public AppController getAppController() {
                                return impl;
                            }

                            @Override public void setDetail(final DetailPaneType detailType, final int detailID, final String value) {
                                try {
                                    application.setDetail(getID(), detailType, detailID, value);
                                } catch (final RemoteException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }

                            @Override public void animationsEnabled(final boolean enabled) {
                                try {
                                    application.animationsEnabled(enabled);
                                } catch (final RemoteException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }

                            @Override public void updateAnimations() {
                                try {
                                    application.updateAnimations();
                                } catch (final RemoteException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    if (!impl.getStages().isEmpty())
                        apps.add(impl);
                } catch (final RemoteException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                count.decrementAndGet();
            }
        });
    }

    public void addVMInfo(final int port, final String id) {
        vmInfo.put(port, id);
    }

    public static void start() {
        PlatformImpl.startup(new Runnable() {

            @Override public void run() {
                final Stage stage = new Stage();
                // workaround for RT-10714
                stage.setWidth(640);
                stage.setHeight(800);
                stage.setTitle("Scenic View v" + ScenicView.VERSION);
                view = new ScenicView(new ArrayList<AppController>(), stage);
                ScenicView.show(view, stage);
            }
        });
        while (view == null) {
            try {
                Thread.sleep(50);
            } catch (final InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try {
            server = new RemoteScenicViewImpl(view);
        } catch (final RemoteException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        view.showRemoteApps(null);
        server.connect();
    }

    public void connect() {
        apps = new ArrayList<AppController>();
        vmInfo.clear();
        final List<VirtualMachine> machines = getRunningJavaFXApplications();
        System.out.println(machines.size() + " JavaFX VMs found");
        count.set(machines.size());
        final File f = new File("./ScenicView.jar");
        System.out.println("Loading agent from file:" + f.getAbsolutePath());

        try {
            for (final VirtualMachine machine : machines) {
                final VirtualMachine temp = machine;
                new Thread() {
                    @Override public void run() {
                        loadAgent(temp, f);
                    }
                }.start();
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        final long initial = System.currentTimeMillis();
        while (count.get() != 0 && System.currentTimeMillis() - initial < 10000) {
            try {
                Thread.sleep(50);
            } catch (final InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        view.showRemoteApps(apps);

    }

    private void loadAgent(final VirtualMachine machine, final File f) {
        try {

            boolean valid = false;
            final long start = System.currentTimeMillis();
            int port = RMIUtils.getClientPort();
            do {
                try {
                    final Socket socket = new Socket();
                    socket.connect(new InetSocketAddress("127.0.0.1", port), 100);
                    socket.close();
                    valid = true;
                    port = RMIUtils.getClientPort();
                } catch (final Exception e) {
                    valid = false;
                }

            } while (valid);
            System.out.println("Loading agent for:" + machine + " on port:" + port + " took:" + (System.currentTimeMillis() - start) + "ms");
            addVMInfo(port, machine.id());
            machine.loadAgent(f.getAbsolutePath(), Integer.toString(port));
            machine.detach();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private static final String JAVAFX_SYSTEM_PROPERTIES_KEY = "javafx.version";

    private List<VirtualMachine> getRunningJavaFXApplications() {
        final List<VirtualMachineDescriptor> machines = VirtualMachine.list();
        final List<VirtualMachine> javaFXMachines = new ArrayList<VirtualMachine>();

        for (int i = 0; i < machines.size(); i++) {
            final VirtualMachineDescriptor vmd = machines.get(i);
            try {
                final VirtualMachine virtualMachine = VirtualMachine.attach(vmd);
                final Properties sysPropertiesMap = virtualMachine.getSystemProperties();
                if (sysPropertiesMap.containsKey(JAVAFX_SYSTEM_PROPERTIES_KEY)) {
                    javaFXMachines.add(virtualMachine);
                } else {
                    virtualMachine.detach();
                }
            } catch (final AttachNotSupportedException ex) {
                ex.printStackTrace();
            } catch (final IOException ex) {
                ex.printStackTrace();
            }
        }

        return javaFXMachines;
    }

}
