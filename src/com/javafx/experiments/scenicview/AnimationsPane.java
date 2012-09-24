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

public abstract class AnimationsPane extends VBox implements ContextMenuContainer {

    private final Map<Integer, List<SVAnimation>> appsAnimations = new HashMap<Integer, List<SVAnimation>>();

    private static final Image PAUSE = DisplayUtils.getUIImage("pause.png");

    ScenicView view;

    public AnimationsPane(final ScenicView view) {
        this.view = view;
    }

    void clear() {
        appsAnimations.clear();
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
            final ObservableList<SVAnimation> animationsItems = FXCollections.observableArrayList();
            animationsItems.addAll(animationsApp);
            final TableView<SVAnimation> table = new TableView<SVAnimation>();
            table.setEditable(false);
            table.getStyleClass().add("animations-table-view");
            final TableColumn<SVAnimation, String> sourceCol = new TableColumn<SVAnimation, String>("Animation ID");
            sourceCol.setCellValueFactory(new PropertyValueFactory<SVAnimation, String>("toString"));
            sourceCol.prefWidthProperty().bind(widthProperty().multiply(0.40));
            final TableColumn<SVAnimation, String> rateCol = new TableColumn<SVAnimation, String>("Rate");
            rateCol.setCellValueFactory(new PropertyValueFactory<SVAnimation, String>("rate"));
            rateCol.prefWidthProperty().bind(widthProperty().multiply(0.1));
            final TableColumn<SVAnimation, String> cycleCountCol = new TableColumn<SVAnimation, String>("Cycle count");
            cycleCountCol.prefWidthProperty().bind(widthProperty().multiply(0.2));

            cycleCountCol.setCellValueFactory(new PropertyValueFactory<SVAnimation, String>("cycleCount"));
            final TableColumn<SVAnimation, String> currentTimeCol = new TableColumn<SVAnimation, String>("Current time");
            currentTimeCol.setCellValueFactory(new PropertyValueFactory<SVAnimation, String>("currentTime"));
            currentTimeCol.prefWidthProperty().bind(widthProperty().multiply(0.25));
            final TableColumn<SVAnimation, Integer> pauseCol = new TableColumn<SVAnimation, Integer>("");
            pauseCol.setCellValueFactory(new PropertyValueFactory<SVAnimation, Integer>("id"));
            pauseCol.setCellFactory(new Callback<TableColumn<SVAnimation, Integer>, TableCell<SVAnimation, Integer>>() {

                @Override public TableCell<SVAnimation, Integer> call(final TableColumn<SVAnimation, Integer> arg0) {
                    final TableCell<SVAnimation, Integer> cell = new TableCell<SVAnimation, Integer>() {
                        @Override public void updateItem(final Integer item, final boolean empty) {
                            if (item != null) {
                                setGraphic(new ImageView(PAUSE));
                                setId(Integer.toString(item));
                            }
                        }
                    };
                    cell.setOnMousePressed(new EventHandler<MouseEvent>() {

                        @Override public void handle(final MouseEvent arg0) {
                            view.pauseAnimation(stageID, Integer.parseInt(cell.getId()));
                        }
                    });
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
