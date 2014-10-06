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
package org.scenicview.view.threedom;

import javafx.beans.property.Property;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;

public class ZoomSupport {

    public EventHandler<KeyEvent> keyboardEventHandler;
    public EventHandler<ScrollEvent> mouseEventHandler;
    private Number anchorX, anchorY;
    private Number anchor;
    private double dragAnchor;
    private MouseEvent lastMouseEvent;
    private final Node target;

    public ZoomSupport(Node target, final KeyCode modifier, final MouseButton mouseButton, final Orientation orientation, final Property<Number> property,final Property<Number> propertyX, final double factor) {
        this.target = target;
        mouseEventHandler = (ScrollEvent t) -> {
            if (t.getEventType() == ScrollEvent.SCROLL) {
                double deltaY = t.getDeltaY();
                Number value = property.getValue();
                property.setValue(value.doubleValue()-(deltaY*factor));
                // x
                deltaY = t.getDeltaX();
                value = propertyX.getValue();
                propertyX.setValue(value.doubleValue()-(deltaY*factor));
                t.consume();
            }
        };
        EventHandler<ZoomEvent> zoomEventHandler = (ZoomEvent z) -> {
            if (z.getEventType() == ZoomEvent.ZOOM) {
                double deltaY = z.getTotalZoomFactor();
                deltaY-=1;  // Make zoom out negative
                Number value = property.getValue();
                property.setValue(value.doubleValue()+deltaY*0.7);
                z.consume();
            }
        };
        
        target.addEventHandler(ScrollEvent.ANY, mouseEventHandler);
        target.addEventHandler(ZoomEvent.ANY, zoomEventHandler);
    }
}
