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

import javafx.collections.ObservableList;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Translate;
import org.fxconnector.node.SVNode;

public class Tile3D extends Box {

    private PhongMaterial material;
    public WritableImage selectedImage;
    public WritableImage writableImage;
    SVNode node2d;
    double depth;
    SVNode currentRoot2D;
    double factor2d3d;
    ITile3DListener iTile3DListener;
    IThreeDOM iThreeDOM;

    public Tile3D(SVNode currentRoot2D, double factor2d3d, SVNode node2D, double depth, double thickness, ITile3DListener l, IThreeDOM i) {
        this.depth = depth;
        this.currentRoot2D = currentRoot2D;
        this.factor2d3d = factor2d3d;
        this.iTile3DListener = l;
        this.iThreeDOM = i;

        node2d = node2D;
        material = new PhongMaterial();
        material.setDiffuseColor(Color.WHITE);
        material.setSpecularColor(Color.TRANSPARENT);
        //
//            Bounds bounds2D = node2D.localToScene(node2D.getLayoutBounds());
        Bounds bounds2D = localetoRoot(node2D);
        //
        Bounds bounds3D = new BoundingBox(bounds2D.getMinX() * factor2d3d,
                bounds2D.getMinY() * factor2d3d,
                bounds2D.getWidth() * factor2d3d,
                bounds2D.getHeight() * factor2d3d);
        super.setDepth(thickness);
        super.setWidth(bounds3D.getWidth());
        super.setHeight(bounds3D.getHeight());
            //
//            node2d.layoutBoundsProperty().addListener((ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) -> {
//                Bounds bounds2D2 = node2d.localToScene(node2d.getLayoutBounds());
//                //
//                Bounds bounds3D2 = new BoundingBox(bounds2D2.getMinX() * factor2d3d,
//                        bounds2D2.getMinY() * factor2d3d,
//                        bounds2D2.getWidth() * factor2d3d,
//                        bounds2D2.getHeight() * factor2d3d);
//                super.setDepth(depth);
//                super.setWidth(bounds3D2.getWidth());
//                super.setHeight(bounds3D2.getHeight());
//                getTransforms().add(new Translate(bounds3D2.getMinX() + bounds3D2.getWidth() / 2,
//                        bounds3D2.getMinY() + bounds3D2.getHeight() / 2,
//                        0));
//            });
//            widthProperty().bind(node2d.layoutBoundsProperty());

        // Place object as 0,0 , that is curently in the middle of 3D universe
        getTransforms().add(new Translate(bounds3D.getMinX() + bounds3D.getWidth() / 2,
                bounds3D.getMinY() + bounds3D.getHeight() / 2,
                0));
        setMaterial(material);
        // TODO: if bound then the snapshot will work only once...
//            node2d.visibleProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
//                Tile3D.this.setVisible(newValue);
//            });
        //
        snapshot();
        //
//            node2d.addEventFilter(MouseEvent.MOUSE_CLICKED, (Event event) -> {
//                //System.err.println("event: " + event);
//                snapshot();
//            });
        //
        super.setOnMouseMoved((MouseEvent me) -> {
            String mouseOverTileText = node2D.getImpl().getClass().getSimpleName();
            iTile3DListener.onMouseMovedOnTile(mouseOverTileText);
        });
        //
        super.setOnMouseClicked((MouseEvent event) -> {
//                System.err.println("");
//                Point2D intersectedTexCoord = event.getPickResult().getIntersectedTexCoord();
//                double x = intersectedTexCoord.getX() / factor2d3d;
//                double y = intersectedTexCoord.getY() / factor2d3d;
//                System.err.println(" Object: " + node2D.getClass().getSimpleName());
//                System.err.println("x:" + x);
//                System.err.println("y:" + y);
            //    Point2D sceneToLocal = node2D.sceneToLocal(x, y);
            //MouseEvent mouseEvent = event.copyFor(event.getSource(), node2D);
            //   System.err.println("xl:" + sceneToLocal.getX());
            //   System.err.println("yl:" + sceneToLocal.getY());
            //  Event.fireEvent(node2D, mouseEvent);

            // Selection
            iTile3DListener.onMouseClickedOnTile(Tile3D.this);

//            if (iThreeDOM != null) {
//                iThreeDOM.click(node2D);
//            } else {
//                Platform.runLater(() -> {
//                    MouseEvent mouseEvent = new MouseEvent(node2D, node2D, MouseEvent.MOUSE_ENTERED, 8, 10, 0, 0, MouseButton.PRIMARY, 1, false, false, false, false, false, false, false, false, false, false, null);
//                    node2D.fireEvent(mouseEvent);
//                    mouseEvent = new MouseEvent(node2D, node2D, MouseEvent.MOUSE_PRESSED, 8, 10, 0, 0, MouseButton.PRIMARY, 1, false, false, false, false, false, false, false, false, false, false, null);
//                    node2D.fireEvent(mouseEvent);
//                    node2D.requestFocus();
//                    mouseEvent = new MouseEvent(node2D, node2D, MouseEvent.MOUSE_MOVED, 8, 10, 0, 0, MouseButton.PRIMARY, 1, false, false, false, false, false, false, false, false, false, false, null);
//                    node2D.fireEvent(mouseEvent);
//                    node2D.requestFocus();
//                    mouseEvent = new MouseEvent(node2D, node2D, MouseEvent.MOUSE_RELEASED, 8, 10, 0, 0, MouseButton.PRIMARY, 1, false, false, false, false, false, false, false, false, false, false, null);
//                    node2D.fireEvent(mouseEvent);
//                    mouseEvent = new MouseEvent(node2D, node2D, MouseEvent.MOUSE_CLICKED, 8, 10, 0, 0, MouseButton.PRIMARY, 1, false, false, false, false, false, false, false, false, false, false, null);
//                    node2D.fireEvent(mouseEvent);
//                });
//            }

        });

    }

