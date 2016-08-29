/*
 * Scenic View, 
 * Copyright (C) 2013 Jonathan Giles, Ander Ruiz, Amy Fowler 
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

import static org.scenicview.model.attach.AttachHandlerFactory.doBasicJdkSearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 *
 */
public class MacAttachHandler extends AttachHandlerBase {
    private static final String[] PATHS_TO_TOOLS_JAR = new String[] {
        "Contents/Home/lib/tools.jar",
        "lib/tools.jar"
    };
    
    @Override public void getOrderedJDKPaths(List<JDKToolsJarPair> jdkPaths) {
        doBasicJdkSearch(jdkPaths);
        
        // go down mac special path
        getToolsClassPathOnMAC(jdkPaths);
    }
    
    private void getToolsClassPathOnMAC(List<JDKToolsJarPair> jdkPaths) {
        Process process = null;
        try {
            Runtime runtime = Runtime.getRuntime();
            process = runtime.exec("/usr/libexec/java_home -V");
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
            
        try(
            InputStream inputStream = process.getErrorStream();
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader)
        ) {
            if (bufferedReader.ready()) {
                bufferedReader.readLine();
            }
            while (bufferedReader.ready()) {
                String versionString = bufferedReader.readLine();
                versionString = versionString.trim();
                
                String path;
                String[] splitted = versionString.split("\t");
                
                if (splitted.length == 3) {
                    path = splitted[splitted.length - 1];
                    
                    final File jdkHome = new File(path);
                    final File toolsFile = searchForToolsJar(jdkHome);
                    if (toolsFile != null && toolsFile.exists()) {
                        jdkPaths.add(new JDKToolsJarPair(jdkHome, toolsFile));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File searchForToolsJar(File jdkHome) {
        for (String pathToToolsJar : PATHS_TO_TOOLS_JAR) {
            final File toolsFile = new File(jdkHome, pathToToolsJar);
            if (toolsFile.exists()) {
                return toolsFile;
            }
        }
        return null;
    }
}
