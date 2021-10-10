package com.xiaomitool.v2.gui.visual;

import javafx.scene.layout.Pane;

public class OverlayPane extends Pane {
  public OverlayPane() {
    super.setPickOnBounds(false);
    super.setMouseTransparent(true);
  }
}
