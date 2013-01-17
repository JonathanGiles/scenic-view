/*
 * Copyright (c) 2012 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.scenicview;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import org.fxconnector.event.EvLogEvent;
import org.fxconnector.node.SVNode;
import org.scenicview.control.FilterTextField;
import org.scenicview.dialog.InfoBox;

class EventLogPane extends VBox implements ContextMenuContainer {

    private static final int MAX_EVENTS = 5000;

    TableView<ScenicViewEvent> table = new TableView<ScenicViewEvent>();
    ChoiceBox<String> showStack = new ChoiceBox<String>();
    CheckMenuItem activateTrace = new CheckMenuItem("Enable Event Tracing");
    ObservableList<ScenicViewEvent> events = FXCollections.observableArrayList();
    ObservableList<ScenicViewEvent> filteredEvents = FXCollections.observableArrayList();
    SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");
    FilterTextField idFilterField;
    Label selectedNodeLabel = new Label("Enable event tracing in the Events menu");
    SVNode selectedNode;

    Menu menu;

    private static final Image MORE_INFO = DisplayUtils.getUIImage("info.png");

    @SuppressWarnings("unchecked") EventLogPane(final ScenicView view) {
        table.setEditable(false);
        table.getStyleClass().add("trace-text-area");
        final DoubleBinding size = widthProperty().subtract(MORE_INFO.getWidth() + 7).divide(4);
        final TableColumn<ScenicViewEvent, String> sourceCol = new TableColumn<ScenicViewEvent, String>("source");
        sourceCol.setCellValueFactory(new PropertyValueFactory<ScenicViewEvent, String>("source"));
        sourceCol.prefWidthProperty().bind(size);
        final TableColumn<ScenicViewEvent, String> eventTypeCol = new TableColumn<ScenicViewEvent, String>("eventType");
        eventTypeCol.setCellValueFactory(new PropertyValueFactory<ScenicViewEvent, String>("eventType"));
        eventTypeCol.prefWidthProperty().bind(size);
        final TableColumn<ScenicViewEvent, String> eventValueCol = new TableColumn<ScenicViewEvent, String>("eventValue");
        eventValueCol.prefWidthProperty().bind(size);
        eventValueCol.setCellValueFactory(new PropertyValueFactory<ScenicViewEvent, String>("eventValue"));
        final TableColumn<ScenicViewEvent, String> momentCol = new TableColumn<ScenicViewEvent, String>("moment");
        momentCol.setCellValueFactory(new PropertyValueFactory<ScenicViewEvent, String>("moment"));
        momentCol.prefWidthProperty().bind(size);
        final TableColumn<ScenicViewEvent, StackTraceElement[]> moreInfoCol = new TableColumn<ScenicViewEvent, StackTraceElement[]>("info");
        moreInfoCol.setCellValueFactory(new PropertyValueFactory<ScenicViewEvent, StackTraceElement[]>("stackTrace"));
        moreInfoCol.setCellFactory(new Callback<TableColumn<ScenicViewEvent, StackTraceElement[]>, TableCell<ScenicViewEvent, StackTraceElement[]>>() {

            @Override public TableCell<ScenicViewEvent, StackTraceElement[]> call(final TableColumn<ScenicViewEvent, StackTraceElement[]> arg0) {
                final TableCell<ScenicViewEvent, StackTraceElement[]> cell = new TableCell<ScenicViewEvent, StackTraceElement[]>() {

                    @Override public void updateItem(final StackTraceElement[] item, final boolean empty) {
                        if (item != null) {
                            setGraphic(new ImageView(MORE_INFO));
                            setId("");
                            setAlignment(Pos.CENTER);
                        }
                    }
                };
                cell.setOnMousePressed(new EventHandler<MouseEvent>() {

                    @SuppressWarnings("unused")
                    @Override public void handle(final MouseEvent arg0) {
                        final ScenicViewEvent newValue = table.getSelectionModel().getSelectedItem();
                        if (newValue != null) {
                            final StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < newValue.stackTrace.length; i++) {
                                sb.append(newValue.stackTrace[i]).append('\n');
                            }
                            new InfoBox("Event Stacktrace", newValue.toString(), sb.toString());
                        }

                    }
                });
                return cell;
            }
        });
        moreInfoCol.setPrefWidth(MORE_INFO.getWidth() + 12);
        moreInfoCol.setResizable(false);

        table.getColumns().addAll(sourceCol, eventTypeCol, eventValueCol, momentCol, moreInfoCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setItems(filteredEvents);
        table.setFocusTraversable(false);

        getStyleClass().add("structure-trace-pane");

        final GridPane filtersGridPane = new GridPane();
        filtersGridPane.setVgap(5);
        filtersGridPane.setHgap(5);
        filtersGridPane.setSnapToPixel(true);
        filtersGridPane.setPadding(new Insets(0, 5, 5, 0));
        filtersGridPane.setId("structure-trace-grid-pane");

        idFilterField = new FilterTextField();
        idFilterField.setMinHeight(USE_PREF_SIZE);
        idFilterField.setPromptText("Insert text to filter (logical operations supported)");
        idFilterField.focusedProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(final ObservableValue<? extends Boolean> arg0, final Boolean arg1, final Boolean newValue) {
                if (newValue)
                    ScenicView.setStatusText("Type any text for filtering (logical expressions NOT AND and OR are supported, example NOT MOUSE_MOVED AND TilePane)");
                else
                    ScenicView.clearStatusText();
            }
        });
        idFilterField.getTextField().setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override public void handle(final KeyEvent arg0) {
                applyFilter();
            }
        });
        idFilterField.setOnButtonClick(new Runnable() {
            @Override public void run() {
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
        GridPane.setHgrow(showStack, Priority.ALWAYS);
        GridPane.setHgrow(selectedNodeLabel, Priority.NEVER);

        filtersGridPane.add(selectedNodeLabel, 1, 1, 3, 1);
        filtersGridPane.add(new Label("Text Filter:"), 1, 2);
        filtersGridPane.add(idFilterField, 2, 2);
        filtersGridPane.setPrefHeight(60);
        VBox.setMargin(table, new Insets(0, 5, 5, 5));

        getChildren().addAll(filtersGridPane, table);
        VBox.setVgrow(table, Priority.ALWAYS);
    }

    public void setSelectedNode(final SVNode selectedNode) {
        this.selectedNode = selectedNode;
        if (!activateTrace.isSelected()) {
            selectedNodeLabel.setText("Enable event tracing in the Events menu");
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
                if (events.size() > MAX_EVENTS) {
                    if (events.size() == filteredEvents.size()) {
                        final ScenicViewEvent oldEvent = events.remove(0);
                        filteredEvents.remove(oldEvent);
                    } else {
                        // Try to find the first unfiltered event
                        for (int i = 0; i < events.size(); i++) {
                            if (!filteredEvents.contains(events.get(i))) {
                                events.remove(i);
                                break;
                            }
                        }
                    }
                }
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

    @Override public Menu getMenu() {
        if (menu == null) {
            menu = new Menu("Events");
            final MenuItem clear = new MenuItem("Clear events");
            clear.setOnAction(new EventHandler<ActionEvent>() {

                @Override public void handle(final ActionEvent arg0) {
                    events.clear();
                    filteredEvents.clear();
                }
            });
            menu.getItems().addAll(activateTrace, clear);
        }
        return menu;
    }

    private boolean checkValid(final SVNode node) {
        if (node == null)
            return false;
        return (selectedNode == null) || selectedNode.equals(node) || checkValid(node.getParent());
    }

    private boolean isActive() {
        return activateTrace.isSelected();
    }

    public ReadOnlyBooleanProperty activeProperty() {
        return activateTrace.selectedProperty();
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
        public StackTraceElement[] stackTrace;

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

        public StackTraceElement[] getStackTrace() {
            return stackTrace;
        }

        public void setStackTrace(final StackTraceElement[] stackTrace) {
            this.stackTrace = stackTrace;
        }

    }

}
