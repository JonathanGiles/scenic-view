package com.javafx.experiments.scenicview;

import java.io.*;

import javafx.beans.value.*;
import javafx.collections.FXCollections;
import javafx.event.*;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

public class StructureTracePane extends VBox {

    TextArea text = new TextArea();
    ChoiceBox<String> showStack = new ChoiceBox<String>();
    boolean activate;
    TextField idFilterField;

    public StructureTracePane() {
        text.setEditable(false);
        text.getStyleClass().add("trace-text-area");
        final Button clear = new Button("Clear TextArea");
        clear.setOnAction(new EventHandler<ActionEvent>() {

            @Override public void handle(final ActionEvent arg0) {
                text.clear();
            }
        });
        clear.setMaxWidth(Integer.MAX_VALUE);
        getStyleClass().add("structure-trace-pane");

        final GridPane filtersGridPane = new GridPane();
        filtersGridPane.setVgap(5);
        filtersGridPane.setHgap(5);
        filtersGridPane.setSnapToPixel(true);
        filtersGridPane.setPadding(new Insets(0, 5, 5, 0));
        filtersGridPane.setId("structure-trace-grid-pane");

        idFilterField = new TextField();
        idFilterField.setPromptText("Insert text to filter");
        idFilterField.focusedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean newValue) {
                if (newValue)
                    ScenicView.setStatusText("Type any text for filtering");
                else
                    ScenicView.clearStatusText();
            }
        });

        final Button b1 = new Button();
        b1.setGraphic(new ImageView(DisplayUtils.CLEAR_IMAGE));
        b1.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent arg0) {
                idFilterField.setText("");

            }
        });

        final Label show = new Label("Show stackTraces:");
        showStack.setItems(FXCollections.observableArrayList("Do not show stackTraces", "Show complete stackTrace"));
        showStack.getSelectionModel().select(0);
        showStack.setMaxWidth(Integer.MAX_VALUE);

        GridPane.setHgrow(idFilterField, Priority.ALWAYS);
        GridPane.setHgrow(b1, Priority.NEVER);
        GridPane.setHgrow(show, Priority.ALWAYS);
        GridPane.setHgrow(showStack, Priority.ALWAYS);
        GridPane.setHgrow(clear, Priority.ALWAYS);

        filtersGridPane.add(new Label("Text Filter:"), 1, 1);
        filtersGridPane.add(idFilterField, 2, 1);
        filtersGridPane.add(b1, 3, 1);
        filtersGridPane.add(show, 1, 2);
        filtersGridPane.add(showStack, 2, 2, 2, 1);
        filtersGridPane.add(clear, 1, 3, 3, 1);
        filtersGridPane.setPrefHeight(60);

        getChildren().addAll(filtersGridPane, text);
        VBox.setVgrow(text, Priority.ALWAYS);
    }

    public void activate(final boolean activate) {
        this.activate = activate;
        if (activate) {
            setPrefHeight(getParent().getBoundsInLocal().getHeight());
            text.clear();
        }
    }

    private void append(final String data) {
        text.setText(text.getText() + "\n" + data);
    }

    public void trace(final String action, final Node node) {
        if (activate) {
            if (idFilterField.getText().equals("") || (action.indexOf(idFilterField.getText()) != -1) || node.toString().indexOf(idFilterField.getText()) != -1) {
                append(action + " " + node);
                if (showStack.getSelectionModel().getSelectedIndex() > 0) {
                    final ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    final PrintStream out = new PrintStream(bout);
                    new Exception(action + "Node:" + node).printStackTrace(out);
                    append(new String(bout.toByteArray()));
                }
            }
        }
    }

}
