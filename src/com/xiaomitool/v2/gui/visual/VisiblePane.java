package com.xiaomitool.v2.gui.visual;

import com.xiaomitool.v2.logging.Log;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.util.LinkedList;

public class VisiblePane {
    private Pane pane;
    private boolean keepStack = false;
    public VisiblePane(){
        this(null);
    }
    public VisiblePane(Pane pane){
        if (pane == null){
            pane = new StackPane();
        } else {
            children.addAll(pane.getChildren());
        }
        this.pane = pane;
    }
    private LinkedList<Node> children = new LinkedList<>();

    public void add(Node node){
        set(node,true);
    }
    public void addHidden(Node node){
        set(node,false);
    }
    public void saveStack(boolean b){
        Log.debug("Set saving stack: "+b);
        keepStack = b;
    }
    public void removeTop(){
        try {
            Node topChild = children.getLast();
            Log.debug(children);
            Log.debug(topChild);
            if (topChild == null){
                return;
            }


        children.removeLast();
        topChild.setVisible(false);
        pane.getChildren().remove(topChild);
        topChild = children.getLast();
        if (topChild != null){
            topChild.setVisible(true);
        } else {
            if (onEmpty != null){
                add(onEmpty);
            }
        }
        } catch (Throwable t){
            if (onEmpty != null){
                add(onEmpty);
            }
            return;
        }
    }

    public Pane getPane() {
        return pane;
    }

    private void set(Node node, boolean show){
        Log.debug("Adding node: "+node.toString());
        if (!keepStack && show){
            children.clear();
            pane.getChildren().clear();
        }
        if (!children.contains(node)){
            children.add(node);
            node.setPickOnBounds(false);
            pane.getChildren().add(node);
        }
        if (keepStack) {
            if (show) {
                for (Node n : children) {
                    n.setVisible(false);
                }
                node.setVisible(true);
            } else {
                node.setVisible(false);
            }
        }
    }

    public void remove(Node node){
        children.remove(node);
        pane.getChildren().remove(node);
    }
    public void clear(){
        children.clear();
        pane.getChildren().clear();
    }

    private Pane onEmpty;
    public void onEmpty(Pane pane) {
        this.onEmpty = onEmpty;
    }
}
