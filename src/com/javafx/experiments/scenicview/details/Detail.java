package com.javafx.experiments.scenicview.details;

import javafx.animation.*;
import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.collections.FXCollections;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

import com.javafx.experiments.scenicview.*;
import com.javafx.experiments.scenicview.connector.details.*;
import com.javafx.experiments.scenicview.connector.details.SimpleSerializer.EditionType;

public class Detail {
    public Label label;
    public Label valueLabel;
    public Node valueNode;
    private final Node value;
    private Node field;

    private boolean isDefault = true;

    WritableValue<String> serializer;
    String reason = DetailPane.STATUS_NOT_SUPPORTED;

    public Detail(final Label label, final Node value) {
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
                if (newValue != null && !newValue.equals(serializer.getValue())) {
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
                if (DetailPane.activeDetail != null) {
                    DetailPane.activeDetail.recover();
                }
                if (serializer != null) {
                    // Probably this should be an interface...
                    if (serializer instanceof SimpleSerializer) {
                        final EditionType type = ((SimpleSerializer) serializer).getEditionType();
                        switch (type) {
                        case COMBO:
                            options.setItems(FXCollections.observableArrayList(((SimpleSerializer) serializer).getValidValues()));
                            options.getSelectionModel().select(serializer.getValue());
                            Detail.this.field = options;
                            break;
                        case SLIDER:
                            slider.setMax(((SimpleSerializer) serializer).getMaxValue());
                            slider.setMin(((SimpleSerializer) serializer).getMinValue());
                            slider.setValue(Double.parseDouble(((SimpleSerializer) serializer).getValue()));
                            Detail.this.field = slider;
                            break;
                        default:
                            field.setText(serializer.getValue());
                            Detail.this.field = field;
                            break;
                        }

                    } else {
                        field.setText(serializer.getValue());
                        Detail.this.field = field;
                    }
                    final Group group = (Group) value.getParent();
                    group.getChildren().clear();
                    group.getChildren().add(Detail.this.field);
                    Detail.this.field.requestFocus();
                    DetailPane.activeDetail = Detail.this;
                } else {
                    ScenicView.setStatusText(reason, 4000);
                }
            }
        });
        if (value instanceof Label) {
            this.valueLabel = (Label) value;
            valueLabel.setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override public void handle(final MouseEvent arg0) {
                    if (serializer != null)
                        ScenicView.setStatusText("Properties which can be changed will show this icon");
                }
            });
            valueLabel.setOnMouseExited(new EventHandler<MouseEvent>() {
                @Override public void handle(final MouseEvent arg0) {
                    if (serializer != null)
                        ScenicView.clearStatusText();
                }
            });
        } else {
            this.valueNode = value;
        }
    }

    @SuppressWarnings("rawtypes") public void setEnumProperty(final Property property, final Class<? extends Enum> enumClass) {
        setSimpleProperty(property, enumClass);
    }

    public void setSimpleProperty(@SuppressWarnings("rawtypes") final Property property) {
        setSimpleProperty(property, null);
    }

    private void setSimpleProperty(@SuppressWarnings("rawtypes") final Property property, @SuppressWarnings({ "rawtypes" }) final Class<? extends Enum> enumClass) {
        if (property != null) {
            if (property.isBound()) {
                unavailableEdition(DetailPane.STATUS_BOUND);
            } else {
                final SimpleSerializer s = new SimpleSerializer(property);
                s.setEnumClass(enumClass);
                setSerializer(s);
            }
        } else {
            unavailableEdition(DetailPane.STATUS_NOT_SUPPORTED);
        }
    }

    void unavailableEdition(final String reason) {
        setReason(reason);
        setSerializer(null);
    }

    public void setSimpleSizeProperty(final DoubleProperty x, final DoubleProperty y) {
        if (x != null) {
            if (x.isBound() && y.isBound()) {
                unavailableEdition(DetailPane.STATUS_BOUND);
            } else {
                setSerializer(new SizeSerializer(x, y));
            }
        } else {
            setReason(DetailPane.STATUS_NOT_SUPPORTED);
            setSerializer(null);
        }
    }

    public void setSerializer(final WritableValue<String> serializer) {
        this.serializer = serializer;
        if (serializer != null && valueLabel != null) {
            final ImageView graphic = new ImageView(DetailPane.EDIT_IMAGE);
            valueLabel.setGraphic(graphic);
        }

    }

    public void setIsDefault(final boolean isDefault) {
        this.isDefault = isDefault;

        if (label != null) {
            label.setOpacity(isDefault ? DetailPane.FADE : 1.0);
        }
        if (valueLabel != null) {
            valueLabel.setOpacity(isDefault ? DetailPane.FADE : 1.0);
        }
        if (valueNode != null) {
            valueNode.setOpacity(isDefault ? DetailPane.FADE : 1.0);
        }

        final boolean showDetail = (!DetailPane.showDefaultProperties && !isDefault) || DetailPane.showDefaultProperties;

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
        DetailPane.activeDetail = null;
    }

    public final void setReason(final String reason) {
        this.reason = reason;
    }

    public final void updated() {
        label.getStyleClass().remove(DetailPane.DETAIL_LABEL_STYLE);
        label.getStyleClass().add("updated-detail-label");
        TimelineBuilder.create().keyFrames(new KeyFrame(Duration.millis(5000), new EventHandler<ActionEvent>() {
            @Override public void handle(final ActionEvent arg0) {
                label.getStyleClass().remove("updated-detail-label");
                label.getStyleClass().add(DetailPane.DETAIL_LABEL_STYLE);
            }
        })).build().play();
    }
}