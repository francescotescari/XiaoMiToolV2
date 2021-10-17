package com.xiaomitool.v2.gui;

import com.xiaomitool.v2.utility.MultiMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import javafx.scene.Node;

public class GuiObjects extends HashMap<String, Node> {
  public static final String LOGIN_LINK = "login_link";
  public static final String LOGIN_NUMBER = "login_number";
  public static final String TRANSLATED_LINK = "translated_link";
  public static final String IMG_CLOSE = "img_close";
  public static final String IMG_MINIFY = "img_minify";
  public static final String IMG_SETTINGS = "img_settings";
  private static final GuiObjects instance = new GuiObjects();

  public static Node getNode(String name) {
    synchronized (instance) {
      return instance.get(name);
    }
  }

  public static void set(String name, Node node) {
    List<Function<Node, Boolean>> callbacks;
    synchronized (instance) {
      instance.put(name, node);
      callbacks = ON_SET_CALLBACKS.get(name);
    }
    if (callbacks != null) {
      List<Function<Node, Boolean>> toRemove = new ArrayList<>();
      callbacks.forEach(
          fn -> {
            if (fn.apply(node)) {
              toRemove.add(fn);
            }
          });
      synchronized (instance) {
        toRemove.forEach(
            fn -> {
              ON_SET_CALLBACKS.removeSingle(name, fn);
            });
      }
    }
  }

  private static final MultiMap<String, Function<Node, Boolean>> ON_SET_CALLBACKS =
      new MultiMap<>();

  public static void setOnSetCallback(String name, Function<Node, Boolean> callback) {
    synchronized (instance) {
      ON_SET_CALLBACKS.putSingle(name, callback);
    }
  }

  public static void runOnReady(String name, Function<Node, Boolean> function) {
    Node node = getNode(name);
    if (node != null) {
      function.apply(node);
    } else {
      setOnSetCallback(name, function);
    }
  }
}
