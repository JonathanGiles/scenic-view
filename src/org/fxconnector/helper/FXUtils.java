package org.fxconnector.helper;

import java.util.Iterator;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Window;

public class FXUtils {
    /**
     * Retrieves the parent of the given node 
     * @param n the node for which the parent is to be found
     * @return the found parent or null
     */
    public static Parent parentOf(Node n) {
        if (n == null) {
            return null;
        }
        Parent p = n.getParent();
        if (p != null) {
            return parentOf(p);
        }
        if (n instanceof Parent) {
            return (Parent) n;
        }
        
        return null;
    }

    
    /**
     * Retrieves the root window containing the given node
     * @param n the node to look window for
     * @return the window the node belongs to, or null if it cannot be found
     */
    public static Window windowOf(Node n) {
        if (n == null) {
            return null;
        }

        Parent p = n.getParent();
        if (p != null) {
            return windowOf(p);
        }

        if (n instanceof Parent) {
            Parent container = (Parent) n;

            for (Iterator<Window> it = Window.impl_getWindows(); it.hasNext();) {
                Window w = it.next();
                if (w.getScene() != null) {
                    if (container == w.getScene().getRoot()) {
                        return w;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Retrieves the root window containing the given scene
     * @param s the scene to look window for
     * @return the window the scene belongs to, or null if it cannot be found
     */
    public static Window windowOf(Scene s) {
        if (s == null) {
            return null;
        }

        for (Iterator<Window> it = Window.impl_getWindows(); it.hasNext();) {
            Window w = it.next();
            if (s == w.getScene()) {
                return w;
            }
        }

        return null;
    }
}
