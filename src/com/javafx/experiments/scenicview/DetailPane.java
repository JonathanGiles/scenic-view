/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.javafx.experiments.scenicview;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.*;

import javafx.beans.*;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

/**
 * 
 * @author aim
 */
public abstract class DetailPane extends TitledPane {

    private static final int LABEL_COLUMN = 0;
    private static final int VALUE_COLUMN = 1;

    private static final String STATUS_NOT_SET = "Value can not be changed ";
    public static final String STATUS_NOT_SUPPORTED = STATUS_NOT_SET + "(Not supported yet)";
    protected static final String STATUS_BOUND = STATUS_NOT_SET + "(Bound property)";
    public static final String STATUS_EXCEPTION = STATUS_NOT_SET + "an exception has ocurred:";
    protected static final String STATUS_READ_ONLY = STATUS_NOT_SET + "(Read-Only property)";

    public static float FADE = .50f;
    public static DecimalFormat f = new DecimalFormat("0.0#");

    private Object target;
    private final InvalidationListener propListener;
    @SuppressWarnings("rawtypes") Map<ObservableValue, String> properties = new HashMap<ObservableValue, String>();

    static final Image EDIT_IMAGE = new Image(Detail.class.getResource("images/ui/editclear.png").toString());

    static final String DETAIL_LABEL_STYLE = "detail-label";
    static Detail activeDetail;

    GridPane gridpane;
    List<Node> paneNodes = new ArrayList<Node>();

    public DetailPane() {
        getStyleClass().add("detail-pane");
        setManaged(false);
        setVisible(false);
        setExpanded(false);
        setMaxWidth(Double.MAX_VALUE);
        propListener = new InvalidationListener() {
            @Override public void invalidated(final Observable arg0) {
                updateDetail(properties.get(arg0));
            }
        };
        setId("title-label");
        setAlignment(Pos.CENTER_LEFT);
        setText(getPaneName());

        createGridPane();
        createDetails();
        updateAllDetails();
    }

    private void createGridPane() {
        gridpane = new GridPane();
        gridpane.getStyleClass().add("detail-grid");
        gridpane.setOnMousePressed(new EventHandler<MouseEvent>() {

            @Override public void handle(final MouseEvent arg0) {
                if (activeDetail != null)
                    activeDetail.recover();
            }
        });
        gridpane.setHgap(4);
        gridpane.setVgap(2);
        gridpane.setSnapToPixel(true);
        final ColumnConstraints colInfo = new ColumnConstraints(180);
        gridpane.getColumnConstraints().addAll(colInfo, new ColumnConstraints());
        setContent(gridpane);
    }

    protected String getPaneName() {
        return getTargetClass().getSimpleName() + " Details";
    }

    public abstract Class<? extends Node> getTargetClass();

    public abstract boolean targetMatches(Object candidate);

    public void setTarget(final Object value) {
        if (doSetTarget(value)) {
            updateAllDetails();
        }
    }

    @SuppressWarnings("rawtypes") protected boolean doSetTarget(final Object value) {
        if (target == value)
            return false;

        final Object old = target;
        if (old != null) {
            for (final ObservableValue ov : properties.keySet()) {
                ov.removeListener(propListener);
            }
            properties.clear();
        }
        target = value;
        if (target != null) {
            properties.clear();
            // Using reflection, locate all properties and their corresponding
            // property references
            for (final Method method : target.getClass().getMethods()) {
                if (method.getName().endsWith("Property")) {
                    try {
                        final Class returnType = method.getReturnType();
                        if (ObservableValue.class.isAssignableFrom(returnType)) {
                            // we've got a winner
                            final String propertyName = method.getName().substring(0, method.getName().lastIndexOf("Property"));
                            // Request access
                            method.setAccessible(true);
                            final ObservableValue property = (ObservableValue) method.invoke(target);
                            // System.out.println("propertyName="+propertyName+".");
                            properties.put(property, propertyName);
                        }
                    } catch (final Exception e) {
                        System.err.println("Failed to get property " + method.getName());
                        e.printStackTrace();
                    }
                }
            }

            for (final ObservableValue ov : properties.keySet()) {
                ov.addListener(propListener);
            }
        }
        return true;
    }

