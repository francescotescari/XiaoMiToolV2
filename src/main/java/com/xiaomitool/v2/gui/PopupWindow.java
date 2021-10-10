package com.xiaomitool.v2.gui;

import com.xiaomitool.v2.gui.controller.PopupController;
import com.xiaomitool.v2.gui.drawable.DrawableManager;
import com.xiaomitool.v2.gui.visual.OverlayPane;
import com.xiaomitool.v2.gui.visual.ToastPane;
import com.xiaomitool.v2.utility.Nullable;
import java.net.URL;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class PopupWindow {
  private double width, height;
  private Node content;
  private OverlayPane overlayPane;
  private ToastPane toastPane;
  private PopupController controller;

  public PopupWindow() {
    this(200, 100);
  }

  public PopupWindow(double width, double height) {
    this.width = width;
    this.height = height;
  }

  public Node getContent() {
    return content;
  }

  public void setContent(Node content) {
    this.content = content;
  }

  public double getWidth() {
    return width;
  }

  public void setWidth(double width) {
    this.width = width;
  }

  public double getHeight() {
    return height;
  }

  public void setHeight(double height) {
    this.height = height;
  }

  public OverlayPane getOverlayPane() {
    return this.overlayPane;
  }

  public void setOverlayPane(OverlayPane overlayPane) {
    this.overlayPane = overlayPane;
  }

  public ToastPane getToastPane() {
    if (toastPane == null) {
      OverlayPane overlayPane = getOverlayPane();
      if (overlayPane != null) {
        toastPane = new ToastPane(overlayPane);
      }
    }
    return toastPane;
  }

  public PopupController getController() {
    return this.controller;
  }

  public void setController(PopupController controller) {
    this.controller = controller;
  }

  public void waitForClose() throws InterruptedException {
    if (this.controller == null) {
      return;
    }
    controller.getWaitCloseSemaphre().waitOnce();
  }

  public enum Icon {
    INFO("info.png"),
    WARN("caution.png"),
    ERROR("error.png");
    private URL png;

    Icon(String png) {
      this.png = DrawableManager.getPng(png);
    }

    public String getPng() {
      return png.toString();
    }
  }

  public static class ImageTextPopup extends PopupWindow {
    private static final double IMG_HEIGHT = 40;

    public ImageTextPopup(String text, Icon icon) {
      this(text, icon, null, null);
    }

    public ImageTextPopup(
        String text, Icon icon, @Nullable Double fontSize, @Nullable Double width) {
      Text t = new Text(text);
      if (width == null || width == 0) {
        width = 300d;
      }
      if (fontSize == null || fontSize == 0) {
        fontSize = 15d;
      }
      t.setFont(Font.font(fontSize));
      double origWidth = t.getLayoutBounds().getWidth();
      if (origWidth > 0 && origWidth / width < 2) {
        width = origWidth;
      }
      t.setWrappingWidth(width);
      Bounds box = t.getLayoutBounds();
      if (4 * box.getHeight() > 3 * box.getWidth()) {
        width = Math.sqrt(box.getHeight() * box.getWidth() * 4 / 3);
        t.setWrappingWidth(width);
        width = t.getLayoutBounds().getWidth();
      }
      double height = t.getLayoutBounds().getHeight();
      if (height < IMG_HEIGHT + 20) {
        height = IMG_HEIGHT + 20;
      }
      ImageView imageView = new ImageView(new Image(icon.getPng()));
      imageView.setFitHeight(IMG_HEIGHT);
      imageView.setPreserveRatio(true);
      StackPane imgPane = new StackPane(imageView);
      HBox hBox = new HBox(imgPane, new StackPane(t));
      hBox.setSpacing(10);
      hBox.setAlignment(Pos.CENTER);
      width = width + 20 + 20 + IMG_HEIGHT;
      setHeight(height + 50);
      setWidth(width);
      setContent(hBox);
    }
  }
}
