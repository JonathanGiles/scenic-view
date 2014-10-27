/*
 * Scenic View, 
 * Copyright (C) 2014 Jonathan Giles, Ander Ruiz, Amy Fowler, Arnaud Nouard
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
package org.scenicview.view.threedom;

import java.util.List;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import org.fxconnector.ConnectorUtils;
import org.fxconnector.node.SVNode;

/**
 * 2D to 3D Miss: a snapshot parameter to only capture container and not
 * descendants Replace Tile3D by a box with only one textured face
 */
public class ThreeDOM implements ITile3DListener {

    @FXML
    Slider slider;
    @FXML
    CheckBox checkBoxAxes;
    @FXML
    Label overTileText;
    @FXML
    Button reload;
    @FXML
    TitledPane controls;
    @FXML
    Accordion accordion;
    @FXML
    BorderPane subSceneContainer;

    static final double FACTOR2D3D = 1;
    static final double AXES_SIZE = 400;

    public boolean onlyOnce = false;

    double translateRootX;
    double translateRootY;
    ParallelTransition depthTransition;

    double maxDepth = 0;

    double THICKNESS = 10;
    Group root3D;
    SVNode currentRoot2D;
    IThreeDOM iThreeDOM;
    Tile3D currentSelectedNode;
    Translate translateCamera;

    RotateTransition rotateTransition;

    public void setHolder(IThreeDOM h) {
        iThreeDOM = h;
    }

