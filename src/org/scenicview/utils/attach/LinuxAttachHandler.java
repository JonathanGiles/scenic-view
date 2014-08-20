/*
 * Copyright (c) 2013 Oracle and/or its affiliates.
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
package org.scenicview.utils.attach;

import static org.scenicview.utils.attach.AttachHandlerFactory.doBasicJdkSearch;

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
