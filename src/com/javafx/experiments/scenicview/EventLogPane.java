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
import javafx.stage.*;

import com.javafx.experiments.scenicview.connector.*;
import com.javafx.experiments.scenicview.connector.event.EvLogEvent;
import com.javafx.experiments.scenicview.connector.node.SVNode;

public class EventLogPane extends VBox {

    public static final String PROPERTY_CHANGED = "PROPERTY_CHANGED";
    public static final String OTHER_EVENTS = "OTHER_EVENTS";
    public static final String NODE_REMOVED = "NODE_REMOVED";
    public static final String NODE_ADDED = "NODE_ADDED";

    TableView<ScenicViewEvent> table = new TableView<ScenicViewEvent>();
    ChoiceBox<String> showStack = new ChoiceBox<String>();
    CheckBox activateTrace = new CheckBox();
    ObservableList<ScenicViewEvent> events = FXCollections.observableArrayList();
    ObservableList<ScenicViewEvent> filteredEvents = FXCollections.observableArrayList();
    SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");
    TextField idFilterField;
    Label selectedNodeLabel = new Label("Filtering is disabled");
    SVNode selectedNode;

    @SuppressWarnings("unchecked") public EventLogPane(final ScenicView view) {
        // selectedNodeLabel.prefWidthProperty().bind(widthProperty().divide(1.1));
        table.setEditable(false);
        table.getStyleClass().add("trace-text-area");
        final TableColumn<ScenicViewEvent, String> sourceCol = new TableColumn<ScenicViewEvent, String>("source");
        sourceCol.setCellValueFactory(new PropertyValueFactory<ScenicViewEvent, String>("source"));
        sourceCol.prefWidthProperty().bind(widthProperty().divide(4));
        final TableColumn<ScenicViewEvent, String> eventTypeCol = new TableColumn<ScenicViewEvent, String>("eventType");
        eventTypeCol.setCellValueFactory(new PropertyValueFactory<ScenicViewEvent, String>("eventType"));
        eventTypeCol.prefWidthProperty().bind(widthProperty().divide(4));
        final TableColumn<ScenicViewEvent, String> eventValueCol = new TableColumn<ScenicViewEvent, String>("eventValue");
        eventValueCol.prefWidthProperty().bind(widthProperty().divide(4));
        eventValueCol.setCellValueFactory(new PropertyValueFactory<ScenicViewEvent, String>("eventValue"));
        final TableColumn<ScenicViewEvent, String> momentCol = new TableColumn<ScenicViewEvent, String>("moment");
        momentCol.setCellValueFactory(new PropertyValueFactory<ScenicViewEvent, String>("moment"));
        momentCol.prefWidthProperty().bind(widthProperty().divide(4));

        table.getColumns().addAll(sourceCol, eventTypeCol, eventValueCol, momentCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setItems(filteredEvents);
        table.setFocusTraversable(false);
        table.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override public void handle(final MouseEvent ev) {
                if (ev.getClickCount() == 2) {
                    final ScenicViewEvent newValue = table.getSelectionModel().getSelectedItem();
                    if (newValue != null) {
                        final StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < newValue.stackTrace.length; i++) {
                            sb.append(newValue.stackTrace[i]).append('\n');
                        }
                        final int width = 700;
                        final int height = 600;
                        final VBox pane = new VBox();
                        final Scene scene = SceneBuilder.create().width(600).height(400).root(pane).stylesheets(ScenicView.STYLESHEETS).build();

                        final Stage stage = StageBuilder.create().style(StageStyle.UTILITY).title("Event Stacktrace").build();
                        stage.initModality(Modality.APPLICATION_MODAL);
                        stage.setScene(scene);
                        stage.getIcons().add(ScenicView.APP_ICON);

                        final Label label = new Label(newValue.toString());
                        stage.setWidth(width);
                        stage.setHeight(height);
                        final TextArea area = new TextArea(sb.toString());
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
                applyFilter();
            }
        });

        final Button b1 = new Button();
        b1.setGraphic(new ImageView(DisplayUtils.CLEAR_IMAGE));
        b1.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent arg0) {
                idFilterField.setText("");
                applyFilter();
            }
        });
        activateTrace.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean arg2) {
                setSelectedNode(selectedNode);
                view.update();
            }
        });
        /**
         * This is an ugly fix for what I think is a bug of the gridPane
         */
        idFilterField.prefWidthProperty().bind(widthProperty().subtract(105));
        GridPane.setHgrow(idFilterField, Priority.ALWAYS);
        GridPane.setHgrow(b1, Priority.NEVER);
        GridPane.setHgrow(showStack, Priority.ALWAYS);
        GridPane.setHgrow(clear, Priority.NEVER);
        GridPane.setHgrow(selectedNodeLabel, Priority.NEVER);

        filtersGridPane.add(new Label("Enable:"), 1, 1);
        filtersGridPane.add(activateTrace, 2, 1, 1, 1);
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

    public void setSelectedNode(final SVNode selectedNode) {
        this.selectedNode = selectedNode;
        if (!activateTrace.isSelected()) {
            selectedNodeLabel.setText("Filtering is disabled");
        } else if (selectedNode != null) {
            selectedNodeLabel.setText("Filtering from current selection: " + selectedNode.getExtendedId());
        } else {
            selectedNodeLabel.setText("Filtering from current selection: Root node");
        }
    }

    public void trace(final EvLogEvent event) {
        trace(event.getSource(), event.getEventType(), event.getEventValue());
    }

    public void trace(final SVNode source, final String eventType, final String eventValue) {
        if (isActive()) {
            if (checkValid(source)) {
                final String sourceId = source.getExtendedId();
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
        if (idFilterField.getText().equals(""))
            return true;
        final String lower = idFilterField.getText().toLowerCase();
        final String[] unparsed = lower.split(" ");
        final String eventData = event.eventType.toLowerCase() + event.eventValue.toLowerCase() + event.source.toLowerCase();
        boolean valid = true;
        boolean and = true;
        boolean not = false;
        for (int i = 0; i < unparsed.length; i++) {
            if (unparsed[i].equals("and") && i < unparsed.length - 1) {
                and = true;
            } else if (unparsed[i].equals("or") && i < unparsed.length - 1) {
                and = false;
            } else if (unparsed[i].equals("not") && i < unparsed.length - 1) {
                not = true;
            } else {
                final boolean actualValid = eventData.indexOf(unparsed[i]) != -1;
                if (and && not)
                    valid &= !actualValid;
                else if (and)
                    valid &= actualValid;
                else if (!and && not)
                    valid |= !actualValid;
                else
                    valid |= actualValid;
                and = true;
                not = false;
            }
        }

        return valid;
    }

    private boolean checkValid(final SVNode node) {
        if (node == null)
            return false;
        return (selectedNode == null) || selectedNode.equals(node) || checkValid(node.getParent());
    }

    public boolean isActive() {
        return activateTrace.isSelected();
    }

    private void applyFilter() {
        final List<ScenicViewEvent> events = new ArrayList<EventLogPane.ScenicViewEvent>();
        for (int i = 0; i < EventLogPane.this.events.size(); i++) {
            if (validForFilter(EventLogPane.this.events.get(i))) {
                events.add(EventLogPane.this.events.get(i));
            }
        }
        EventLogPane.this.filteredEvents.setAll(events);
    }

    public class ScenicViewEvent {

        public String source;
        public String eventType;
        public String eventValue;
        public String moment;
        String relative;
        StackTraceElement[] stackTrace;

        public ScenicViewEvent(final String source, final String eventType, final String eventValue) {
            this.source = source;
            this.eventType = eventType;
            this.eventValue = eventValue;
            this.moment = format.format(new Date());
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
            return "Event [source=" + source + ", eventType=" + eventType + ((eventValue != null && !eventValue.equals("")) ? (", eventValue=" + eventValue) : "") + ", moment=" + moment + "]";
        }

    }

}
