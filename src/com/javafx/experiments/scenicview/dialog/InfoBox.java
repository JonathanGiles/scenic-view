package com.javafx.experiments.scenicview.dialog;

import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import com.javafx.experiments.scenicview.ScenicView;

public class InfoBox {

    public InfoBox(final String title, final String labelText, final String textAreaText) {
        final int width = 700;
        final int height = 600;
        final VBox pane = new VBox();
        final Scene scene = SceneBuilder.create().width(600).height(400).root(pane).stylesheets(ScenicView.STYLESHEETS).build();

        final Stage stage = StageBuilder.create().style(StageStyle.UTILITY).title(title).build();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.getIcons().add(ScenicView.APP_ICON);

        final Label label = new Label(labelText);
        stage.setWidth(width);
        stage.setHeight(height);
        final TextArea area = new TextArea(textAreaText);
        area.setFocusTraversable(false);
        area.setEditable(false);
        final Button close = new Button("Close");
        VBox.setMargin(label, new Insets(5, 5, 0, 5));
        VBox.setMargin(area, new Insets(5, 5, 0, 5));
        VBox.setMargin(close, new Insets(5, 5, 5, 5));

        VBox.setVgrow(area, Priority.ALWAYS);
        pane.setAlignment(Pos.CENTER);

        close.setDefaultButton(true);
        close.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent arg0) {
                stage.close();
            }
        });
        pane.getChildren().addAll(label, area, close);

        stage.show();
    }

    public static InfoBox make(final String title, final String labelText, final String textAreaText) {
        return new InfoBox(title, labelText, textAreaText);
    }

}
