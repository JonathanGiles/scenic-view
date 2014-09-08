/*
 * Scenic View, 
 * Copyright (C) 2012 Jonathan Giles, Ander Ruiz, Amy Fowler 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fxconnector.remote;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.application.Platform;

import org.fxconnector.AppController;
import org.fxconnector.AppControllerImpl;
import org.fxconnector.Configuration;
import org.fxconnector.StageController;
import org.fxconnector.StageID;
import org.fxconnector.details.DetailPaneType;
import org.fxconnector.event.FXConnectorEvent;
import org.fxconnector.event.FXConnectorEventDispatcher;
import org.fxconnector.node.SVNode;
import org.scenicview.utils.ExceptionLogger;
import org.scenicview.utils.Logger;

import com.sun.javafx.Utils;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

class RemoteConnectorImpl extends UnicastRemoteObject implements RemoteConnector, FXConnector {

    private static final long serialVersionUID = -8263538629805832734L;

    private final Map<Integer, String> vmInfo = new HashMap<>();
    private final Map<String, RemoteApplication> applications = new HashMap<>();
    private FXConnectorEventDispatcher dispatcher;
    private final List<FXConnectorEvent> previous = new ArrayList<>();
    private List<AppController> apps;
    private final AtomicInteger count = new AtomicInteger();
    private final int port;
    private final List<String> attachError = new ArrayList<>();

    private File agentFile;

    RemoteConnectorImpl() throws RemoteException {
        super();
        this.port = getValidPort();
        RMIUtils.bindScenicView(this, port);
    }

    @Override public void dispatchEvent(final FXConnectorEvent event) {
        if (dispatcher != null) {
            Platform.runLater(() -> {
                synchronized (previous) {
                    if (!previous.isEmpty()) {
                        for (int i = 0; i < previous.size(); i++) {
                            dispatcher.dispatchEvent(previous.get(i));
                        }
                        previous.clear();
                    }
                }
                dispatcher.dispatchEvent(event);
            });
        } else {
            synchronized (previous) {
                previous.add(event);
            }
        }
    }

    @Override public void onAgentStarted(final int port) {
        Logger.print("Remote agent started on port:" + port);
        RMIUtils.findApplication(port, application -> {
            applications.put(vmInfo.get(port), application);
            try {
                final int appsID = Integer.parseInt(vmInfo.get(port));
                final StageID[] ids = application.getStageIDs();
                addStages(appsID, ids, application);
            } catch (final RemoteException e) {
                ExceptionLogger.submitException(e);
            }
            count.decrementAndGet();
        });
    }

    private void addStages(final int appsID, final StageID[] ids, final RemoteApplication application) {
        final AppControllerImpl impl = new AppControllerImpl(appsID, Integer.toString(appsID)) {
            @Override public void close() {
                super.close();
                try {
                    application.close();
                } catch (final RemoteException e) {
                    // Nothing to do
                }
            }
        };
        
        for (int i = 0; i < ids.length; i++) {
            Logger.print("RemoteApp connected on:" + port + " stageID:" + ids[i]);
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
                        ExceptionLogger.submitException(e);
                    }
                }

                @Override public void configurationUpdated(final Configuration configuration) {
                    try {
                        application.configurationUpdated(getID(), configuration);
                    } catch (final RemoteException e) {
                        ExceptionLogger.submitException(e);
                    }
                }

                @Override public void close() {
                    try {
                        isOpened = false;
                        application.close(getID());
                    } catch (final ConnectException e2) {
                        // Nothing to do
                    } catch (final Exception e) {
                        ExceptionLogger.submitException(e);
                    }

                }

                @Override public boolean isOpened() {
                    return isOpened;
                }

                @Override public void setEventDispatcher(final FXConnectorEventDispatcher dispatcher) {
                    isOpened = true;
                    RemoteConnectorImpl.this.dispatcher = dispatcher;
                    try {
                        application.setEventDispatcher(getID(), null);
                    } catch (final RemoteException e) {
                        // TODO Auto-generated catch block
                        ExceptionLogger.submitException(e);
                    }
                }

                @Override public void setSelectedNode(final SVNode value) {
                    try {
                        application.setSelectedNode(getID(), value);
                    } catch (final RemoteException e) {
                        ExceptionLogger.submitException(e);
                    }
                }

                @Override public void removeSelectedNode() {
                    try {
                        application.removeSelectedNode(getID());
                    } catch (final RemoteException e) {
                        ExceptionLogger.submitException(e);
                    }
                }

                @Override public AppController getAppController() {
                    return impl;
                }

                @Override public void setDetail(final DetailPaneType detailType, final int detailID, final String value) {
                    try {
                        application.setDetail(getID(), detailType, detailID, value);
                    } catch (final RemoteException e) {
                        ExceptionLogger.submitException(e);
                    }
                }

                @Override public void animationsEnabled(final boolean enabled) {
                    try {
                        application.animationsEnabled(getID(), enabled);
                    } catch (final RemoteException e) {
                        ExceptionLogger.submitException(e);
                    }
                }

                @Override public void updateAnimations() {
                    try {
                        application.updateAnimations(getID());
                    } catch (final RemoteException e) {
                        ExceptionLogger.submitException(e);
                    }
                }

                @Override public void pauseAnimation(final int animationID) {
                    try {
                        application.pauseAnimation(getID(), animationID);
                    } catch (final RemoteException e) {
                        ExceptionLogger.submitException(e);
                    }
                }
            });
        }
        if (!impl.getStages().isEmpty()) {
            apps.add(impl);
        } else {
            /**
             * Keep the agent connected
             */
        }
    }

    /**
     * This method is periodically call to connect to remote VM that may have JavaFX Application running on them
     */
    @Override public List<AppController> connect() {
        apps = new ArrayList<>();
        vmInfo.clear();
        final List<VirtualMachine> machines = getRunningJavaFXApplications();
        Logger.print(machines.size() + " JavaFX applications found");
        count.set(machines.size());
        if (agentFile == null) {
            agentFile = findAgent();
        }
        try {
            final List<String> validIDs = new ArrayList<>();
            for (final VirtualMachine machine : machines) {
                validIDs.add(machine.id());
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
                        ExceptionLogger.submitException(e, "Failure connecting to machine.");
                        applications.remove(temp.id());
                    }
                }
                if (!connected) {
                    new Thread() {
                        @Override public void run() {
                            loadAgent(temp, agentFile);
                        }
                    }.start();
                }
            }
            /**
             * Remove obsolete VM
             */
            for (Iterator<String> iterator = applications.keySet().iterator(); iterator.hasNext();) {
                String ids = (String) iterator.next();
                if(!validIDs.contains(ids)) {
                    iterator.remove();
                }
            }
        } catch (final Exception e) {
            ExceptionLogger.submitException(e);
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
                // no-op
            }
        }
        Logger.setEnabled(false);
        return apps;
    }

    @Override public void close() {
        try {
            RMIUtils.unbindScenicView(port);
        } catch (final Exception e) {
            ExceptionLogger.submitException(e);
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

    private void loadAgent(final VirtualMachine machine, final File agentFile) {
        try {
            final long start = System.currentTimeMillis();
            final int port = getValidPort();
            Logger.print("Loading agent for:" + machine + " ID:" + machine.id() + " on port:" + port + " took:" + (System.currentTimeMillis() - start) + "ms using agent defined in " + agentFile.getAbsolutePath());
            vmInfo.put(port, machine.id());
            machine.loadAgent(agentFile.getAbsolutePath(), Integer.toString(port) + ":" + this.port + ":" + machine.id() + ":" + Logger.isEnabled());
            machine.detach();
        } catch (final Exception e) {
            ExceptionLogger.submitException(e);
        }
    }

    private static final String JAVAFX_SYSTEM_PROPERTIES_KEY = "javafx.version";

    private List<VirtualMachine> getRunningJavaFXApplications() {
        final List<VirtualMachineDescriptor> machines = VirtualMachine.list();
        Logger.print("Number of running Java applications found: " + machines.size());
        final List<VirtualMachine> javaFXMachines = new ArrayList<>();

        final Map<String, Properties> vmsProperties = new HashMap<>(machines.size());
        for (int i = 0; i < machines.size(); i++) {
            final VirtualMachineDescriptor vmd = machines.get(i);
            try {
                final VirtualMachine virtualMachine = VirtualMachine.attach(vmd);
                Logger.print("Obtaining properties for Java application with PID:" + virtualMachine.id());
                final Properties sysPropertiesMap = virtualMachine.getSystemProperties();
                vmsProperties.put(virtualMachine.id(), sysPropertiesMap);
                if (sysPropertiesMap != null && sysPropertiesMap.containsKey(JAVAFX_SYSTEM_PROPERTIES_KEY)/* && !sysPropertiesMap.containsKey(SCENIC_VIEW_VM)*/) {
                    javaFXMachines.add(virtualMachine);
                } else {
                    virtualMachine.detach();
                }
//                Logger.print("JVM:" + virtualMachine.id() + " detection finished");
            } catch (final AttachNotSupportedException ex) {
                dumpAttachError(vmd, ex);
            } catch (final IOException ex) {
                dumpAttachError(vmd, ex);
            } catch (final InternalError ex) {
                dumpAttachError(vmd, ex);
            }

        }
//        if (debug && javaFXMachines.isEmpty() && machines.size() > 1) {
//            debug("No running JavaFX applications found.");
//            for (final Iterator<String> iterator = vmsProperties.keySet().iterator(); iterator.hasNext();) {
//                final String id = iterator.next();
//
//                final Properties properties = vmsProperties.get(id);
//                if (!properties.containsKey(JAVAFX_SYSTEM_PROPERTIES_KEY)) {
//                    debug("ID:" + id);
//                    for (@SuppressWarnings("rawtypes") final Iterator iterator2 = properties.keySet().iterator(); iterator2.hasNext();) {
//                        final String value = (String) iterator2.next();
//                        debug("\t" + value + "=" + properties.getProperty(value));
//                    }
//                }
//            }
//        }

        return javaFXMachines;
    }

    private void dumpAttachError(final VirtualMachineDescriptor vmd, final Throwable ex) {
        if (!attachError.contains(vmd.id())) {
            attachError.add(vmd.id());
            System.err.println("Error while obtaining properties for JVM:" + vmd);
            ex.printStackTrace();
        }
    }
    
    private File findAgent() {
        File tempf = null;

        try {
            URL url = RuntimeAttach.class.getResource("/org/fxconnector/remote/RuntimeAttach.class");
            if (url.getProtocol().equals("jar")) {
                String urlFile = url.getFile();
                String fileUrl = urlFile.substring(0, urlFile.indexOf('!'));
                tempf = new File(new URL(fileUrl).toURI());
            }
        } catch (MalformedURLException | URISyntaxException e) {
            ExceptionLogger.submitException(e, "Attempting to get agent jar.");
        }

        if (tempf == null || !tempf.exists()) {
            /**
             * Find jar file in the classpath
             */
            final String classPath = System.getProperty("java.class.path");
            final String pathSeparator = System.getProperty("path.separator");
            final String[] files = classPath.split(pathSeparator);
            for (int i = 0; i < files.length; i++) {
                if (files[i].toLowerCase().indexOf("scenicview.jar") != -1) {
                    tempf = new File(files[i]);
                    break;
                }
            }
        }

        if (tempf == null || !tempf.exists()) {
            // if we are here, lets check the development location, and try to get the jar from there
            final File buildLibsDir = new File("build/libs/");
            if (buildLibsDir.exists()) {
                File[] jarFiles = buildLibsDir.listFiles(new FilenameFilter() {
                    @Override public boolean accept(File f, String name) {
                        return name.endsWith(".jar");
                    }
                });

                for (File jarFile : jarFiles) {
                    if (tempf == null || jarFile.length() > tempf.length()) {
                        tempf = jarFile;
                    }
                }
            }

            // System.err.println("Cannot load the agent, ScenicView.jar not found here:" + tempf.getAbsolutePath());
        }
        Logger.print("Loading agent from: " + (tempf == null ? "null" : tempf.getAbsolutePath()));
        return tempf;
    }

}
