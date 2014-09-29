/*
 * Scenic View, 
 * Copyright (C) 2014 Jonathan Giles, Ander Ruiz, Amy Fowler, Arnaud Nouard
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
package org.scenicview.view.tabs;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.Parent;
import javafx.scene.control.Menu;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;

import org.fxconnector.node.SVNode;
import org.scenicview.view.threedom.ThreeDOM;
import org.scenicview.view.threedom.Tile3D;
import org.scenicview.view.ContextMenuContainer;
import org.scenicview.view.DisplayUtils;
import org.scenicview.view.ScenicViewGui;
import org.scenicview.view.threedom.IThreeDOM;

public class ThreeDOMTab extends Tab implements ContextMenuContainer, IThreeDOM {

    private final ScenicViewGui scenicView;

    public static final String TAB_NAME = "ThreeDOM";
//    TreeItem<SVNode> root2D;
    SVNode root2D;
    ThreeDOM threeDOM;
    SVNode selectedNode;

    public ThreeDOMTab(final ScenicViewGui view) {
        super(TAB_NAME);
        this.scenicView = view;

        setGraphic(new ImageView(DisplayUtils.getUIImage("cinema.png")));
        setClosable(false);
        selectedProperty().addListener((final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean newValue) -> {
            if (newValue) {
                init();
            }
        });
    }

    @Override
    public Menu getMenu() {
        return null;
    }

    void init() {
        if (threeDOM == null) {
            threeDOM = ThreeDOM.getInstance();
            threeDOM.setHolder(this);

            Parent threeDOMPanel = null;
            try {
                if (root2D != null) {
                    threeDOMPanel = threeDOM.createContent(root2D.getChildren().get(0), true);
                }
                super.setContent(threeDOMPanel);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            // First time: do not miss current selection
            if (selectedNode != null) {
                Platform.runLater(() -> {
                    setSelectedNode(selectedNode);
                });

            }

        }
    }

    /**
     * A node has been selected in the SV treeview
     *
     * @param selected
     */
    public void setSelectedNode(final SVNode selected) {
        selectedNode = selected;
        if (threeDOM != null) {
            if (selectedNode == null) {
                threeDOM.clearSelection();
            }
            Tile3D found = threeDOM.find(selectedNode);
            if (found != null) {
                threeDOM.setSelectedTile(found);
            }
        }
    }

    /**
     * Recreate the 3D model from current 2D state
     */
    public void reload() {
        if (threeDOM != null) {
            threeDOM.reload();
        }
    }

    /**
     * Root changed
     */
    public void placeNewRoot(SVNode newRoot) {
        root2D = newRoot;
        reload();
    }

    public void removeNode(SVNode node) {
        if (threeDOM != null) {
            threeDOM.removeNode(node);
        }
    }

    /**
     * User clicked on a 3D box
     * @param node
     */
    @Override
    public void clickOnTile(SVNode node) {
        scenicView.getTreeView().nodeSelected(node);        // Simulate classic click on tree
    }
}
