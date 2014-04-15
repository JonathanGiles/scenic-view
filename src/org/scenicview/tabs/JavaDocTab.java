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
package org.scenicview.tabs;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Menu;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;

import org.fxconnector.node.SVNode;
import org.scenicview.ContextMenuContainer;
import org.scenicview.DisplayUtils;
import org.scenicview.ScenicView;
import org.scenicview.control.ProgressWebView;

public class JavaDocTab extends Tab implements ContextMenuContainer {
    
    private final ScenicView scenicView;
    
    private final ProgressWebView webView;

    public JavaDocTab(final ScenicView view) {
        super("JavaDoc");
        
        this.scenicView = view;
        this.webView = new ProgressWebView();
        webView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        setContent(webView);
        setGraphic(new ImageView(DisplayUtils.getUIImage("javadoc.png")));
        setClosable(false);
        selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean newValue) {
                if (newValue) {
                    DisplayUtils.showWebView(true);
                    loadAPI(null);
                } else {
                    DisplayUtils.showWebView(false);
                }
            }
        });
    }

    @Override
    public Menu getMenu() {
        return null;
    }
    
    public void loadAPI(final String property) {
        SVNode selectedNode = scenicView.getSelectedNode();
        
        if (selectedNode == null || selectedNode.getNodeClassName() == null || !selectedNode.getNodeClassName().startsWith("javafx.")) {
            webView.doLoad("http://docs.oracle.com/javase/8/javafx/api/overview-summary.html");
        } else {
            String baseClass = selectedNode.getNodeClassName();
            if (property != null) {
                baseClass = scenicView.findProperty(baseClass, property);
            }
            final String page = "http://docs.oracle.com/javase/8/javafx/api/" + baseClass.replace('.', '/') + ".html"
                    + (property != null ? ("#" + property + "Property") : "");
            webView.doLoad(page);
        }
    }
}
