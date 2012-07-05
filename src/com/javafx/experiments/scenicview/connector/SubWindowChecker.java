package com.javafx.experiments.scenicview.connector;

import java.util.*;

import javafx.application.Platform;
import javafx.stage.*;

import com.javafx.experiments.scenicview.connector.helper.WindowChecker;

@SuppressWarnings("rawtypes")
class SubWindowChecker extends WindowChecker {

    StageControllerImpl model;

    public SubWindowChecker(final StageControllerImpl model) {
        super(new WindowFilter() {

            @Override public boolean accept(final Window window) {
                return window instanceof PopupWindow;
            }
        }, model.getID().toString());
        this.model = model;
    }

    Map<PopupWindow, Map> previousTree = new HashMap<PopupWindow, Map>();
    List<PopupWindow> windows = new ArrayList<PopupWindow>();
    final Map<PopupWindow, Map> tree = new HashMap<PopupWindow, Map>();

    @Override protected void onWindowsFound(final List<Window> tempPopups) {
        tree.clear();
        windows.clear();

        for (final Window popupWindow : tempPopups) {
            final Map<PopupWindow, Map> pos = valid((PopupWindow) popupWindow, tree);
            if (pos != null) {
                pos.put((PopupWindow) popupWindow, new HashMap<PopupWindow, Map>());
                windows.add((PopupWindow) popupWindow);
            }
        }
        if (!tree.equals(previousTree)) {
            previousTree.clear();
            previousTree.putAll(tree);
            final List<PopupWindow> actualWindows = new ArrayList<PopupWindow>(windows);
            Platform.runLater(new Runnable() {

                @Override public void run() {
                    // No need for synchronization here
                    model.popupWindows.clear();
                    model.popupWindows.addAll(actualWindows);
                    model.update();

                }
            });

        }

    }

    @SuppressWarnings("unchecked") Map<PopupWindow, Map> valid(final PopupWindow window, final Map<PopupWindow, Map> tree) {
        if (window.getOwnerWindow() == model.targetWindow)
            return tree;
        for (final Iterator<PopupWindow> iterator = tree.keySet().iterator(); iterator.hasNext();) {
            final PopupWindow type = iterator.next();
            if (type == window.getOwnerWindow()) {
                return tree.get(type);
            } else {
                final Map<PopupWindow, Map> lower = valid(window, tree.get(type));
                if (lower != null)
                    return lower;
            }
        }
        return null;
    }

}