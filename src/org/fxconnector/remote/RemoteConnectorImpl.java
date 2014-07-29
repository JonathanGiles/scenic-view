/*
 * Copyright (c) 2012 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.fxconnector.remote;

import org.fxconnector.StageController;
import org.fxconnector.StageID;
import org.fxconnector.Configuration;
import org.fxconnector.AppController;
import org.fxconnector.AppControllerImpl;

import java.net.*;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.application.Platform;

import org.fxconnector.details.DetailPaneType;
import org.fxconnector.event.FXConnectorEvent;
import org.fxconnector.event.FXConnectorEventDispatcher;
import org.fxconnector.node.SVNode;
import org.fxconnector.remote.util.ScenicViewExceptionLogger;

import com.sun.javafx.Utils;
import com.sun.javafx.scene.web.Debugger;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.rmi.AccessException;

class RemoteConnectorImpl extends UnicastRemoteObject implements RemoteConnector, FXConnector {

    /**
     * 
     */
    private static final long serialVersionUID = -8263538629805832734L;

    private final Map<Integer, String> vmInfo = new HashMap<Integer, String>();
    private final Map<String, RemoteApplication> applications = new HashMap<String, RemoteApplication>();
    private FXConnectorEventDispatcher dispatcher;
    private final List<FXConnectorEvent> previous = new ArrayList<FXConnectorEvent>();
    private List<AppController> apps;
    private final AtomicInteger count = new AtomicInteger();
    private final int port;
    private final List<String> attachError = new ArrayList<String>();

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
        org.fxconnector.Debugger.debug("Remote agent started on port:" + port);
        RMIUtils.findApplication(port, application -> {
            applications.put(vmInfo.get(port), application);
            try {
                final int appsID = Integer.parseInt(vmInfo.get(port));
                final StageID[] ids = application.getStageIDs();
                addStages(appsID, ids, application);
            } catch (final RemoteException e) {
                ScenicViewExceptionLogger.submitException(e);
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
            org.fxconnector.Debugger.debug("RemoteApp connected on:" + port + " stageID:" + ids[i]);
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
                        ScenicViewExceptionLogger.submitException(e);
                    }
                }

                @Override public void configurationUpdated(final Configuration configuration) {
                    try {
                        application.configurationUpdated(getID(), configuration);
                    } catch (final RemoteException e) {
                        ScenicViewExceptionLogger.submitException(e);
                    }
                }

                @Override public void close() {
                    try {
                        isOpened = false;
                        application.close(getID());
                    } catch (final ConnectException e2) {
                        // Nothing to do
                    } catch (final Exception e) {
                        ScenicViewExceptionLogger.submitException(e);
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
                        ScenicViewExceptionLogger.submitException(e);
                    }
                }

                @Override public void setSelectedNode(final SVNode value) {
                    try {
                        application.setSelectedNode(getID(), value);
                    } catch (final RemoteException e) {
                        ScenicViewExceptionLogger.submitException(e);
                    }
                }

                @Override public void removeSelectedNode() {
                    try {
                        application.removeSelectedNode(getID());
                    } catch (final RemoteException e) {
                        ScenicViewExceptionLogger.submitException(e);
                    }
                }

                @Override public AppController getAppController() {
                    return impl;
                }

                @Override public void setDetail(final DetailPaneType detailType, final int detailID, final String value) {
                    try {
                        application.setDetail(getID(), detailType, detailID, value);
                    } catch (final RemoteException e) {
                        ScenicViewExceptionLogger.submitException(e);
                    }
                }

                @Override public void animationsEnabled(final boolean enabled) {
                    try {
                        application.animationsEnabled(getID(), enabled);
                    } catch (final RemoteException e) {
                        ScenicViewExceptionLogger.submitException(e);
                    }
                }

                @Override public void updateAnimations() {
                    try {
                        application.updateAnimations(getID());
                    } catch (final RemoteException e) {
                        ScenicViewExceptionLogger.submitException(e);
                    }
                }

                @Override public void pauseAnimation(final int animationID) {
                    try {
                        application.pauseAnimation(getID(), animationID);
                    } catch (final RemoteException e) {
                        ScenicViewExceptionLogger.submitException(e);
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
        apps = new ArrayList<AppController>();
        vmInfo.clear();
        final List<VirtualMachine> machines = getRunningJavaFXApplications();
        org.fxconnector.Debugger.debug(machines.size() + " JavaFX applications found");
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
                        ScenicViewExceptionLogger.submitException(e, "Failure connecting to machine.");
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
            ScenicViewExceptionLogger.submitException(e);
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
        org.fxconnector.Debugger.setDebug(false);
        return apps;
    }

    @Override public void close() {
        try {
            RMIUtils.unbindScenicView(port);
        } catch (final Exception e) {
            ScenicViewExceptionLogger.submitException(e);
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
            org.fxconnector.Debugger.debug("Loading agent for:" + machine + " ID:" + machine.id() + " on port:" + port + " took:" + (System.currentTimeMillis() - start) + "ms using agent defined in " + agentFile.getAbsolutePath());
            vmInfo.put(port, machine.id());
            machine.loadAgent(agentFile.getAbsolutePath(), Integer.toString(port) + ":" + this.port + ":" + machine.id() + ":" + org.fxconnector.Debugger.isDebug());
            machine.detach();
        } catch (final Exception e) {
            ScenicViewExceptionLogger.submitException(e);
        }
    }

    private static final String JAVAFX_SYSTEM_PROPERTIES_KEY = "javafx.version";

    private List<VirtualMachine> getRunningJavaFXApplications() {
        final List<VirtualMachineDescriptor> machines = VirtualMachine.list();
        org.fxconnector.Debugger.debug("Number of running Java applications found: " + machines.size());
        final List<VirtualMachine> javaFXMachines = new ArrayList<VirtualMachine>();

        final Map<String, Properties> vmsProperties = new HashMap<String, Properties>(machines.size());
        for (int i = 0; i < machines.size(); i++) {
            final VirtualMachineDescriptor vmd = machines.get(i);
            try {
                final VirtualMachine virtualMachine = VirtualMachine.attach(vmd);
                org.fxconnector.Debugger.debug("Obtaining properties for Java application with PID:" + virtualMachine.id());
                final Properties sysPropertiesMap = virtualMachine.getSystemProperties();
                vmsProperties.put(virtualMachine.id(), sysPropertiesMap);
                if (sysPropertiesMap != null && sysPropertiesMap.containsKey(JAVAFX_SYSTEM_PROPERTIES_KEY) && !sysPropertiesMap.containsKey(SCENIC_VIEW_VM)) {
                    javaFXMachines.add(virtualMachine);
                } else {
                    virtualMachine.detach();
                }
//                org.fxconnector.Debugger.debug("JVM:" + virtualMachine.id() + " detection finished");
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
            ScenicViewExceptionLogger.submitException(e, "Attempting to get agent jar.");
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
        org.fxconnector.Debugger.debug("Loading agent from:" + tempf.getAbsolutePath());
        return tempf;
    }

}
