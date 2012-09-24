package com.javafx.experiments.scenicview.control;

import java.lang.reflect.Field;

import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.event.*;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import com.sun.javafx.scene.control.skin.CustomColorDialog;

public class ColorMenuItem extends CustomMenuItem {

    private final Rectangle rect;
    ObjectProperty<Color> color = new SimpleObjectProperty<Color>();

    public ColorMenuItem() {
        final HBox box2 = new HBox();
        box2.setSpacing(10);
        rect = new Rectangle(16, 16);
        setColor(Color.BLACK);
        final Label label = new Label("Color");
        box2.getChildren().addAll(rect, label);
        setContent(box2);
        setOnAction(new EventHandler<ActionEvent>() {

            @Override public void handle(final ActionEvent arg0) {
                final CustomColorDialog dialog = new CustomColorDialog(null);

                dialog.setCurrentColor(color.get());
                try {
                    final Field field = CustomColorDialog.class.getDeclaredField("customColorProperty");
                    field.setAccessible(true);
                    final ObjectProperty<Color> color = (ObjectProperty<Color>) field.get(dialog);
                    color.addListener(new ChangeListener<Color>() {

                        @Override public void changed(final ObservableValue<? extends Color> arg0, final Color arg1, final Color newValue) {
                            setColor(newValue);
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                dialog.show(400, 300);
            }
        });
    }

    public void setColor(final Color color) {
        rect.setFill(color);
        this.color.set(color);
    }

    public Color getColor() {
        return this.color.get();
    }

    public ObjectProperty<Color> colorProperty() {
        return this.color;
    }

}
