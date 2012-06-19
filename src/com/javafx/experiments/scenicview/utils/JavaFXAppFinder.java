package com.javafx.experiments.scenicview.utils;


import java.io.*;
import java.util.*;

import javafx.stage.Stage;

import com.sun.tools.attach.*;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jonathan
 */
public class JavaFXAppFinder {
    
    public static final List<Stage> stages = new ArrayList<Stage>();
    
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
    
//    public int getPID() {
//        String name = ManagementFactory.getRuntimeMXBean().getName();
//        String pidString = name.substring(0, name.indexOf("@"));
//        return Integer.parseInt(pidString);
//    }
    
    public static void main(final String[] args) {
        final List<VirtualMachine> machines = new JavaFXAppFinder().getRunningJavaFXApplications();
        
        for (final Iterator iterator = machines.iterator(); iterator.hasNext();) {
            final VirtualMachine virtualMachine = (VirtualMachine) iterator.next();
            System.out.println(virtualMachine);
        }
        
        final File f = new File("./dist/ScenicView.jar");
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
        for (final Iterator iterator = stages.iterator(); iterator.hasNext();) {
            final Stage stage = (Stage) iterator.next();
            System.out.println(stage);
        }
    }
}
