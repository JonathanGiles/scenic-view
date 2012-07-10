package com.javafx.experiments.scenicview.connector.remote;

import java.io.*;
import java.net.*;
import java.rmi.*;
import java.rmi.ConnectException;
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
import com.javafx.experiments.scenicview.update.RemoteVMsUpdateStrategy;
import com.sun.javafx.application.PlatformImpl;
import com.sun.tools.attach.*;

public class RemoteScenicViewImpl extends UnicastRemoteObject implements RemoteScenicView {

    /**
     * 
     */
    private static final long serialVersionUID = -8263538629805832734L;
    public static RemoteScenicViewImpl server;
    private static ScenicView view;
    private static RemoteVMsUpdateStrategy strategy;

    private final Map<Integer, String> vmInfo = new HashMap<Integer, String>();
    private final Map<String, RemoteApplication> applications = new HashMap<String, RemoteApplication>();
    private AppEventDispatcher dispatcher;
    private final List<AppEvent> previous = new ArrayList<AppEvent>();
    private List<AppController> apps;
    private final AtomicInteger count = new AtomicInteger();
    private final int port;

    public boolean first = true;

    public RemoteScenicViewImpl(final ScenicView view) throws RemoteException {
        super();
        this.port = getValidPort();
        RMIUtils.bindScenicView(this, port);
    }

    @Override public void dispatchEvent(final AppEvent event) {
        if (dispatcher != null) {
            Platform.runLater(new Runnable() {

                @Override public void run() {
                    synchronized (previous) {
                        if (!previous.isEmpty()) {
                            for (int i = 0; i < previous.size(); i++) {
                                dispatcher.dispatchEvent(previous.get(i));
                            }
                            previous.clear();
                        }
                    }
                    dispatcher.dispatchEvent(event);
                }
            });

        } else {
            synchronized (previous) {
                previous.add(event);
            }

        }
    }

    @Override public void onAgentStarted(final int port) {
        if (first)
            System.out.println("Remote agent started on port:" + port);
        RMIUtils.findApplication(port, new Observer() {

            @Override public void update(final Observable o, final Object obj) {
                final RemoteApplication application = (RemoteApplication) obj;
                applications.put(vmInfo.get(port), application);
                try {
                    final int appsID = Integer.parseInt(vmInfo.get(port));
                    final StageID[] ids = application.getStageIDs();
                    addStages(appsID, ids, application);
                } catch (final RemoteException e1) {
                    e1.printStackTrace();
                }
                count.decrementAndGet();
            }
        });
    }

    private void addStages(final int appsID, final StageID[] ids, final RemoteApplication application) {
        final AppControllerImpl impl = new AppControllerImpl(appsID, Integer.toString(appsID));
        for (int i = 0; i < ids.length; i++) {
            if (first)
                System.out.println("RemoteApp connected on:" + port + " stageID:" + ids[i]);
            final int cont = i;
            impl.getStages().add(new StageController() {

                StageID id = new StageID(appsID, ids[cont].getStageID());
                {
                    id.setName(ids[cont].getName());
                }

                @Override public StageID getID() {
                    return id;
                }

                @Override public void update() {
                    try {
                        application.update(getID());
                    } catch (final RemoteException e) {
                        e.printStackTrace();
                    }
                }

                @Override public void configurationUpdated(final Configuration configuration) {
                    try {
                        application.configurationUpdated(getID(), configuration);
                    } catch (final RemoteException e) {
                        e.printStackTrace();
                    }
                }

                @Override public void close() {
                    try {
                        application.close(getID());
                    } catch (final ConnectException e2) {
                        // Nothing to do
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }

                }

                @Override public void setEventDispatcher(final AppEventDispatcher dispatcher) {
                    RemoteScenicViewImpl.this.dispatcher = dispatcher;
                    try {
                        application.setEventDispatcher(getID(), null);
                    } catch (final RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                @Override public void setSelectedNode(final SVNode value) {
                    try {
                        application.setSelectedNode(getID(), value);
                    } catch (final RemoteException e) {
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
                        e.printStackTrace();
                    }
                }

                @Override public void animationsEnabled(final boolean enabled) {
                    try {
                        application.animationsEnabled(getID(), enabled);
                    } catch (final RemoteException e) {
                        e.printStackTrace();
                    }
                }

                @Override public void updateAnimations() {
                    try {
                        application.updateAnimations(getID());
                    } catch (final RemoteException e) {
                        e.printStackTrace();
                    }
                }

                @Override public void pauseAnimation(final int animationID) {
                    try {
                        application.pauseAnimation(getID(), animationID);
                    } catch (final RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        if (!impl.getStages().isEmpty())
            apps.add(impl);
    }

    public static void start() {
        strategy = new RemoteVMsUpdateStrategy();
        PlatformImpl.startup(new Runnable() {

            @Override public void run() {
                final Stage stage = new Stage();
                // workaround for RT-10714
                stage.setWidth(640);
                stage.setHeight(800);
                stage.setTitle("Scenic View v" + ScenicView.VERSION);
                view = new ScenicView(strategy, stage);
                ScenicView.show(view, stage);
            }
        });
        while (view == null) {
            try {
                Thread.sleep(50);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            server = new RemoteScenicViewImpl(view);
        } catch (final RemoteException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    public List<AppController> connect() {
        apps = new ArrayList<AppController>();
        vmInfo.clear();
        final List<VirtualMachine> machines = getRunningJavaFXApplications();
        if (first)
            System.out.println(machines.size() + " JavaFX VMs found");
        count.set(machines.size());
        final File f = new File("./ScenicView.jar");
        if (first)
            System.out.println("Loading agent from file:" + f.getAbsolutePath());

        try {
            for (final VirtualMachine machine : machines) {
                final VirtualMachine temp = machine;
                boolean connected = false;
                if (applications.containsKey(temp.id())) {
                    final RemoteApplication application = applications.get(temp.id());
                    try {
                        final int appsID = Integer.parseInt(temp.id());
                        final StageID[] ids = application.getStageIDs();
                        addStages(appsID, ids, application);
                        connected = true;
                        count.decrementAndGet();
                    } catch (final Exception e) {
                        applications.remove(temp.id());
                    }
                }
                if (!connected) {
                    new Thread() {
                        @Override public void run() {
                            System.out.println("Loading agent!!!!");
                            loadAgent(temp, f);
                        }
                    }.start();
                }
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
        first = false;
        return apps;
    }

    public void close() {
        try {
            RMIUtils.unbindScenicView(port);
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

    private int getValidPort() {
        int port = RMIUtils.getClientPort();
        boolean valid = false;
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
        return port;
    }

    private void loadAgent(final VirtualMachine machine, final File f) {
        try {
            final long start = System.currentTimeMillis();
            final int port = getValidPort();
            if (first)
                System.out.println("Loading agent for:" + machine + " on port:" + port + " took:" + (System.currentTimeMillis() - start) + "ms");
            vmInfo.put(port, machine.id());
            machine.loadAgent(f.getAbsolutePath(), Integer.toString(port) + ":" + this.port + ":" + machine.id());
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
