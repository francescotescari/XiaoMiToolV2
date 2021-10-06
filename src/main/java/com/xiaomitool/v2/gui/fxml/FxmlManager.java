package com.xiaomitool.v2.gui.fxml;

import java.net.URL;

public class FxmlManager {
  public static URL getFxml(String name) {
    if (!name.endsWith(".fxml")) {
      name += ".fxml";
    }
    return FxmlManager.class.getResource(name);
  }
}
