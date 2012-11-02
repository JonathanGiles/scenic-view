/*
 * Copyright (c) 2012 Oracle and/or its affiliates.
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
package com.javafx.experiments.fxconnector.node;

import java.util.List;

import javafx.scene.*;

import com.javafx.experiments.fxconnector.Configuration;

public class SVNodeFactory {

    private SVNodeFactory() {
        // TODO Auto-generated constructor stub
    }

    public static SVNode createNode(final Node node, final Configuration configuration, final boolean remote) {
        if (remote) {
            /**
             * This may sound strange but if the node has a parent we create an
             * SVNode for the parent a we get the correct node latter and if it
             * has not then we create a normal node
             */
            final Node parent = node.getParent();
            if (parent != null) {
                final SVRemoteNodeAdapter svparent = new SVRemoteNodeAdapter(parent, configuration.isCollapseControls(), configuration.isCollapseContentControls());
                final List<SVNode> childrens = svparent.getChildren();
                for (int i = 0; i < childrens.size(); i++) {
                    if (childrens.get(i).equals(node)) {
                        return childrens.get(i);
                    }
                }
                final StringBuilder sb = new StringBuilder();
                sb.append("Error while creating node:" + node.getClass() + " id:" + node.getId()).append('\n');

                sb.append("NODE INFORMATION\n");
                dumpNodeInfo(sb, node);
                sb.append("PARENT INFORMATION\n");
                dumpNodeInfo(sb, node.getParent());
                throw new RuntimeException(sb.toString());

            } else {
                return new SVRemoteNodeAdapter(node, configuration.isCollapseControls(), configuration.isCollapseContentControls());
            }
        } else {
            return new SVRealNodeAdapter(node, configuration.isCollapseControls(), configuration.isCollapseContentControls());
        }
    }

    private static void dumpNodeInfo(final StringBuilder sb, final Node node) {
        sb.append("Node:").append(node).append(" Class:").append(node.getClass()).append(" Id:").append(node.getId()).append('\n');
        if (node instanceof Parent) {
            sb.append("Children:").append(((Parent) node).getChildrenUnmodifiable()).append('\n');
        }
    }

}
