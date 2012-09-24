package com.javafx.experiments.scenicview.dialog;

import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import com.javafx.experiments.scenicview.ScenicView;

public class InfoBox {

    public InfoBox(final String title, final String labelText, final String textAreaText, final int width, final int height) {
        final VBox pane = new VBox();
        final Scene scene = SceneBuilder.create().width(width).height(height).root(pane).stylesheets(ScenicView.STYLESHEETS).build();

        final Stage stage = StageBuilder.create().style(StageStyle.UTILITY).title(title).build();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.getIcons().add(ScenicView.APP_ICON);

        final Label label = new Label(labelText);
        stage.setWidth(width);
        stage.setHeight(height);
        TextArea area = null;
        if (textAreaText != null) {
            area = new TextArea(textAreaText);
            area.setFocusTraversable(false);
            area.setEditable(false);
            VBox.setMargin(area, new Insets(5, 5, 0, 5));
            VBox.setVgrow(area, Priority.ALWAYS);
        }
        final Button close = new Button("Close");
        VBox.setMargin(label, new Insets(5, 5, 0, 5));

        VBox.setMargin(close, new Insets(5, 5, 5, 5));

        pane.setAlignment(Pos.CENTER);

        close.setDefaultButton(true);
        close.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent arg0) {
                stage.close();
            }
        });
        if (area != null) {
            pane.getChildren().addAll(label, area, close);
        } else {
            pane.getChildren().addAll(label, close);
        }

        stage.show();
    }

    public static InfoBox make(final String title, final String labelText, final String textAreaText) {
        return make(title, labelText, textAreaText, 700, 600);
    }

    public static InfoBox make(final String title, final String labelText, final String textAreaText, final int width, final int height) {
        return new InfoBox(title, labelText, textAreaText, width, height);
    }

}
