package com.javafx.experiements.scenicview.example;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.*;

import javafx.animation.*;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.collections.*;
import javafx.event.*;
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
import javafx.util.Duration;

import com.javafx.experiments.scenicview.ScenicView;
import com.javafx.experiments.scenicview.connector.AppController;
import com.javafx.experiments.scenicview.update.LocalVMUpdateStrategy;

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
        TilePane.setAlignment(rect3, Pos.BASELINE_LEFT);
        TilePane.setMargin(rect3, new Insets(3));

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

        final ObservableList<String> citems = FXCollections.observableArrayList();
        for (int i = 0; i < 10; i++) {
            citems.add("Combo content:" + i);
        }

        final ComboBox<String> comboTest = new ComboBox<String>();
        comboTest.setItems(citems);
        comboTest.setPrefWidth(250);
        comboTest.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

            @Override public void changed(final ObservableValue<? extends String> arg0, final String arg1, final String newValue) {
                System.out.println("Changed");
            }
        });

        final BooleanProperty visible = new SimpleBooleanProperty(true);
        final DoubleProperty pos = new SimpleDoubleProperty(0);
        final Font f = new Font(18);
        final Button b1 = new Button("First");
        b1.visibleProperty().bind(visible);
        b1.translateXProperty().bind(pos);
        b1.translateYProperty().bind(pos);
        b1.setFont(f);

        final Button b2 = new Button("Second");
        b2.setId("Second");
        b2.getStyleClass().add("second-button");
        b2.translateXProperty().bind(pos);
        b2.setFont(f);
        b2.setMouseTransparent(false);
        final ObservableList<String> items = FXCollections.observableArrayList();
        b2.setEffect(shadow);
        for (int i = 0; i < 1000; i++) {
            items.add("List View content:" + i);
        }

        TimelineBuilder.create().keyFrames(new KeyFrame(Duration.millis(5000), new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent arg0) {
                b2.setVisible(!b2.isVisible());
            }
        })).cycleCount(Animation.INDEFINITE).build().play();

        final Group gb2 = new Group();
        gb2.getChildren().add(b2);

        final ListView<String> listViewTest = new ListView<String>(items);
        listViewTest.setPrefHeight(40);
        final Button b4 = new Button("Fourth");
        b4.setRotate(45);
        b4.setFont(f);
        b4.setOnAction(new EventHandler<ActionEvent>() {

            @Override public void handle(final ActionEvent arg0) {
                tilepane.getChildren().remove(b4);
            }
        });

        final GridPane pane = new GridPane();
        pane.setHgap(4);
        final RowConstraints row1 = new RowConstraints();
        row1.setPercentHeight(25);
        final RowConstraints row2 = new RowConstraints();
        row2.setPercentHeight(50);
        final RowConstraints row3 = new RowConstraints();
        row3.setPercentHeight(25);
        pane.getRowConstraints().addAll(row1, row2, row3);
        final Group invisible = createGroup();
        invisible.setId("InvisibleGroup");
        invisible.setVisible(false);

        tilepane.getChildren().addAll(rect1, rect2, rect3, new Group(rect4), b1, gb2, listViewTest, new Group(b4), g1, g2, g3, comboTest, invisible, pane);

        final Scene scene = new Scene(tilepane);
        scene.getStylesheets().add(ScenicView.STYLESHEETS);
        stage.setScene(scene);
        stage.show();

        // ScenicView.show(scene);

        final Stage stage2 = new Stage();
        stage2.setTitle("Second example");
        final Scene scene2 = new Scene(new Group());
        stage2.setScene(scene2);
        stage2.show();

        final Stage stages = new Stage();
        // workaround for RT-10714
        stages.setWidth(640);
        stages.setHeight(800);
        final List<AppController> controllers = new ArrayList<AppController>();

        // final AppController aController = new AppControllerImpl();
        // aController.getStages().add(new StageControllerImpl(scene.getRoot(),
        // aController));
        // aController.getStages().add(new StageControllerImpl(scene2.getRoot(),
        // aController));
        // controllers.add(aController);
        ScenicView.show(new ScenicView(new LocalVMUpdateStrategy(), stages), stages);
    }

    public static void main(final String[] args) {
        Application.launch(args);
    }

}
