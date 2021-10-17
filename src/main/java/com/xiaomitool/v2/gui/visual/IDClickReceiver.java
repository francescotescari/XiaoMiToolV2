package com.xiaomitool.v2.gui.visual;

import com.xiaomitool.v2.utility.MessageReceiver;
import com.xiaomitool.v2.utility.RunnableWithArg;
import com.xiaomitool.v2.utility.WaitSemaphore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.scene.Node;

public class IDClickReceiver implements MessageReceiver {
  private final List<RunnableWithArg> listeners = Collections.synchronizedList(new ArrayList<>());
  private ArrayList<Node> buttons = new ArrayList<>();
  private int lastClickID = -1;
  private WaitSemaphore semaphore = new WaitSemaphore();
  private int currentID = 0;

  public void addListener(RunnableWithArg args) {
    synchronized (listeners) {
      this.listeners.add(args);
    }
  }

  public int addNode(Node node) {
    return addIDButton(node);
  }

  public int addNodes(Node... nodes) {
    int id = -1;
    for (Node node : nodes) {
      id = addNode(node);
    }
    return id;
  }

  private int addIDButton(Node node) {
    IDNode button = new IDNode();
    button.node = node;
    button.id = currentID;
    buttons.add(node);
    ++currentID;
    node.setOnMouseClicked(
        event -> {
          message(button.id);
        });
    return button.id;
  }

  public int waitClick() throws InterruptedException {
    semaphore.setPermits(0);
    semaphore.waitOnce();
    return lastClickID;
  }

  public void message(int message) {
    synchronized (listeners) {
      for (RunnableWithArg runnable : listeners) {
        runnable.run(message);
      }
    }
    lastClickID = message;
    semaphore.increase();
  }

  public boolean removeListener(RunnableWithArg arg) {
    return this.listeners.remove(arg);
  }

  private static class IDNode {
    public int id;
    public Node node;
  }
}
