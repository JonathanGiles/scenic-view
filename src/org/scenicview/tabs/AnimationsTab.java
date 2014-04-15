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
package org.scenicview.tabs;

import org.fxconnector.StageID;
import org.fxconnector.SVAnimation;
import org.scenicview.ContextMenuContainer;
import org.scenicview.DisplayUtils;
import org.scenicview.ScenicView;

import java.util.*;

import javafx.collections.*;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.Callback;


public class AnimationsTab extends Tab implements ContextMenuContainer {

    private final Map<Integer, List<SVAnimation>> appsAnimations = new HashMap<Integer, List<SVAnimation>>();

    private static final Image PAUSE = DisplayUtils.getUIImage("pause.png");

    private final ScenicView scenicView;
    private final VBox vbox;

    private Menu menu;

    public AnimationsTab(final ScenicView view) {
        this.scenicView = view;
        this.vbox = new VBox();

        setText("Animations");
        setContent(vbox);
        setGraphic(new ImageView(DisplayUtils.getUIImage("cinema.png")));
        setClosable(false);
        selectedProperty().addListener((o, oldValue, newValue) -> scenicView.updateAnimations());
    }

    public void clear() {
        appsAnimations.clear();
    }

    @Override public Menu getMenu() {
        if (menu == null) {
            menu = new Menu("Animations");
            final CheckMenuItem animationsEnabled = scenicView.buildCheckMenuItem("Animations enabled", "Animations will run on the application",
                    "Animations will be stopped", null, true);

            animationsEnabled.selectedProperty().addListener((o, oldValue, newValue) -> {
                scenicView.animationsEnabled(animationsEnabled.isSelected());
            });
            menu.getItems().add(animationsEnabled);
        }
        return menu;
    }

    @SuppressWarnings("unchecked") 
    public void update(final StageID stageID, final List<SVAnimation> animations) {
        appsAnimations.put(stageID.getAppID(), animations);

        vbox.getChildren().clear();

        for (final Iterator<Integer> iterator = appsAnimations.keySet().iterator(); iterator.hasNext();) {
            final Integer app = iterator.next();
            final TitledPane pane = new TitledPane();
            pane.setPrefHeight(vbox.getHeight() / appsAnimations.size());
            pane.setText("Animations for VM - " + app);

            final List<SVAnimation> animationsApp = appsAnimations.get(app);
            vbox.getChildren().add(pane);

            final VBox box = new VBox();
            box.prefWidthProperty().bind(pane.widthProperty());
            final ObservableList<SVAnimation> animationsItems = FXCollections.observableArrayList();
            animationsItems.addAll(animationsApp);
            final TableView<SVAnimation> table = new TableView<SVAnimation>();
            table.setEditable(false);
            table.getStyleClass().add("animations-table-view");
            final TableColumn<SVAnimation, String> sourceCol = new TableColumn<SVAnimation, String>("Animation ID");
            sourceCol.setCellValueFactory(new PropertyValueFactory<SVAnimation, String>("toString"));
            sourceCol.prefWidthProperty().bind(vbox.widthProperty().multiply(0.40));
            final TableColumn<SVAnimation, String> rateCol = new TableColumn<SVAnimation, String>("Rate");
            rateCol.setCellValueFactory(new PropertyValueFactory<SVAnimation, String>("rate"));
            rateCol.prefWidthProperty().bind(vbox.widthProperty().multiply(0.1));
            final TableColumn<SVAnimation, String> cycleCountCol = new TableColumn<SVAnimation, String>("Cycle count");
            cycleCountCol.prefWidthProperty().bind(vbox.widthProperty().multiply(0.2));

            cycleCountCol.setCellValueFactory(new PropertyValueFactory<SVAnimation, String>("cycleCount"));
            final TableColumn<SVAnimation, String> currentTimeCol = new TableColumn<SVAnimation, String>("Current time");
            currentTimeCol.setCellValueFactory(new PropertyValueFactory<SVAnimation, String>("currentTime"));
            currentTimeCol.prefWidthProperty().bind(vbox.widthProperty().multiply(0.20));
            final TableColumn<SVAnimation, Integer> pauseCol = new TableColumn<SVAnimation, Integer>("");
            pauseCol.setCellValueFactory(new PropertyValueFactory<SVAnimation, Integer>("id"));
            pauseCol.setCellFactory(new Callback<TableColumn<SVAnimation, Integer>, TableCell<SVAnimation, Integer>>() {

                @Override public TableCell<SVAnimation, Integer> call(final TableColumn<SVAnimation, Integer> arg0) {
                    final TableCell<SVAnimation, Integer> cell = new TableCell<SVAnimation, Integer>() {
                        @Override public void updateItem(final Integer item, final boolean empty) {
                            if (item != null) {
                                setGraphic(new ImageView(PAUSE));
                                setId(Integer.toString(item));
                                setAlignment(Pos.CENTER);
                            }
                        }
                    };
                    cell.setOnMousePressed(new EventHandler<MouseEvent>() {

                        @Override public void handle(final MouseEvent arg0) {
                            scenicView.pauseAnimation(stageID, Integer.parseInt(cell.getId()));
                        }
                    });
                    cell.setAlignment(Pos.CENTER);
                    return cell;
                }
            });
            pauseCol.setPrefWidth(PAUSE.getWidth() + 7);
            pauseCol.setResizable(false);

            table.getColumns().addAll(sourceCol, rateCol, cycleCountCol, currentTimeCol, pauseCol);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            table.setItems(animationsItems);
            table.setFocusTraversable(false);
            box.getChildren().add(table);
            VBox.setMargin(table, new Insets(5, 5, 5, 5));
            VBox.setVgrow(table, Priority.ALWAYS);
            pane.setContent(box);
        }

    }

}
