package com.xiaomitool.v2.gui.visual;

import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.utility.MessageReceiver;
import com.xiaomitool.v2.utility.WaitSemaphore;
import javafx.scene.Node;

import java.util.ArrayList;

public class IDClickReceiver implements MessageReceiver {

    private static class IDNode {
        public int id;
        public Node node;
    }
    private ArrayList<Node> buttons = new ArrayList<>();
    private int lastClickID = -1;
    private WaitSemaphore semaphore = new WaitSemaphore();
    private int currentID = 0;


    public int addNode(Node node){
        return addIDButton(node);

    }
    public int addNodes(Node ... nodes){
        int id = -1;
        for (Node node : nodes){
            id = addNode(node);
        }
        return id;
    }

    private int addIDButton(Node node){
        IDNode button = new IDNode();
        button.node = node;
        button.id = currentID;
        buttons.add(node);
        ++currentID;
                node.setOnMouseClicked(event -> {
                    Log.debug("Button pressed: "+ button.id);
                    message(button.id);
                });
                return button.id;
    }
    public int waitClick() throws InterruptedException {
        semaphore.setPermits(0);
        semaphore.waitOnce();
        return lastClickID;
    }
    public void message(int message){
        lastClickID =message;
        semaphore.increase();
    }


}
