/*
 * Scenic View, 
 * Copyright (C) 2012 Jonathan Giles, Ander Ruiz, Amy Fowler, Matthieu Brouillard
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.scenicview.view.cssfx;

import java.util.function.Predicate;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import org.scenicview.view.ScenicViewGui;

public class CSSFXTabContentController {
    @SuppressWarnings("unused")
    private ScenicViewGui gui;

    @FXML
    TextField tfFilter;
    @FXML
    CheckBox cbNonMapped;
    @FXML
    TableView<MonitoredCSS> items;
    @FXML
    private TableColumn<MonitoredCSS, String> cssColumn;
    @FXML
    private TableColumn<MonitoredCSS, String> mappedByColumn;
    
    private ObjectProperty<ObservableList<MonitoredCSS>> monitoredCSS = new SimpleObjectProperty<>(); 

    @FXML
    public void initialize() {
        tfFilter.setPromptText("CSS URIs or File (contains)");
        
        cssColumn.prefWidthProperty().bind(items.widthProperty().divide(2.0f));
        mappedByColumn.prefWidthProperty().bind(items.widthProperty().divide(2.0f));
        
        cssColumn.setCellValueFactory(cellData -> cellData.getValue().css());
        mappedByColumn.setCellValueFactory(cellData -> cellData.getValue().mappedBy());
        
        monitoredCSS.addListener(new ChangeListener<ObservableList<MonitoredCSS>>() {
            @Override
            public void changed(ObservableValue<? extends ObservableList<MonitoredCSS>> observable, ObservableList<MonitoredCSS> oldValue, ObservableList<MonitoredCSS> newValue) {
                if (newValue == null) {
                    items.setItems(FXCollections.emptyObservableList());
                } else {
                FilteredList<MonitoredCSS> filteredData = new FilteredList<>(newValue, p -> true);
                
                final ChangeListener<Object> anyFilterChange = new ChangeListener<Object>() {
                    @Override
                    public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {
                        filteredData.setPredicate(new Predicate<MonitoredCSS>() {
                            @Override
                            public boolean test(MonitoredCSS t) {
                                boolean isEmpty = t.getMappedBy() == null || "".equals(t.getMappedBy());
                                if (cbNonMapped.isSelected() && !isEmpty) {
                                    return false;
                                }
                                
                                String filter = tfFilter.getText();
                                return t.getCSS().contains(filter) || (t.getMappedBy() != null && t.getMappedBy().contains(filter));
                            }
                        });
                    }
                };
                cbNonMapped.selectedProperty().addListener(anyFilterChange);
                tfFilter.textProperty().addListener(anyFilterChange);
                
                SortedList<MonitoredCSS> sortedData = new SortedList<>(filteredData);
                sortedData.comparatorProperty().bind(items.comparatorProperty());
                items.setItems(sortedData);                
                }
            }
        });
    }

    public void setScenicViewGui(ScenicViewGui gui) {
        this.gui = gui;
    }

    public void setMonitoredCSS(ObservableList<MonitoredCSS> stageCSS) {
        monitoredCSS.set(stageCSS);
    }
    public ObservableList<MonitoredCSS> getMonitoredCSS() {
        return monitoredCSS.get();
    }
}
