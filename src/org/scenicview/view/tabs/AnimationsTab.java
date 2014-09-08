/*
 * Scenic View, 
 * Copyright (C) 2012 Jonathan Giles, Ander Ruiz, Amy Fowler 
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
package org.scenicview.view.tabs;

import org.fxconnector.StageID;
import org.fxconnector.SVAnimation;
import org.scenicview.view.ContextMenuContainer;
import org.scenicview.view.DisplayUtils;
import org.scenicview.view.ScenicViewGui;

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
    
    public static final String TAB_NAME = "Animations";

    private final Map<Integer, List<SVAnimation>> appsAnimations = new HashMap<>();

    private static final Image PAUSE = DisplayUtils.getUIImage("pause.png");

    private final ScenicViewGui scenicView;
    private final VBox vbox;

    private Menu menu;

    public AnimationsTab(final ScenicViewGui view) {
        super(TAB_NAME);
        this.scenicView = view;
        this.vbox = new VBox();

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
            final TableView<SVAnimation> table = new TableView<>();
            table.setEditable(false);
            table.getStyleClass().add("animations-table-view");
            final TableColumn<SVAnimation, String> sourceCol = new TableColumn<>("Animation ID");
            sourceCol.setCellValueFactory(new PropertyValueFactory<SVAnimation, String>("toString"));
            sourceCol.prefWidthProperty().bind(vbox.widthProperty().multiply(0.40));
            final TableColumn<SVAnimation, String> rateCol = new TableColumn<>("Rate");
            rateCol.setCellValueFactory(new PropertyValueFactory<SVAnimation, String>("rate"));
            rateCol.prefWidthProperty().bind(vbox.widthProperty().multiply(0.1));
            final TableColumn<SVAnimation, String> cycleCountCol = new TableColumn<>("Cycle count");
            cycleCountCol.prefWidthProperty().bind(vbox.widthProperty().multiply(0.2));

            cycleCountCol.setCellValueFactory(new PropertyValueFactory<SVAnimation, String>("cycleCount"));
            final TableColumn<SVAnimation, String> currentTimeCol = new TableColumn<>("Current time");
            currentTimeCol.setCellValueFactory(new PropertyValueFactory<SVAnimation, String>("currentTime"));
            currentTimeCol.prefWidthProperty().bind(vbox.widthProperty().multiply(0.20));
            final TableColumn<SVAnimation, Integer> pauseCol = new TableColumn<>("");
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
