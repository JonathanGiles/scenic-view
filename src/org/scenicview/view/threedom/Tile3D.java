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

import java.util.ArrayList;
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

    private final PhongMaterial material;
    public WritableImage selectedImage;
    public WritableImage writableImage;
    SVNode node2d;
    double depth;
    SVNode currentRoot2D;
    double factor2d3d;
    ITile3DListener iTile3DListener;
    IThreeDOM iThreeDOM;
    ArrayList<Tile3D> children;

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

        Bounds bounds2D = localetoRoot(node2D);
        //
        Bounds bounds3D = new BoundingBox(bounds2D.getMinX() * factor2d3d,
                bounds2D.getMinY() * factor2d3d,
                bounds2D.getWidth() * factor2d3d,
                bounds2D.getHeight() * factor2d3d);
        super.setDepth(thickness);
        super.setWidth(bounds3D.getWidth());
        super.setHeight(bounds3D.getHeight());

        // Place object as 0,0 that is curently in the middle of 3D universe
        getTransforms().add(new Translate(bounds3D.getMinX() + bounds3D.getWidth() / 2,
                bounds3D.getMinY() + bounds3D.getHeight() / 2,
                0));
        setMaterial(material);

        snapshot();

        super.setOnMouseMoved((MouseEvent me) -> {
            String mouseOverTileText = node2D.getImpl().getClass().getSimpleName();
            iTile3DListener.onMouseMovedOnTile(mouseOverTileText);
        });
//        super.setOnMouseClicked((MouseEvent event) -> {
        super.setOnMousePressed((MouseEvent event) -> {
           
            // Selection
            iTile3DListener.onMouseClickedOnTile(Tile3D.this);
             if(event.isSecondaryButtonDown()){
                  iTile3DListener.onMouseRightClickedOnTile(event);
            }
        });

    }

    public Node getNode() {
        return node2d.getImpl();
    }

    public double get3Depth() {
        return depth;
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public final void snapshot() {
        Bounds layoutBounds = node2d.getImpl().getLayoutBounds();
        if (layoutBounds.getWidth() > 0 && layoutBounds.getHeight() > 0) {
            writableImage = new WritableImage((int) layoutBounds.getWidth(), (int) layoutBounds.getHeight());
            SnapshotParameters snapshotParameters = new SnapshotParameters();

            // Hide children
            if (node2d.getImpl() instanceof Parent) {
                ObservableList<Node> childrenUnmodifiable = ((Parent) node2d.getImpl()).getChildrenUnmodifiable();
                childrenUnmodifiable.stream().forEach((c) -> {
                    if (!c.visibleProperty().isBound()) {
                        c.getProperties().put("VISIBLE-STATE", c.isVisible());
                        c.setVisible(false);
                    } else {
                        System.err.println("Bound: " + c.toString());
                    }
                });
            }
            try {
                node2d.getImpl().snapshot(snapshotParameters, writableImage);
                material.setDiffuseMap(writableImage);
            } catch (Throwable t) {
                // Sometimes the snapshot hangs (e.g. webview)
                t.printStackTrace();
            }
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

    public SVNode getSVNode() {
        return node2d;
    }

    private Bounds localetoRoot(SVNode sv) {
        Node n = sv.getImpl();
        Bounds node = n.localToScene(n.getLayoutBounds());
        Bounds root = currentRoot2D.getImpl().localToScene(currentRoot2D.getImpl().getLayoutBounds());
        return new BoundingBox(node.getMinX() - root.getMinX(), node.getMinY() - root.getMinY(), node.getWidth(), node.getHeight());
    }

    public void addChildrenTile(Tile3D child) {
        if (children == null) {
            children = new ArrayList(5);
        }
        children.add(child);
    }

    public ArrayList<Tile3D> getChildrenTile() {
        return children;
    }
}