    public Object getTarget() {
        return target;
    }

    protected abstract void createDetails();

    protected abstract void updateAllDetails();

    protected abstract void updateDetail(String propertyName);

    protected Detail addDetail(final String propertyName, final String labelText, final int row) {
        return addDetail(propertyName, labelText, new Label(), row);
    }

    protected Detail addDetail(final String propertyName, final String labelText, final Node valueNode, final int row) {
        return addDetail(propertyName, labelText, null, valueNode, row);
    }

    protected Detail addDetail(final String propertyName, final String labelText, final Node labelGraphic, final Node valueNode, final int row) {
        final Label label = new Label(labelText);
        if (labelGraphic != null) {
            label.setGraphic(labelGraphic);
            label.setContentDisplay(ContentDisplay.LEFT);
        }
        final Detail detail = new Detail(label, valueNode);
        GridPane.setConstraints(detail.label, LABEL_COLUMN, row);
        GridPane.setHalignment(detail.label, HPos.RIGHT);
        GridPane.setValignment(detail.label, VPos.TOP);
        detail.label.getStyleClass().add(DETAIL_LABEL_STYLE);

        if (valueNode instanceof Label) {
            final Group group = new Group(detail.valueLabel);
            GridPane.setConstraints(group, VALUE_COLUMN, row);
            GridPane.setHalignment(group, HPos.LEFT);
            GridPane.setValignment(group, VPos.TOP);
            detail.valueLabel.getStyleClass().add("detail-value");
            addToPane(detail.label, group);
        } else {
            // icky, but fine for now
            final Group group = new Group(detail.valueNode);
            GridPane.setConstraints(group, VALUE_COLUMN, row);
            GridPane.setHalignment(group, HPos.LEFT);
            GridPane.setValignment(group, VPos.TOP);
            addToPane(detail.label, group);
        }
        
        return detail;
    }

    protected void clearPane() {
        gridpane.getChildren().clear();
        paneNodes.clear();
    }

    protected void addToPane(final Node... nodes) {
        gridpane.getChildren().addAll(nodes);
        paneNodes.addAll(Arrays.asList(nodes));
    }

    static boolean showDefaultProperties = true;

    public void setShowDefaultProperties(final boolean show) {
        showDefaultProperties = show;
        updateAllDetails();
    }

    @Override protected double computeMinWidth(final double height) {
        return prefWidth(height);
    }

    @Override protected double computeMinHeight(final double width) {
        return prefHeight(width);
    }

    private String currentFilter = null;
    public void filterProperties(final String text) {
        if (currentFilter != null && currentFilter.equals(text)) {
            return;
        }
        currentFilter = text;
        
        /**
         * Make this more clean
         */
        gridpane.getChildren().clear();
        final List<Node> nodes = paneNodes;
        int row = 0;
        for (int i = 0; i < nodes.size(); i++) {

            final Label label = (Label) nodes.get(i++);
            boolean valid = text.equals("") || label.getText().toLowerCase().indexOf(text.toLowerCase()) != -1;
            final Group g = (Group) nodes.get(i);
            final Node value = g.getChildren().get(0);
            
            if (!valid && value instanceof Label) {
                valid |= ((Label) value).getText().toLowerCase().indexOf(text.toLowerCase()) != -1;
            }
            
            if (valid) {
                GridPane.setConstraints(label, LABEL_COLUMN, row);
                GridPane.setConstraints(g, VALUE_COLUMN, row);
                gridpane.getChildren().addAll(label, g);
                row++;
            }
        }
    }

    public void setShowCSSProperties(final boolean show) {

    }
}