    public Node getNode() {
        return node2d.getImpl();
    }

    public double get3Depth() {
        return depth;
    }

    public void snapshot() {
        Bounds layoutBounds = node2d.getImpl().getLayoutBounds();
        if (layoutBounds.getWidth() > 0 && layoutBounds.getHeight() > 0) {
            writableImage = new WritableImage((int) layoutBounds.getWidth(), (int) layoutBounds.getHeight());
            SnapshotParameters snapshotParameters = new SnapshotParameters();
            //Hide children
            if (node2d.getImpl() instanceof Parent) {
            //    super.setMouseTransparent(true); // why?
            //    snapshotParameters.setFill(Color.TRANSPARENT);    // Even parent can have css

                ObservableList<Node> childrenUnmodifiable = ((Parent) node2d.getImpl()).getChildrenUnmodifiable();
                childrenUnmodifiable.stream().forEach((c) -> {
                    if (!c.visibleProperty().isBound()) {
                        c.getProperties().put("VISIBLE-STATE", c.isVisible());  // Could also work with opacity?
                        c.setVisible(false);
                    } else {
                        System.err.println("Bound: " + c.toString());
                    }
                });
            } else {
      //          snapshotParameters.setFill(Color.WHITESMOKE);
            }
            node2d.getImpl().snapshot(snapshotParameters, writableImage);
//                node2d.snapshot((SnapshotResult p) -> {
            material.setDiffuseMap(writableImage);
            //
//                Color color = new Color(0.5, 0.5, 0.5, 0.5);
//                selectedImage = new WritableImage(writableImage.getPixelReader(), (int) writableImage.getWidth(), (int) writableImage.getHeight());
//                PixelWriter pixelWriter = selectedImage.getPixelWriter();
//                PixelReader pixelReader = selectedImage.getPixelReader();
//                for (int y = 0; y < writableImage.getHeight(); y++) {
//                    for (int x = 0; x < writableImage.getWidth(); x++) {
//                        pixelWriter.setColor(x, y, pixelReader.getColor(x, y).darker());
//                    }
//                }
            //                

//                if (node2d instanceof Parent) {
//                    material.diffuseColorProperty().bind(
//                            Bindings.when(checkBox.selectedProperty()).then(PARENT_COLOR).otherwise(Color.TRANSPARENT));
            //visibleProperty().bind(
            //       Bindings.when(checkBox.selectedProperty()).then(true).otherwise(false));
//                }
//                    return null;
//                }, new SnapshotParameters(), writableImage);
            // Show children
            if (node2d.getImpl() instanceof Parent) {
                ObservableList<Node> childrenUnmodifiable = ((Parent) node2d.getImpl()).getChildrenUnmodifiable();
                childrenUnmodifiable.stream().forEach((Node c) -> {
                    if (!c.visibleProperty().isBound()) {
                        Boolean v = (Boolean) c.getProperties().get("VISIBLE-STATE");
                        c.getProperties().remove("VISIBLE-STATE");
                        c.visibleProperty().set(v);
                    }
                });
            }
        }
    }
    public SVNode getSVNode(){
        return node2d;
    }
    Bounds localetoRoot(SVNode sv) {
        Node n = sv.getImpl();
        Bounds node = n.localToScene(n.getLayoutBounds());
        Bounds root = currentRoot2D.getImpl().localToScene(currentRoot2D.getImpl().getLayoutBounds());
        return new BoundingBox(node.getMinX() - root.getMinX(), node.getMinY() - root.getMinY(), node.getWidth(), node.getHeight());
    }
}
