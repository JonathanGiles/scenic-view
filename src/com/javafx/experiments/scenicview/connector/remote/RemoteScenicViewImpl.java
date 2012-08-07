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
import com.sun.javafx.Utils;
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

    private static boolean debug = false;

    public static void setDebug(final boolean debug) {
        RemoteScenicViewImpl.debug = debug;
    }

    private static void debug(final String debug) {
        if (RemoteScenicViewImpl.debug) {
            System.out.println(debug);
        }
    }

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
        debug("Remote agent started on port:" + port);
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
        final AppControllerImpl impl = new AppControllerImpl(appsID, Integer.toString(appsID)) {

            @Override public void close() {
                // TODO Auto-generated method stub
                super.close();
                try {
                    application.close();
                } catch (final RemoteException e) {
                    // Nothing to do
                }
            }

        };
        for (int i = 0; i < ids.length; i++) {
            debug("RemoteApp connected on:" + port + " stageID:" + ids[i]);
            final int cont = i;
            impl.getStages().add(new StageController() {

                StageID id = new StageID(appsID, ids[cont].getStageID());
                private boolean isOpened;
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
                        isOpened = false;
                        application.close(getID());
                    } catch (final ConnectException e2) {
                        // Nothing to do
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }

                }

                @Override public boolean isOpened() {
                    return isOpened;
                }

                @Override public void setEventDispatcher(final AppEventDispatcher dispatcher) {
                    isOpened = true;
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
        if (!impl.getStages().isEmpty()) {
            apps.add(impl);
        } else {
            // The application has no stages, close agent
            impl.close();
            applications.remove(Integer.toString(impl.getID()));
        }
    }

    public static void start() {
        strategy = new RemoteVMsUpdateStrategy();
        final Thread current = Thread.currentThread();
        new Thread() {
            @Override public void run() {
                while (view == null) {
                    try {
                        Thread.sleep(20000);
                    } catch (final InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if (view == null) {
                        System.out.println("ScenicView blocked?, stackTrace:");
                        final StackTraceElement[] trace = current.getStackTrace();
                        for (int i = 0; i < trace.length; i++) {
                            System.out.println(trace[i]);
                        }
                    }
                }
            }
        }.start();
        PlatformImpl.startup(new Runnable() {

            @Override public void run() {
                System.out.println("Platform running");
                final Stage stage = new Stage();
                // workaround for RT-10714
                stage.setWidth(640);
                stage.setHeight(800);
                stage.setTitle("Scenic View v" + ScenicView.VERSION);
                view = new ScenicView(strategy, stage);
                ScenicView.show(view, stage);
            }
        });
        System.out.println("Startup done");
        while (view == null) {
            try {
                Thread.sleep(500);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Creating server");
        try {
            server = new RemoteScenicViewImpl(view);
        } catch (final RemoteException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        System.out.println("Server done");
    }

    public List<AppController> connect() {
        apps = new ArrayList<AppController>();
        vmInfo.clear();
        final List<VirtualMachine> machines = getRunningJavaFXApplications();
        debug(machines.size() + " JavaFX VMs found");
        count.set(machines.size());
        final File f = new File("./ScenicView.jar");
        debug("Loading agent from file:" + f.getAbsolutePath());

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
                            loadAgent(temp, f);
                        }
                    }.start();
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        final long initial = System.currentTimeMillis();
        /**
         * MAC Seems to be slower using attach API
         */
        final long timeout = Utils.isMac() ? 30000 : 10000;
        while (count.get() != 0 && System.currentTimeMillis() - initial < timeout) {
            try {
                Thread.sleep(50);
            } catch (final InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        debug = false;
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
            debug("Loading agent for:" + machine + " ID:" + machine.id() + " on port:" + port + " took:" + (System.currentTimeMillis() - start) + "ms");
            vmInfo.put(port, machine.id());
            machine.loadAgent(f.getAbsolutePath(), Integer.toString(port) + ":" + this.port + ":" + machine.id() + ":" + debug);
            machine.detach();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private static final String JAVAFX_SYSTEM_PROPERTIES_KEY = "javafx.version";

    private List<VirtualMachine> getRunningJavaFXApplications() {
        final List<VirtualMachineDescriptor> machines = VirtualMachine.list();
        debug("Number of JVMs:" + machines.size());
        final List<VirtualMachine> javaFXMachines = new ArrayList<VirtualMachine>();

        final Map<String, Properties> vmsProperties = new HashMap<String, Properties>(machines.size());
        for (int i = 0; i < machines.size(); i++) {
            final VirtualMachineDescriptor vmd = machines.get(i);
            try {
                final VirtualMachine virtualMachine = VirtualMachine.attach(vmd);
                debug("Obtaining properties for JVM:" + virtualMachine.id());
                final Properties sysPropertiesMap = virtualMachine.getSystemProperties();
                vmsProperties.put(virtualMachine.id(), sysPropertiesMap);
                if (sysPropertiesMap != null && sysPropertiesMap.containsKey(JAVAFX_SYSTEM_PROPERTIES_KEY)) {
                    javaFXMachines.add(virtualMachine);
                } else {
                    virtualMachine.detach();
                }
                debug("JVM:" + virtualMachine.id() + " detection finished");
            } catch (final AttachNotSupportedException ex) {
                ex.printStackTrace();
            } catch (final IOException ex) {
                ex.printStackTrace();
            }
        }
        /**
         * We always have at least one JFX VM
         */
        if (debug && javaFXMachines.size() <= 1 && machines.size() > 1) {
            debug("No JavaFX VM found? dumping properties");
            for (final Iterator<String> iterator = vmsProperties.keySet().iterator(); iterator.hasNext();) {
                final String id = iterator.next();

                final Properties properties = vmsProperties.get(id);
                if (!properties.containsKey(JAVAFX_SYSTEM_PROPERTIES_KEY)) {
                    debug("ID:" + id);
                    for (@SuppressWarnings("rawtypes") final Iterator iterator2 = properties.keySet().iterator(); iterator2.hasNext();) {
                        final String value = (String) iterator2.next();
                        debug("\t" + value + "=" + properties.getProperty(value));
                    }
                }

            }
        }

        return javaFXMachines;
    }

}
