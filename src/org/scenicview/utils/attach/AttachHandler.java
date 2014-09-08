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
package org.scenicview.utils.attach;

import java.io.File;
import java.util.List;

/**
 *
 */
public interface AttachHandler {
    
    public void getOrderedJDKPaths(List<JDKToolsJarPair> jdkPaths);

    /**
     * We are given a JDKToolsJarPair where it is assumed the jdkPath is known
     * and the path to the tools.jar is unknown. We need to return the path
     * to the tools.jar file.
     */
    public File resolveToolsJarPath(JDKToolsJarPair jdkPath);
}
