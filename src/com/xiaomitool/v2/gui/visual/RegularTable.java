package com.xiaomitool.v2.gui.visual;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.*;

public class RegularTable extends GridPane {
    public RegularTable(int rows, int columns, double height, double width){
        super();
        double rowHeight = height/rows;
        double columnWidth = width/columns;
        super.setPrefSize(width,height);
        RowConstraints[] rowConstraints = new RowConstraints[rows];
        RowConstraints c = new RowConstraints(rowHeight);
        c.setVgrow(Priority.ALWAYS);
        for (int i = 0; i<rows; ++i){
            rowConstraints[i] = c;
        }
        super.getRowConstraints().addAll(rowConstraints);
        ColumnConstraints[] columnConstraints = new ColumnConstraints[columns];
        ColumnConstraints c1 = new ColumnConstraints(columnWidth);
        c1.setHgrow(Priority.ALWAYS);
        for (int i = 0; i<columns; ++i){
            columnConstraints[i] = c1;
        }
        super.getColumnConstraints().addAll(columnConstraints);
    }
    @Override
    public void add(Node node, int columnIndex, int rowIndex){
        add(node,columnIndex,rowIndex,Pos.CENTER);
    }
    public void add(Node node, int columnIndex, int rowIndex,Pos position){
        StackPane stackPane = new StackPane(node);
        stackPane.setAlignment(position);
        super.add(stackPane,columnIndex,rowIndex);
    }
}