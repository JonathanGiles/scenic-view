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
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LinuxAttachHandler extends AttachHandlerBase {
    
    private static final String[] commonJdkPaths = {
        "/usr/java",
        "/usr/java/jdk",
        "/usr/jdk",
        "/usr/lib/java",
        "/usr/lib/jdk",
        "/usr/lib/jvm/java",
        "/usr/lib/jvm/jdk",
        "/usr/local/java",
        "/usr/local/java/jdk",
        "/opt/java",
        "/opt/jdk"
    };
    
    @Override public void getOrderedJDKPaths(List<JDKToolsJarPair> jdkPaths) {
        doBasicJdkSearch(jdkPaths);
        
        // go down linux special path
        getToolsClassPathOnLinux(jdkPaths);
    }
    
    private void getToolsClassPathOnLinux(List<JDKToolsJarPair> jdkPaths) {
        Process p = null;

        try {
            Runtime r = Runtime.getRuntime();
            p = r.exec("which javac");
            p.waitFor();
        } catch(Exception e) {
            e.printStackTrace();
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            if (br.ready()) {
                String path = br.readLine().replaceAll("/bin/javac$", "");
                jdkPaths.add(new JDKToolsJarPair(path));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        jdkPaths.addAll(Arrays.stream(commonJdkPaths)
                              .filter(s -> Files.exists(Paths.get(s + "/lib/tools.jar")))
                              .map(s -> new JDKToolsJarPair(new File(s), new File(s + "/lib/tools.jar")))
                              .collect(Collectors.toList()));
    }
}
