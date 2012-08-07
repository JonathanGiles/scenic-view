package com.javafx.experiments.scenicview.details;

import java.util.*;

import javafx.animation.*;
import javafx.beans.value.*;
import javafx.collections.FXCollections;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

import com.javafx.experiments.scenicview.ScenicView;
import com.javafx.experiments.scenicview.connector.ConnectorUtils;
import com.javafx.experiments.scenicview.connector.details.*;
import com.javafx.experiments.scenicview.connector.details.Detail.EditionType;

public class GDetail {
    public Label label;
    public Label valueLabel;
    public Node valueNode;
    private final Node value;
    private Node field;
    Detail detail;

    private boolean isDefault = true;

    WritableValue<String> serializer;
    String reason = GDetailPane.STATUS_NOT_SUPPORTED;
    private String[] validItems;
    private double min;
    private double max;
    private String realValue;
    private EditionType editionType;
    private APILoader loader;

    public GDetail(final Label label, final Node value) {
        this.label = label;

        this.value = value;
        /**
         * Initial implementation...
         */
        final TextField field = new TextField();
        field.getStyleClass().add("detail-field");
        field.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent arg0) {
                if (serializer != null) {
                    serializer.setValue(field.getText());
                }
                recover();
            }
        });
        final ComboBox<String> options = new ComboBox<String>();
        options.getStyleClass().add("detail-field");
        options.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(final ObservableValue<? extends String> arg0, final String arg1, final String newValue) {
                if (newValue != null && !newValue.equals(realValue)) {
                    serializer.setValue(newValue.toString());
                    recover();
                }

            }
        });
        options.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override public void handle(final MouseEvent ev) {
                if (ev.isSecondaryButtonDown()) {
                    recover();
                }
            }
        });
        final Slider slider = new Slider();
        slider.getStyleClass().add("detail-field");
        slider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(final ObservableValue<? extends Number> arg0, final Number arg1, final Number newValue) {
                serializer.setValue(newValue.toString());
            }
        });
        value.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override public void handle(final MouseEvent arg0) {
                if (GDetailPane.activeDetail != null) {
                    GDetailPane.activeDetail.recover();
                }

                if (editionType != EditionType.NONE) {
                    switch (editionType) {
                    case COMBO:

                        options.setItems(FXCollections.observableArrayList(validItems));
                        options.getSelectionModel().select(realValue);
                        GDetail.this.field = options;
                        break;
                    case SLIDER:

                        slider.setMax(max);
                        slider.setMin(min);
                        slider.setValue(Double.parseDouble(realValue));
                        GDetail.this.field = slider;

                        break;
                    default:
                        field.setText(realValue);
                        GDetail.this.field = field;
                        break;

                    }
                    final Group group = (Group) value.getParent();
                    group.getChildren().clear();
                    group.getChildren().add(GDetail.this.field);
                    GDetail.this.field.requestFocus();
                    GDetailPane.activeDetail = GDetail.this;
                } else {
                    ScenicView.setStatusText(reason, 4000);
                }
            }

        });
        if (value instanceof Label) {
            this.valueLabel = (Label) value;
            valueLabel.setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override public void handle(final MouseEvent arg0) {
                    if (editionType != EditionType.NONE)
                        ScenicView.setStatusText("Properties which can be changed will show this icon");
                }
            });
            valueLabel.setOnMouseExited(new EventHandler<MouseEvent>() {
                @Override public void handle(final MouseEvent arg0) {
                    if (editionType != EditionType.NONE)
                        ScenicView.clearStatusText();
                }
            });
        } else {
            this.valueNode = value;
        }
    }

    public void setSerializer(final WritableValue<String> serializer) {
        this.serializer = serializer;
    }

    public void setIsDefault(final boolean isDefault) {
        this.isDefault = isDefault;

        if (label != null) {
            label.setOpacity(isDefault ? GDetailPane.FADE : 1.0);
        }
        if (valueLabel != null) {
            valueLabel.setOpacity(isDefault ? GDetailPane.FADE : 1.0);
        }
        if (valueNode != null) {
            valueNode.setOpacity(isDefault ? GDetailPane.FADE : 1.0);
        }

        final boolean showDetail = (!AllDetailsPane.showDefaultProperties && !isDefault) || AllDetailsPane.showDefaultProperties;

        if (label != null) {
            label.setVisible(showDetail);
            label.setManaged(showDetail);
        }

        if (valueLabel != null) {
            valueLabel.setVisible(showDetail);
            valueLabel.setManaged(showDetail);
        }

        if (valueNode != null) {
            valueNode.setVisible(showDetail);
            valueNode.setManaged(showDetail);
        }
    }

    public boolean isDefault() {
        return isDefault;
    }

    void recover() {
        final Group group = (Group) field.getParent();
        group.getChildren().clear();
        group.getChildren().add(value);
        GDetailPane.activeDetail = null;
    }

    public final void setReason(final String reason) {
        this.reason = reason;
    }

    public final void updated() {
        label.getStyleClass().remove(GDetailPane.DETAIL_LABEL_STYLE);
        label.getStyleClass().add("updated-detail-label");
        TimelineBuilder.create().keyFrames(new KeyFrame(Duration.millis(5000), new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent arg0) {
                label.getStyleClass().remove("updated-detail-label");
                label.getStyleClass().add(GDetailPane.DETAIL_LABEL_STYLE);
            }
        })).build().play();
    }

    public void setValidItems(final String[] validItems) {
        this.validItems = validItems;
    }

    public void setMinMax(final double min, final double max) {
        this.min = min;
        this.max = max;
    }

    public void setEditionType(final EditionType editionType) {
        this.editionType = editionType;
    }

    public void setRealValue(final String realValue) {
        this.realValue = realValue;
    }

    public void setDetail(final Detail detail) {
        this.detail = detail;
        if (detail.getDetailType() == DetailPaneType.FULL) {
            label.setOnMouseClicked(new EventHandler<MouseEvent>() {

                @Override public void handle(final MouseEvent ev) {
                    if (ev.getClickCount() == 2) {
                        loader.loadAPI(detail.getProperty());
                    }
                }
            });
        }
    }

    public void setValue(final String value2) {
        switch (detail.getValueType()) {
        case NORMAL:
            ((Label) value).setText(value2);
            break;

        case INSETS:
            ((InsetsDisplay) value).setInsetsTarget(ConnectorUtils.deserializeInsets(value2));
            break;

        case CONSTRAINTS:
            ((ConstraintsDisplay) value).setPropertiesMap(ConnectorUtils.deserializeMap(value2));
            break;

        case GRID_CONSTRAINTS:
            ((GridConstraintDisplay) value).setConstraints(detail.hasGridConstraints());
            final List<GridConstraintsDetail> constraints = detail.getGridConstraintsDetails();
            for (final Iterator<GridConstraintsDetail> iterator = constraints.iterator(); iterator.hasNext();) {
                final GridConstraintsDetail d = iterator.next();
                ((GridConstraintDisplay) value).addObject(d.getText(), d.getRowIndex(), d.getColIndex());
            }
            break;
        default:
            System.out.println("GDetail strange value for" + value2);
            break;
        }

    }

    public void setAPILoader(final APILoader loader) {
        this.loader = loader;
    }
}