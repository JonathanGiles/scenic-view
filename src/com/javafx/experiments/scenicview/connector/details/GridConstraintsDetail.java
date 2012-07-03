package com.javafx.experiments.scenicview.connector.details;

import java.io.Serializable;

public class GridConstraintsDetail implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4944813187008963711L;
    private final String text;
    private final int colIndex;
    private final int rowIndex;

    public GridConstraintsDetail(final String text, final int colIndex, final int rowIndex) {
        this.text = text;
        this.colIndex = colIndex;
        this.rowIndex = rowIndex;
    }

    public String getText() {
        return text;
    }

    public int getColIndex() {
        return colIndex;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    @Override public String toString() {
        return "GridConstraintsDetail [text=" + text + ", colIndex=" + colIndex + ", rowIndex=" + rowIndex + "]";
    }
}
