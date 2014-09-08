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
package org.scenicview.model.update;

import java.util.List;

import org.fxconnector.AppController;

/**
 * This strategy will be used when we are showing only one stage
 */
public class LocalUpdateStrategy implements UpdateStrategy {

    List<AppController> controller;

    public LocalUpdateStrategy(final List<AppController> controller) {
        this.controller = controller;
    }

    @Override public void start(final AppsRepository repository) {
        for (int i = 0; i < controller.size(); i++) {
            repository.appAdded(controller.get(i));
        }
    }

    @Override public void finish() {

    }
}
