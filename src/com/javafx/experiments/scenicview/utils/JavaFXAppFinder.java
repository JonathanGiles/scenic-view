package com.javafx.experiments.scenicview.utils;


import com.sun.jdi.PathSearchingVirtualMachine;
import com.sun.tools.attach.*;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jonathan
 */
public class JavaFXAppFinder {
    private static final String JAVAFX_SYSTEM_PROPERTIES_KEY = "javafx.version";
    
    public List<VirtualMachine> getRunningJavaFXApplications() {
        List<VirtualMachineDescriptor> machines = VirtualMachine.list();
        List<VirtualMachine> javaFXMachines = new ArrayList<VirtualMachine>();
        
        for (int i = 0; i < machines.size(); i++) {
            VirtualMachineDescriptor vmd = machines.get(i);
            try {
                VirtualMachine virtualMachine = VirtualMachine.attach(vmd);
                Map sysPropertiesMap = virtualMachine.getSystemProperties();
                
                if (sysPropertiesMap.containsKey(JAVAFX_SYSTEM_PROPERTIES_KEY)) {
                    javaFXMachines.add(virtualMachine);
                } else {
                    virtualMachine.detach();
                }
            } catch (AttachNotSupportedException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
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
    
    public static void main(String[] args) {
        List<VirtualMachine> machines = new JavaFXAppFinder().getRunningJavaFXApplications();
        
        File f = new File("./dist/ScenicView.jar");
        System.out.println(f.getAbsolutePath());
        
        try {
            for (VirtualMachine machine : machines) {
                machine.loadAgent(f.getAbsolutePath());
                machine.detach();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
