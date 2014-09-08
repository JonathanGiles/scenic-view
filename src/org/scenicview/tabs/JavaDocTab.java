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
package org.scenicview.tabs;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Menu;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;

import org.fxconnector.node.SVNode;
import org.scenicview.ContextMenuContainer;
import org.scenicview.DisplayUtils;
import org.scenicview.ScenicViewGui;
import org.scenicview.control.ProgressWebView;

public class JavaDocTab extends Tab implements ContextMenuContainer {
    
    private final ScenicViewGui scenicView;
    
    private final ProgressWebView webView;
    
    public static final String TAB_NAME = "JavaDoc";

    public JavaDocTab(final ScenicViewGui view) {
        super(TAB_NAME);
        
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
