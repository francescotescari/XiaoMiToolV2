package com.xiaomitool.v2.gui.visual;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

public class SidePane extends HBox {
    private StackPane left,right;
    public SidePane(){
        build();
    }
    private  void build(){
        left = new StackPane();
        right = new StackPane();
        left.setPrefWidth(20000);
        right.setPrefWidth(20000);

        HBox.setHgrow(left,Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);
        super.getChildren().addAll(left, right);
        super.setAlignment(Pos.CENTER);
    }
    public void setLeft(Node node){
        ObservableList<Node> list = left.getChildren();
        list.clear();
        list.add(node);
    }
    public void setRight(Node node){
        ObservableList<Node> list = right.getChildren();
        list.clear();
        list.add(node);
    }

    public StackPane getLeftPane() {
        return left;
    }

    public StackPane getRightPane() {
        return right;
    }
}
