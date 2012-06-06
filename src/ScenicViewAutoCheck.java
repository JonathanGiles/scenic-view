import javafx.application.Application;
import javafx.scene.*;
import javafx.stage.Stage;

import com.javafx.experiments.scenicview.ScenicView;

public class ScenicViewAutoCheck extends Application {

    public static void main(final String[] args) {
        Application.launch(args);
    }

    @Override public void start(final Stage stageTest) throws Exception {
        // workaround for RT-10714
        stageTest.setWidth(40);
        stageTest.setHeight(80);
        stageTest.setTitle("Scenic View Test Test :-P");
        stageTest.setScene(new Scene(new Group()));

        final Stage stage = new Stage();
        // workaround for RT-10714
        stage.setWidth(640);
        stage.setHeight(800);
        stage.setTitle("Scenic View Test");
        final ScenicView example = new ScenicView(stageTest.getScene().getRoot(), stage);
        ScenicView.show(example, stage);
        ScenicView.show(example);
    }

}
