package com.xiaomitool.v2.gui.controller;

import com.xiaomitool.v2.gui.PopupWindow;
import com.xiaomitool.v2.gui.visual.OverlayPane;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class PopupController extends DefaultController {
  @FXML Region HEADER_REGION;
  @FXML ImageView IMG_CLOSE;
  @FXML HBox HEADER;
  @FXML StackPane CONTENT_PANE;
  private PopupWindow popupWindow;

  public PopupController(PopupWindow popupWindow) {
    this.popupWindow = popupWindow;
    popupWindow.setController(this);
  }

  @Override
  protected void initialize() {
    initHeaderDrag(HEADER);
    setCloseImage(IMG_CLOSE);
    double w = popupWindow.getWidth() - 150;
    if (w > 0) {
      HEADER_REGION.setPrefWidth(w);
    }
    CONTENT_PANE.setPrefSize(popupWindow.getWidth(), popupWindow.getHeight());
    OverlayPane overlayPane = new OverlayPane();
    popupWindow.setOverlayPane(overlayPane);
    if (popupWindow.getContent() != null) {
      CONTENT_PANE.getChildren().add(popupWindow.getContent());
    }
    CONTENT_PANE.getChildren().add(overlayPane);
  }
}
