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
 package org.scenicview.view;

import org.fxconnector.node.SVNode;

interface NodeFilter {
    /**
     * Checks if the node is accepted for this filter
     * 
     * @param node
     * @return
     */
    public boolean accept(SVNode node);

    /**
     * Checks if the children could be accepted even though this node is
     * rejected
     * 
     * @return
     */
    public boolean allowChildrenOnRejection();

    /**
     * Flag to hide always nodes
     * 
     * @return
     */
    public boolean ignoreShowFilteredNodesInTree();

    /**
     * Flag to indicate if all the nodes must be expanded on filtering
     * 
     * @return
     */
    public boolean expandAllNodes();

}