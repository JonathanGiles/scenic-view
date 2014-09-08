/*
 * Scenic View, 
 * Copyright (C) 2014, Jonathan Giles, Ander Ruiz, Amy Fowler 
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
package org.fxconnector.helper;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.SubScene;

public class ChildrenGetter {

    private ChildrenGetter() { }
    
    public static ObservableList<Node> getChildren(Node node) {
        if (node == null) return FXCollections.emptyObservableList();
        
        if (node instanceof Parent) {
            return ((Parent)node).getChildrenUnmodifiable();
        } else if (node instanceof SubScene) {
            return ((SubScene)node).getRoot().getChildrenUnmodifiable();
        }
        
        return FXCollections.emptyObservableList();
    }
}
