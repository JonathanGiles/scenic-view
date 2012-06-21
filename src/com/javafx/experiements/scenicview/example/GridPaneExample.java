package com.javafx.experiements.scenicview.example;

import javafx.application.Application;
import javafx.event.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class GridPaneExample extends Application {
    
    @Override public void start(final Stage stage) throws Exception {
        final TextField idFilterField = new TextField();
        final Button b1 = new Button("b");
        final Label selectedNodeLabel = new Label("Filtering is disabled");
        final Button clear = new Button("clear");
        clear.setOnAction(new EventHandler<ActionEvent>() {
            
            @Override public void handle(final ActionEvent arg0) {
                if(selectedNodeLabel.getText().equals("Filtering is disabled")) {
                    selectedNodeLabel.setText("Filtering from current selection: Rectangle 'Rect3'");
                }
                else {
                    selectedNodeLabel.setText("Filtering is disabled");
                }
            }
        });
        clear.setMaxWidth(Integer.MAX_VALUE);
        final GridPane filtersGridPane = new GridPane();
        filtersGridPane.setVgap(5);
        GridPane.setHgrow(idFilterField, Priority.ALWAYS);
        GridPane.setHgrow(b1, Priority.NEVER);
        GridPane.setHgrow(clear, Priority.ALWAYS);
        GridPane.setHgrow(selectedNodeLabel, Priority.NEVER);

        /**
         * This is an ugly fix for what I think is a bug of the gridPane
         */
//        idFilterField.prefWidthProperty().bind(filtersGridPane.widthProperty().subtract(130));
        filtersGridPane.add(new Label("Text Filter:"), 1, 2);
        filtersGridPane.add(idFilterField, 2, 2);
        filtersGridPane.add(b1, 3, 2);
        filtersGridPane.add(clear, 1, 3, 3, 1);
        filtersGridPane.add(selectedNodeLabel, 1, 4, 3, 1);
        filtersGridPane.setPrefHeight(60);
        filtersGridPane.setPrefWidth(300);
        stage.setScene(new Scene(filtersGridPane));

        stage.show();
    }

    public static void main(final String[] args) {
        Application.launch(args);
    }

}
