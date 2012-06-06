/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import javafx.application.Application;
import javafx.beans.property.*;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.stage.*;

import com.javafx.experiments.scenicview.ScenicView;

/**
 * 
 * @author aim
 */
public class ScenicViewExample extends Application {

    protected Group createGroup() {
        final Group g = new Group();
        final Circle c1 = new Circle(35, 25, 20, Color.RED);
        c1.setOpacity(.70);
        final Circle c2 = new Circle(0, 25, 32, Color.BLUE);
        c2.setOpacity(.60);
        final Circle c3 = new Circle(55, 25, 12, Color.GREEN);
        c3.setOpacity(.50);
        g.getChildren().addAll(c1, c2, c3);
        return g;
    }

    @Override public void start(final Stage stage) {
        stage.setTitle("ScenicView Test App");
        final TilePane tilepane = new TilePane(12, 12);
        tilepane.setPadding(new Insets(18, 18, 18, 18));
        tilepane.setPrefColumns(4);

        final DropShadow shadow = new DropShadow();
        shadow.setRadius(30);

        final Rectangle rect1 = new Rectangle(75, 75, Color.BLUEVIOLET);
        rect1.setId("rect1");

        rect1.setOnMousePressed(new EventHandler<MouseEvent>() {

            @Override public void handle(final MouseEvent arg0) {
                final ContextMenu cm = new ContextMenu();
                final MenuItem chartItem1 = new MenuItem("Chart Settings");

                cm.getItems().add(chartItem1);
                cm.show(stage);

            }
        });
        final Rectangle rect2 = new Rectangle(75, 75, Color.CRIMSON);
        rect2.setId("rect2");
        rect2.setEffect(shadow);
        rect2.setOnMousePressed(new EventHandler<MouseEvent>() {

            @Override public void handle(final MouseEvent arg0) {
                final Popup popup = new Popup();

                final HBox box = new HBox();
                box.getChildren().add(new Label("In popup..."));
                box.setPrefSize(100, 100);
                box.setAlignment(Pos.BOTTOM_RIGHT);
                box.setStyle("-fx-background-color: gray;");
                box.setOnMousePressed(new EventHandler<MouseEvent>() {

                    @Override public void handle(final MouseEvent arg0) {
                        popup.hide();
                    }
                });
                popup.getContent().add(box);
                popup.show(stage);

            }
        });
        final Rectangle rect3 = new Rectangle(75, 75, Color.AQUAMARINE);
        rect3.setId("rect3");
        rect3.setRotate(45);

        final Rectangle rect4 = new Rectangle(75, 75);
        rect4.setId("rect4");
        rect4.getStyleClass().add("color");
        rect4.setRotate(45);
        rect4.setOnMousePressed(new EventHandler<MouseEvent>() {

            @Override public void handle(final MouseEvent arg0) {
                rect4.setVisible(false);
            }
        });
        rect3.setOnMousePressed(new EventHandler<MouseEvent>() {

            @Override public void handle(final MouseEvent arg0) {
                rect4.setVisible(true);
            }
        });
        final Group g1 = createGroup();
        final Group g2 = createGroup();
        g2.setEffect(shadow);
        final Group g3 = createGroup();
        g3.setRotate(45);
        final Group g4 = createGroup();
        g4.setRotate(45);

        final BooleanProperty visible = new SimpleBooleanProperty(true);
        final DoubleProperty pos = new SimpleDoubleProperty(0);
        final Font f = new Font(18);
        final Button b1 = new Button("First");
        b1.visibleProperty().bind(visible);
        b1.translateXProperty().bind(pos);
        b1.translateYProperty().bind(pos);
        b1.setFont(f);
        final Button b2 = new Button("Second");
        b2.getStyleClass().add("second-button");
        b2.translateXProperty().bind(pos);
        b2.setFont(f);
        b2.setMouseTransparent(true);
        b2.setEffect(shadow);
        final Button b3 = new Button("Third");
        b3.setFont(f);
        b3.setRotate(45);
        final Button b4 = new Button("Fourth");
        b4.setRotate(45);
        b4.setFont(f);

        final Group invisible = createGroup();
        invisible.setId("InvisibleGroup");
        invisible.setVisible(false);

        tilepane.getChildren().addAll(rect1, rect2, rect3, new Group(rect4), b1, b2, b3, new Group(b4), g1, g2, g3, new Group(g4), invisible);

        final Scene scene = new Scene(tilepane);
        scene.getStylesheets().add(getClass().getResource("scenicviewtest.css").toString());
        stage.setScene(scene);
        stage.show();

        ScenicView.show(scene);
    }

    public static void main(final String[] args) {
        Application.launch(args);
    }

}
