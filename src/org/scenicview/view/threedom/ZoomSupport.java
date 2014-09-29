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

/**
 *
 * @author in-sideFX
 */
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
