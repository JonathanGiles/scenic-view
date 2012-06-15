package com.javafx.experiments.scenicview.dialog;

import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import com.javafx.experiments.scenicview.*;

public class AboutBox {
    private static final int SCENE_WIDTH = 476;
    private static final int SCENE_HEIGHT = 464;
    private static final int LEFT_AND_RIGHT_MARGIN = 30;
    private static final int SPACER_Y = 38;
    private final VBox panel;
    private final Stage stage;
    private final Scene scene;
    private final ImageView header;
    private final Button footer;
    private final TextArea textArea;

    private AboutBox(final String title, final double x, final double y) {
        this.panel = new VBox();
        this.panel.getStyleClass().add("about");

        this.footer = new Button("Close");
        this.footer.setDefaultButton(true);
        this.footer.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent arg0) {
                stage.close();
            }
        });
        VBox.setMargin(this.footer, new Insets(SPACER_Y / 2, LEFT_AND_RIGHT_MARGIN, SPACER_Y / 2, LEFT_AND_RIGHT_MARGIN));

        this.header = ((ImageViewBuilder.create().id("AboutHeader")).image(DisplayUtils.getUIImage("about-header.png"))).build();

        VBox.setMargin(this.header, new Insets(42.0D, LEFT_AND_RIGHT_MARGIN, 0.0D, LEFT_AND_RIGHT_MARGIN));

        this.textArea = new TextArea();
        this.textArea.setFocusTraversable(false);
        this.textArea.setEditable(false);
        this.textArea.setId("aboutDialogDetails");
        this.textArea.setText(getAboutText());
        this.textArea.setWrapText(true);
        this.textArea.setPrefHeight(221.0D);
        VBox.setMargin(this.textArea, new Insets(SPACER_Y, LEFT_AND_RIGHT_MARGIN, 0.0D, LEFT_AND_RIGHT_MARGIN));
        VBox.setVgrow(this.textArea, Priority.ALWAYS);
        this.panel.setAlignment(Pos.TOP_CENTER);
        this.panel.getChildren().addAll(this.header, this.textArea, this.footer);

        this.scene = SceneBuilder.create().width(SCENE_WIDTH).height(SCENE_HEIGHT).root(this.panel).stylesheets(ScenicView.STYLESHEETS).build();

        this.stage = StageBuilder.create().style(StageStyle.UTILITY).title(title).build();
        this.stage.initModality(Modality.APPLICATION_MODAL);
        this.stage.setScene(this.scene);
        this.stage.getIcons().add(ScenicView.APP_ICON);
        this.stage.setResizable(false);
        this.stage.setX(x);
        this.stage.setY(y);
        this.stage.show();
    }

    public static AboutBox make(final String title, final Stage stage) {
        return new AboutBox(title, stage.getX() + (stage.getWidth() / 2) - (SCENE_WIDTH / 2), stage.getY() + (stage.getHeight() / 2) - (SCENE_HEIGHT / 2));
    }

    private static String getAboutText() {
        final String text = "JavaFX Scenic View "+ScenicView.VERSION
                + "\n"
                + "\n"
                + "JavaFX Build Information:"
                + "\n"
                + "Java FX " + System.getProperty("javafx.runtime.version")
                + "\n"
                + "\n"
                + "Operating System\n"+ System.getProperty("os.name")
                + ", "
                + System.getProperty("os.arch")
                + ", "
                + System.getProperty("os.version")
                + "\n\nJava Version\n"
                + System.getProperty("java.version")
                + ", "
                + System.getProperty("java.vendor")
                + ", "
                + System.getProperty("java.runtime.version");

        return text;
    }
}