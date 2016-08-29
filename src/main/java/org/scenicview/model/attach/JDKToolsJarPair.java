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
package org.scenicview.model.attach;

import java.io.File;

/**
 *
 */
public class JDKToolsJarPair {
    
    private final File jdkPath;
    
    private File toolsPath;
    
    public JDKToolsJarPair(String jdkPath) {
        if (jdkPath == null || jdkPath.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.jdkPath = new File(jdkPath);
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
    
    @Override public String toString() {
        return getJdkPath().getAbsolutePath();
    }
}
