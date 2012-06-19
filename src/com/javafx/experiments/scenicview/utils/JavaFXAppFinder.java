package com.javafx.experiments.scenicview.utils;


import java.io.*;
import java.util.*;

import javafx.stage.Stage;

import com.sun.tools.attach.*;

/**
 *
 * @author Jonathan
 */
public class JavaFXAppFinder {
    
    private static final List<Stage> stages = new ArrayList<Stage>();
    
    private static final String JAVAFX_SYSTEM_PROPERTIES_KEY = "javafx.version";
    
    public List<VirtualMachine> getRunningJavaFXApplications() {
        final List<VirtualMachineDescriptor> machines = VirtualMachine.list();
        final List<VirtualMachine> javaFXMachines = new ArrayList<VirtualMachine>();
        
        for (int i = 0; i < machines.size(); i++) {
            final VirtualMachineDescriptor vmd = machines.get(i);
            try {
                final VirtualMachine virtualMachine = VirtualMachine.attach(vmd);
                final Map sysPropertiesMap = virtualMachine.getSystemProperties();
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
    
    public JavaFXAppFinder() {
        final List<VirtualMachine> machines = getRunningJavaFXApplications();
        
        for (final Iterator<VirtualMachine> iterator = machines.iterator(); iterator.hasNext();) {
            final VirtualMachine virtualMachine = iterator.next();
            System.out.println(virtualMachine);
        }
        final File f = new File("./ScenicView.jar");
        System.out.println(f.getAbsolutePath());
        
        try {
            for (final VirtualMachine machine : machines) {
                System.out.println("Loading agent for:"+machine);
                machine.loadAgent(f.getAbsolutePath());
                machine.detach();
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        for (final Iterator<Stage> iterator = stages.iterator(); iterator.hasNext();) {
            final Stage stage = iterator.next();
            System.out.println(stage);
        }
    }
}
