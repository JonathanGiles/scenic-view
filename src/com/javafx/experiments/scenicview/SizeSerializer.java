package com.javafx.experiments.scenicview;

import javafx.beans.property.DoubleProperty;
import javafx.beans.value.WritableValue;

public class SizeSerializer implements WritableValue<String> {

    private final DoubleProperty x;
    private final DoubleProperty y;

    public SizeSerializer(final DoubleProperty x, final DoubleProperty y) {
        this.x = x;
        this.y = y;
    }

    @Override public String getValue() {
        return x.getValue() + " x " + y.getValue();
    }

    @Override public void setValue(final String value) {
        try {
            final int pos = value.indexOf(" x ");
            if (pos == -1) {
                throw new IllegalArgumentException("Invalid size format should be xValue x yValue");
            }
            final String x = value.substring(0, pos);
            final String y = value.substring(pos + 3);
            final double xValue = Double.parseDouble(x);
            final double yValue = Double.parseDouble(y);
            if ((this.x.isBound() && this.x.get() != xValue) || (this.y.isBound() && this.y.get() != yValue)) {
                throw new IllegalArgumentException("Bound value cannot be changed");
            }
            if (!this.x.isBound())
                this.x.set(xValue);
            if (!this.y.isBound())
                this.y.set(yValue);
        } catch (final Exception e) {
            ScenicView.setStatusText(DetailPane.STATUS_EXCEPTION + e.getMessage(), 10000);
            e.printStackTrace();
        }
    }

}
