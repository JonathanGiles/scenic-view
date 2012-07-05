package com.javafx.experiments.scenicview;

import java.util.*;

import javafx.collections.*;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.Callback;

import com.javafx.experiments.scenicview.connector.*;

public class AnimationsPane extends VBox {

    private final Map<Integer, List<SVAnimation>> appsAnimations = new HashMap<Integer, List<SVAnimation>>();

    private static final Image PAUSE = DisplayUtils.getUIImage("pause.png");

    public AnimationsPane() {
        // TODO Auto-generated constructor stub
    }

    @SuppressWarnings("unchecked") public void update(final StageID stageID, final List<SVAnimation> animations) {

        appsAnimations.put(stageID.getAppID(), animations);

        getChildren().clear();

        for (final Iterator<Integer> iterator = appsAnimations.keySet().iterator(); iterator.hasNext();) {
            final Integer app = iterator.next();
            final TitledPane pane = new TitledPane();
            pane.setText("Animations for VM - " + app);

            final List<SVAnimation> animationsApp = appsAnimations.get(app);
            getChildren().add(pane);

            final VBox box = new VBox();
            box.prefWidthProperty().bind(pane.widthProperty());
            final ObservableList<SVAnimation> filteredEvents = FXCollections.observableArrayList();
            filteredEvents.addAll(animationsApp);
            final TableView<SVAnimation> table = new TableView<SVAnimation>();
            table.setEditable(false);
            table.getStyleClass().add("trace-text-area");
            final TableColumn<SVAnimation, String> sourceCol = new TableColumn<SVAnimation, String>("Animation ID");
            sourceCol.setCellValueFactory(new PropertyValueFactory<SVAnimation, String>("toString"));
            sourceCol.prefWidthProperty().bind(widthProperty().multiply(0.40));
            final TableColumn<SVAnimation, String> eventTypeCol = new TableColumn<SVAnimation, String>("Rate");
            eventTypeCol.setCellValueFactory(new PropertyValueFactory<SVAnimation, String>("rate"));
            eventTypeCol.prefWidthProperty().bind(widthProperty().multiply(0.2));
            final TableColumn<SVAnimation, String> eventValueCol = new TableColumn<SVAnimation, String>("Cycle count");
            eventValueCol.prefWidthProperty().bind(widthProperty().multiply(0.2));
            eventValueCol.setCellValueFactory(new PropertyValueFactory<SVAnimation, String>("cycleCount"));
            final TableColumn<SVAnimation, String> momentCol = new TableColumn<SVAnimation, String>("Current time");
            momentCol.setCellValueFactory(new PropertyValueFactory<SVAnimation, String>("currentTime"));
            momentCol.prefWidthProperty().bind(widthProperty().multiply(0.15));
            final TableColumn<SVAnimation, String> pauseCol = new TableColumn<SVAnimation, String>("Pause");
            pauseCol.setCellValueFactory(new PropertyValueFactory<SVAnimation, String>("toString"));
            pauseCol.setCellFactory(new Callback<TableColumn<SVAnimation, String>, TableCell<SVAnimation, String>>() {

                @Override public TableCell<SVAnimation, String> call(final TableColumn<SVAnimation, String> arg0) {
                    final TableCell<SVAnimation, String> cell = new TableCell<SVAnimation, String>() {
                        @Override public void updateItem(final String item, final boolean empty) {
                            if (item != null) {
                                setGraphic(new ImageView(PAUSE));
                            }
                        }
                    };
                    cell.setOnMousePressed(new EventHandler<MouseEvent>() {

                        @Override public void handle(final MouseEvent arg0) {
                            System.out.println("Pause animation");
                        }
                    });
                    return cell;
                }
            });
            pauseCol.prefWidthProperty().bind(widthProperty().multiply(0.1));

            table.getColumns().addAll(sourceCol, eventTypeCol, eventValueCol, momentCol, pauseCol);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            table.setItems(filteredEvents);
            table.setFocusTraversable(false);
            box.getChildren().add(table);
            VBox.setMargin(table, new Insets(5, 5, 5, 5));
            VBox.setVgrow(table, Priority.ALWAYS);
            pane.setContent(box);
        }

    }

}
