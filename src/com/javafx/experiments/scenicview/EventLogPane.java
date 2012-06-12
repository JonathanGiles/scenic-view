package com.javafx.experiments.scenicview;

import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.beans.value.*;
import javafx.collections.*;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class EventLogPane extends VBox {

    TableView<ScenicViewEvent> table = new TableView<ScenicViewEvent>();
    ChoiceBox<String> showStack = new ChoiceBox<String>();
    ChoiceBox<String> activateTrace = new ChoiceBox<String>();
    ObservableList<ScenicViewEvent> events = FXCollections.observableArrayList();
    SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");
    TextField idFilterField;

    @SuppressWarnings("unchecked")
    public EventLogPane() {
        table.setEditable(false);
        table.getStyleClass().add("trace-text-area");
        final TableColumn<ScenicViewEvent,String> sourceCol = new TableColumn<ScenicViewEvent,String>("source");
        sourceCol.setCellValueFactory(new PropertyValueFactory<ScenicViewEvent,String>("source"));
        sourceCol.prefWidthProperty().bind(widthProperty().divide(4));
        final TableColumn<ScenicViewEvent,String> eventTypeCol = new TableColumn<ScenicViewEvent,String>("eventType");
        eventTypeCol.setCellValueFactory(new PropertyValueFactory<ScenicViewEvent,String>("eventType"));
        eventTypeCol.prefWidthProperty().bind(widthProperty().divide(4));
        final TableColumn<ScenicViewEvent,String> eventValueCol = new TableColumn<ScenicViewEvent,String>("eventValue");
        eventValueCol.prefWidthProperty().bind(widthProperty().divide(4));
        eventValueCol.setCellValueFactory(new PropertyValueFactory<ScenicViewEvent,String>("eventValue"));
        final TableColumn<ScenicViewEvent,String> momentCol = new TableColumn<ScenicViewEvent,String>("moment");
        momentCol.setCellValueFactory(new PropertyValueFactory<ScenicViewEvent,String>("moment"));
        momentCol.prefWidthProperty().bind(widthProperty().divide(4));
          
        table.getColumns().addAll(sourceCol, eventTypeCol, eventValueCol, momentCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setItems(events);
        table.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ScenicViewEvent>() {

            @Override public void changed(final ObservableValue<? extends ScenicViewEvent> arg0, final ScenicViewEvent arg1, final ScenicViewEvent newValue) {
                final StringBuilder sb = new StringBuilder();
                for (int i = 0; i < newValue.stackTrace.length; i++) {
                    sb.append(newValue.stackTrace[i]).append('\n');
                }
                final int width = 600;
                final int height = 400;
                final Stage stage = new Stage();
                stage.getIcons().add(ScenicView.APP_ICON);
                stage.setResizable(false);
                stage.setTitle("Event StackTrace");
                stage.setWidth(width);
                stage.setHeight(height);
                final TextArea area = new TextArea(sb.toString());
                area.setFocusTraversable(false);
                area.setEditable(false);
                area.setMaxWidth(width-15);
                area.setMaxHeight(height-35);
                final StackPane pane = new StackPane();
                pane.setPrefHeight(height);
                pane.setPrefWidth(width);
                pane.setAlignment(Pos.CENTER);
                pane.getChildren().add(area);
                
                stage.setScene(new Scene(pane));
                stage.show();
            }
        });
        final Button clear = new Button("Clear");
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

//        final Label show = new Label("Show stackTraces:");
//        showStack.setItems(FXCollections.observableArrayList("Do not show stackTraces", "Show complete stackTrace"));
//        showStack.getSelectionModel().select(0);
//        showStack.setMaxWidth(Integer.MAX_VALUE);

        GridPane.setHgrow(idFilterField, Priority.ALWAYS);
        GridPane.setHgrow(b1, Priority.NEVER);
//        GridPane.setHgrow(show, Priority.ALWAYS);
        GridPane.setHgrow(showStack, Priority.ALWAYS);
        GridPane.setHgrow(clear, Priority.ALWAYS);
        
        activateTrace.setItems(FXCollections.observableArrayList("false", "true"));
        activateTrace.getSelectionModel().select(0);
        activateTrace.setMaxWidth(Integer.MAX_VALUE);

        filtersGridPane.add(new Label("Activate trace"), 1, 1);
        filtersGridPane.add(activateTrace, 2, 1, 2, 1);
        filtersGridPane.add(new Label("Text Filter:"), 1, 2);
        filtersGridPane.add(idFilterField, 2, 2);
        filtersGridPane.add(b1, 3, 2);
//        filtersGridPane.add(show, 1, 3);
//        filtersGridPane.add(showStack, 2, 3, 2, 1);
        filtersGridPane.add(clear, 1, 3, 3, 1);
        filtersGridPane.setPrefHeight(60);

        getChildren().addAll(filtersGridPane, table);
        VBox.setVgrow(table, Priority.ALWAYS);
    }

    public void trace(final String source, final String eventType, final String eventValue) {
        if (isActive()) {
            if (idFilterField.getText().equals("") || (eventType.indexOf(idFilterField.getText()) != -1)  || (eventValue.indexOf(idFilterField.getText()) != -1) || source.indexOf(idFilterField.getText()) != -1) {
            	events.add(new ScenicViewEvent(source, eventType, eventValue));
            }
        }
    }
    
    public boolean isActive() {
        return activateTrace.getSelectionModel().getSelectedIndex()!=0;
    }
    
    public class ScenicViewEvent {
    	
    	public String source;
		public String eventType;
    	public String eventValue;
    	public String moment;
    	String relative;
    	StackTraceElement [] stackTrace;
    	
    	
    	public ScenicViewEvent(final String source, final String eventType, final String eventValue) {
			this.source=source;
			this.eventType=eventType;
			this.eventValue=eventValue;
			this.moment=format.format(new Date());
			this.stackTrace = Thread.currentThread().getStackTrace();
		}

        public String getSource() {
            return source;
        }

        public void setSource(final String source) {
            this.source = source;
        }

        public String getEventType() {
            return eventType;
        }

        public void setEventType(final String eventType) {
            this.eventType = eventType;
        }

        public String getEventValue() {
            return eventValue;
        }

        public void setEventValue(final String eventValue) {
            this.eventValue = eventValue;
        }

        public String getMoment() {
            return moment;
        }

        public void setMoment(final String moment) {
            this.moment = moment;
        }

        public String getRelative() {
            return relative;
        }

        public void setRelative(final String relative) {
            this.relative = relative;
        }

    }

}
