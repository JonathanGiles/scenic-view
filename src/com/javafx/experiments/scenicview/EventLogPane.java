package com.javafx.experiments.scenicview;

import java.text.SimpleDateFormat;
import java.util.*;

import javafx.beans.value.*;
import javafx.collections.*;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class EventLogPane extends VBox {

    TableView<ScenicViewEvent> table = new TableView<ScenicViewEvent>();
    ChoiceBox<String> showStack = new ChoiceBox<String>();
    CheckBox activateTrace = new CheckBox();
    ObservableList<ScenicViewEvent> events = FXCollections.observableArrayList();
    ObservableList<ScenicViewEvent> filteredEvents = FXCollections.observableArrayList();
    SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");
    TextField idFilterField;
    Label selectedNodeLabel = new Label("Filtering from current selection: None");
    Node selectedNode;

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
        table.setItems(filteredEvents);
        table.setFocusTraversable(false);
        table.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ScenicViewEvent>() {

            @Override public void changed(final ObservableValue<? extends ScenicViewEvent> arg0, final ScenicViewEvent arg1, final ScenicViewEvent newValue) {
                if(newValue != null) {
                    final StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < newValue.stackTrace.length; i++) {
                        sb.append(newValue.stackTrace[i]).append('\n');
                    }
                    final int width = 600;
                    final int height = 400;
                    final Stage stage = new Stage();
                    stage.getIcons().add(ScenicView.APP_ICON);
                    stage.setTitle(newValue.toString());
                    stage.setWidth(width);
                    stage.setHeight(height);
                    final TextArea area = new TextArea(sb.toString());
                    area.setFocusTraversable(false);
                    area.setEditable(false);
                    StackPane.setMargin(area, new Insets(5, 5, 5, 5));
                    final StackPane pane = new StackPane();
                    pane.setAlignment(Pos.CENTER);
                    pane.getChildren().add(area);
                    
                    final Scene scene = new Scene(pane);
                    scene.getStylesheets().addAll(ScenicView.STYLESHEETS);
                    stage.setScene(scene);
                    stage.show();
                }
            }
        });
        final Button clear = new Button("Clear");
        clear.setOnAction(new EventHandler<ActionEvent>() {

            @Override public void handle(final ActionEvent arg0) {
            	events.clear();
            	filteredEvents.clear();
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
        idFilterField.setOnKeyReleased(new EventHandler<KeyEvent>() {

            @Override public void handle(final KeyEvent arg0) {
                final List<ScenicViewEvent> events = new ArrayList<EventLogPane.ScenicViewEvent>();
                for (int i = 0; i < EventLogPane.this.events.size(); i++) {
                    if(validForFilter(EventLogPane.this.events.get(i))) {
                        events.add(EventLogPane.this.events.get(i));
                    }
                }
                EventLogPane.this.filteredEvents.setAll(events);
            }
        });

        final Button b1 = new Button();
        b1.setGraphic(new ImageView(DisplayUtils.CLEAR_IMAGE));
        b1.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent arg0) {
                idFilterField.setText("");

            }
        });
        final ListView<String> events = new ListView<String>();
        events.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        final ObservableList<String> items = FXCollections.observableArrayList();
        items.add(MouseEvent.MOUSE_ENTERED.toString());
        items.add(MouseEvent.MOUSE_ENTERED_TARGET.toString());
        items.add(MouseEvent.MOUSE_EXITED.toString());
        items.add(MouseEvent.MOUSE_EXITED_TARGET.toString());
        items.add(MouseEvent.MOUSE_MOVED.toString());
        items.add(MouseEvent.MOUSE_PRESSED.toString());
        items.add(MouseEvent.MOUSE_RELEASED.toString());
        events.setItems(items);
        events.setPrefWidth(160);
        
        GridPane.setHgrow(idFilterField, Priority.ALWAYS);
        GridPane.setHgrow(b1, Priority.NEVER);
        GridPane.setHgrow(showStack, Priority.ALWAYS);
        GridPane.setHgrow(clear, Priority.ALWAYS);

        filtersGridPane.add(new Label("Enable:"), 1, 1);
        filtersGridPane.add(activateTrace, 2, 1, 1, 1);
        filtersGridPane.add(new Label("Valid Events"), 4, 1, 1, 1);
        filtersGridPane.add(events, 4, 2, 1, 3);
        filtersGridPane.add(new Label("Text Filter:"), 1, 2);
        filtersGridPane.add(idFilterField, 2, 2);
        filtersGridPane.add(b1, 3, 2);
        filtersGridPane.add(clear, 1, 3, 3, 1);
        filtersGridPane.add(selectedNodeLabel, 1, 4, 3, 1);
        filtersGridPane.setPrefHeight(60);
        VBox.setMargin(table, new Insets(0, 5, 5, 5));

        getChildren().addAll(filtersGridPane, table);
        VBox.setVgrow(table, Priority.ALWAYS);
    }
    
    public void setSelectedNode(final Node selectedNode) {
        this.selectedNode = selectedNode;
        if(selectedNode != null) {
            selectedNodeLabel.setText("Filtering from current selection: "+DisplayUtils.nodeDetail(selectedNode, true));
        }
        else {
            selectedNodeLabel.setText("Filtering from current selection: None");
        }
    }

    public void trace(final Node source, final String eventType, final String eventValue) {
        if (isActive()) {
            if(checkValid(source)) {
                final String sourceId = DisplayUtils.nodeDetail(source, true);
                final ScenicViewEvent event = new ScenicViewEvent(sourceId, eventType, eventValue);
                addToFiltered(event);
                events.add(event);
            }
        }
    }
    
    private void addToFiltered(final ScenicViewEvent event) {
        if (validForFilter(event)) {
            filteredEvents.add(event);
        }
    }
    
    private boolean validForFilter(final ScenicViewEvent event) {
        final String lower = idFilterField.getText().toLowerCase();
        return (idFilterField.getText().equals("") || (event.eventType.toLowerCase().indexOf(lower) != -1)  || (event.eventValue.toLowerCase().indexOf(lower) != -1) || event.source.toLowerCase().indexOf(lower) != -1);
    }
    
    private boolean checkValid(final Node node) {
        if(node == null) return false;
        return (selectedNode == null) || selectedNode == node || checkValid(node.getParent());
    }
    
    public boolean isActive() {
        return activateTrace.isSelected();
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

        @Override public String toString() {
            return "Event [s=" + source + ", et=" + eventType + ((eventValue!=null && !eventValue.equals(""))?(", ev=" + eventValue):"") + ", m=" + moment + "]";
        }

    }

}