    public Parent createContent(SVNode root2D) throws Exception {
        currentRoot2D = root2D;
        // Build the Scene Graph
        Pane root = null;
        // UI part of the decoration
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("threedom.fxml"));
            fxmlLoader.setController(this);
            root = (BorderPane) fxmlLoader.load();
        } catch (Exception ex) {
            System.err.println(ex);
        }

        Group world = new Group();
        // Create and position camera
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setFieldOfView(70);
        camera.setNearClip(0.1);
        camera.setFarClip(10000);
        Rotate rotateX = new Rotate(20, Rotate.X_AXIS);
        Rotate rotateY = new Rotate(20, Rotate.Y_AXIS);
        translateCamera = new Translate(0, 0, -600);
        camera.getTransforms().addAll(
                rotateY,
                rotateX,
                translateCamera);

        world.getChildren().add(camera);
        rotateTransition = new RotateTransition(Duration.seconds(2));
        rotateTransition.setDelay(Duration.seconds(1));
        rotateTransition.setNode(camera);
        rotateTransition.setAxis(new Point3D(0, 1, 0));
        rotateTransition.setByAngle(30);

        Group axes3DRoot = new Group();

        root3D = new Group();
        world.getChildren().addAll(axes3DRoot, root3D);

        // Use a SubScene       
        SubScene subScene = new SubScene(world, 1024, 768, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.WHITE);
        subScene.setCamera(camera);

        // Mouse
        DragSupport dragSupport = new DragSupport(subScene, null, Orientation.HORIZONTAL, rotateY.angleProperty());
        DragSupport dragSupport2 = new DragSupport(subScene, null, Orientation.VERTICAL, rotateX.angleProperty());
        ZoomSupport zoomSupport = new ZoomSupport(subScene, null, MouseButton.NONE, Orientation.VERTICAL, translateCamera.zProperty(), translateCamera.xProperty(), 1);

        controls.setExpanded(true);
        accordion.setExpandedPane(controls);
        //
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setBlockIncrement(2);
        slider.setOrientation(Orientation.HORIZONTAL);
        slider.setPrefWidth(200);
        slider.setPrefHeight(30);
        slider.setValue(maxDepth);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            setDepth(slider.getValue(), root3D);
        });
        //
        reload.setOnAction((ActionEvent event) -> {
            _reload();
        });

        subSceneContainer.setPrefSize(600, 500);
        subSceneContainer.setMinSize(60, 50);
        subSceneContainer.setCenter(subScene);
        subScene.heightProperty().bind(subSceneContainer.heightProperty());
        subScene.widthProperty().bind(subSceneContainer.widthProperty());

        buildAxes(axes3DRoot);        // Axes

        if (!onlyOnce) {
            init3D(true);
            Bounds layoutBounds = root3D.getLayoutBounds();
            translateRootX = -layoutBounds.getWidth() / 2;
            translateRootY = -layoutBounds.getHeight() / 2;
            root3D.getTransforms().add(new Translate(translateRootX, translateRootY));
            // Scale to Scene's size
            double zoom = 600 * (layoutBounds.getWidth() / 600);
            translateCamera.setZ(-zoom);
        }

        return root;
    }

    void init3D(boolean animate) {
        onlyOnce = true;
        maxDepth = 0;
        if (animate) {
            depthTransition = new ParallelTransition();
            depthTransition.setDelay(Duration.seconds(1));
            depthTransition.setInterpolator(Interpolator.EASE_OUT);
        } else {
            depthTransition = null;
        }

        from2Dto3D(currentRoot2D, root3D, 0);

        slider.setMax(maxDepth);
        slider.setValue(maxDepth);
        if (animate) {
            if (rotateTransition != null) {
                depthTransition.getChildren().add(rotateTransition);
            }
            depthTransition.play();
        }
    }

    private Node from2Dto3D(SVNode root2D, Group root3D, double depth) {
        // Parent3D
        Node node3D = nodeToTile3D(root2D, FACTOR2D3D, depth);
        root3D.getChildren().add(node3D);
        depth += 3;
        List<SVNode> childrenUnmodifiable = root2D.getChildren();
        for (SVNode svnode : childrenUnmodifiable) {

            if (!ConnectorUtils.isNormalNode(svnode)) {
                continue;
            }
            Node node = svnode.getImpl();
            if (node.isVisible() && node instanceof Parent) {
                from2Dto3D(svnode, root3D, depth);
            } else if (node.isVisible()) {
                node3D = nodeToTile3D(svnode, FACTOR2D3D, depth);
                root3D.getChildren().add(node3D);
            }
        }
        if (depth > maxDepth) {
            maxDepth = depth;
        }
        return node3D;
    }

    private Node nodeToTile3D(SVNode node2D, double factor2d3d, double depth) {

        Tile3D tile = new Tile3D(currentRoot2D, factor2d3d, node2D, depth, THICKNESS, this, iThreeDOM);

        if (depthTransition != null && depth > 1) {
            TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(2));
            translateTransition.setInterpolator(Interpolator.EASE_OUT);
            translateTransition.setNode(tile);
            translateTransition.setDelay(Duration.seconds(2));
            translateTransition.setToZ(-depth * THICKNESS);
            depthTransition.getChildren().add(translateTransition);
        } else {
            tile.setTranslateZ(-depth * THICKNESS);
        }

        return tile;
    }

    public void removeNode(SVNode node) {
        Tile3D found = find(node);
        if (found != null) {
            root3D.getChildren().remove(found);
        }
    }

    public void reload() {
        _reload();
    }

    void _reload() {
        root3D.getChildren().clear();
        init3D(false);
    }

    private void setDepth(double depth, Group world) {
        ObservableList<Node> childrenUnmodifiable = world.getChildrenUnmodifiable();
        childrenUnmodifiable.stream().filter((child) -> (child instanceof Tile3D)).forEach((Node child) -> {
            double cDepth = ((Tile3D) child).get3Depth();
            if (cDepth > depth) {
                child.setVisible(false);
            } else {
                child.setVisible(true);
            }
        });
    }

    @Override
    public void onMouseMovedOnTile(String s) {
        overTileText.setText(s);
    }

    @Override
    public void onMouseClickedOnTile(Tile3D tile) {
        iThreeDOM.clickOnTile(tile.getSVNode());
    }

    public void clearSelection() {
        if (currentSelectedNode != null) {
            ((PhongMaterial) currentSelectedNode.getMaterial()).setDiffuseColor(Color.WHITE);
            ((Tile3D) currentSelectedNode).snapshot();   // In case of changes
        }
    }

    public void setSelectedTile(Tile3D tile) {
        // Simulate SV behavior
        if (currentSelectedNode != null) {
            ((PhongMaterial) currentSelectedNode.getMaterial()).setDiffuseColor(Color.WHITE);
            final Node currentSelectedNodeToUnselect = currentSelectedNode;
            // Wait for principal UI to be updated (the selection)
            Platform.runLater(() -> {
                ((Tile3D) currentSelectedNodeToUnselect).snapshot();   // In case of changes on 2D node after edit
            });

        }
        currentSelectedNode = tile;
        ((PhongMaterial) tile.getMaterial()).setDiffuseColor(Color.YELLOW); // Simulate SV selection color
    }

    private void buildAxes(Group world) {
        PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);
        PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(Color.DARKGREEN);
        greenMaterial.setSpecularColor(Color.GREEN);
        PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(Color.DARKBLUE);
        blueMaterial.setSpecularColor(Color.BLUE);

        Box xAxis = new Box(AXES_SIZE, 1, 1);
        xAxis.setMaterial(redMaterial);
        Box yAxis = new Box(1, AXES_SIZE, 1);
        yAxis.setMaterial(greenMaterial);
        Box zAxis = new Box(1, 1, AXES_SIZE);
        zAxis.setMaterial(blueMaterial);

        Group group = new Group();
        group.getChildren().addAll(xAxis, yAxis, zAxis);
        group.setVisible(true);
        world.getChildren().addAll(group);
        group.visibleProperty().bind(checkBoxAxes.selectedProperty());
    }

    static public ThreeDOM getInstance() {
        return new ThreeDOM();
    }

    public Tile3D find(SVNode node) {
        ObservableList<Node> childrenUnmodifiable = root3D.getChildrenUnmodifiable();
        for (Node n : childrenUnmodifiable) {
            if (n instanceof Tile3D) {
                if (((Tile3D) n).getSVNode().equals(node)) {
                    return (Tile3D) n;
                }
            }
        }
        return null;
    }

}
