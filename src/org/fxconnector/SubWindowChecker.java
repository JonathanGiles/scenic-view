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
 package org.fxconnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.stage.PopupWindow;
import javafx.stage.Window;

import org.fxconnector.helper.WindowChecker;

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