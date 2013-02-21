/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scenicview.utils.attach;

import java.io.File;

/**
 *
 */
public class JDKToolsJarPair {
    
    private final File jdkPath;
    
    private File toolsPath;
    
    public JDKToolsJarPair(String jdkPath) {
        this(new File(jdkPath));
    }
    
    public JDKToolsJarPair(File jdkPath) {
        this.jdkPath = jdkPath;
    }

    public JDKToolsJarPair(File jdkPath, File toolsPath) {
        this.jdkPath = jdkPath;
        this.toolsPath = toolsPath;
    }

    public File getJdkPath() {
        return jdkPath;
    }

    public File getToolsPath(AttachHandler attachHandler) {
        if (toolsPath == null) {
            // attempt to resolve path
            toolsPath = attachHandler.resolveToolsJarPath(this);
        }
        return toolsPath;
    }
}
