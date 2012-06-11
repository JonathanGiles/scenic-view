package com.javafx.experiments.scenicview;

import java.util.Date;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class StructureTracePane extends VBox {

    TableView<ScenicViewEvent> table = new TableView<ScenicViewEvent>();
    ChoiceBox<String> showStack = new ChoiceBox<String>();
    ObservableList<ScenicViewEvent> events = FXCollections.observableArrayList();
    boolean activate = true;
    TextField idFilterField;

    public StructureTracePane() {
        table.setEditable(false);
        table.getStyleClass().add("trace-text-area");
        TableColumn firstNameCol = new TableColumn("source");
        TableColumn lastNameCol = new TableColumn("eventType");
        TableColumn emailCol = new TableColumn("eventValue");
        TableColumn momentCol = new TableColumn("moment");
          
        table.getColumns().addAll(firstNameCol, lastNameCol, emailCol, momentCol);
        table.setItems(events);
        final Button clear = new Button("Clear TextArea");
        clear.setOnAction(new EventHandler<ActionEvent>() {

            @Override public void handle(final ActionEvent arg0) {
            	events.clear();
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

        getChildren().addAll(filtersGridPane, table);
        VBox.setVgrow(table, Priority.ALWAYS);
    }

    public void activate(final boolean activate) {
        this.activate = activate;
        if (activate) {
            setPrefHeight(getParent().getBoundsInLocal().getHeight());
            events.clear();
        }
    }

    public void trace(final String source, String eventType, String eventValue) {
        if (activate) {
            if (idFilterField.getText().equals("") || (eventType.indexOf(idFilterField.getText()) != -1)  || (eventValue.indexOf(idFilterField.getText()) != -1) || source.indexOf(idFilterField.getText()) != -1) {
            	events.add(new ScenicViewEvent(source, eventType, eventValue));
            	table.setItems(events);
            }
        }
    }
    
    class ScenicViewEvent {
    	
    	public String source;
    	public String getSource() {
			return source;
		}

		public void setSource(String source) {
			this.source = source;
		}

		public String getEventType() {
			return eventType;
		}

		public void setEventType(String eventType) {
			this.eventType = eventType;
		}

		public String getEventValue() {
			return eventValue;
		}

		public void setEventValue(String eventValue) {
			this.eventValue = eventValue;
		}

		public String getMoment() {
			return moment;
		}

		public void setMoment(String moment) {
			this.moment = moment;
		}

		public String getRelative() {
			return relative;
		}

		public void setRelative(String relative) {
			this.relative = relative;
		}

		public String eventType;
    	public String eventValue;
    	public String moment;
    	String relative;
    	
    	public ScenicViewEvent(String source, String eventType, String eventValue) {
			this.source = source;
			this.eventType = eventType;
			this.eventValue = eventValue;
			this.moment = new Date().toString();
		}
    }

}
