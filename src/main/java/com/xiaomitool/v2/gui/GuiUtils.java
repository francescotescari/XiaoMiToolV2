package com.xiaomitool.v2.gui;

import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.utility.NotNull;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;

public class GuiUtils {
  private static ExecutorService guiWaitThread = Executors.newSingleThreadExecutor();

  public static Background backgroundFromColor(Paint paint) {
    return new Background(new BackgroundFill(paint, null, null));
  }

  public static void init() {
    Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
  }

  public static void setViewportChange(ImageView image, GetViewport func) {
    image.setOnMouseEntered(
        event -> {
          if (!func.isPressed) {
            image.setViewport(func.get(1));
          }
          func.isHover = true;
        });
    image.setOnMouseExited(
        event -> {
          if (!func.isPressed) {
            image.setViewport(func.get(0));
          }
          func.isHover = false;
        });
    image.setOnMousePressed(
        event -> {
          image.setViewport(func.get(2));
          func.isPressed = true;
        });
    image.setOnMouseReleased(
        event -> {
          image.setViewport(func.get(func.isHover ? 1 : 0));
          func.isPressed = false;
        });
  }

  public static Pane center(Node node) {
    HBox hBox = new HBox(node);
    VBox vBox = new VBox(hBox);
    vBox.setAlignment(Pos.CENTER);
    hBox.setAlignment(Pos.CENTER);
    return vBox;
  }

  public static void waitGui(long millis, Runnable runnable) {
    Runnable runnable1 =
        new Runnable() {
          @Override
          public void run() {
            try {
              Thread.sleep(millis);
            } catch (InterruptedException e) {
              Log.warn("Thread interrupted :(");
            }
            Platform.runLater(runnable);
          }
        };
    guiWaitThread.submit(runnable1);
  }

  public static void tooltip(Node node, LRes key) {
    tooltip(node, key.toString());
  }

  public static void tooltip(Node node, String text) {
    Tooltip.install(node, new Tooltip(text));
  }

  public static Pane debug(Pane pane) {
    debug(pane, Color.ALICEBLUE);
    return pane;
  }

  public static Pane debug(Pane pane, Color color) {
    pane.setBackground(backgroundFromColor(color));
    return pane;
  }

  public static Pane crop(Pane pane, double x, double y, double width, double height) {
    pane.setLayoutY(-1 * y);
    pane.setLayoutX(-1 * x);
    Pane pane1 = new Pane(pane);
    pane1.setClip(new Rectangle(width, height));
    pane1.setPrefSize(width, height);
    pane1.setMaxSize(width, height);
    return pane1;
  }

  public static boolean hasParent(Node node, Parent parent) {
    Parent p = node.getParent();
    while (p != null) {
      if (p == parent) {
        return true;
      }
      p = p.getParent();
    }
    return false;
  }

  public static double distanceBetween(@NotNull Node node1, @NotNull Node node2) {
    if (node2 == null) {
      return -1;
    }
    double[] center = getCenterCoordinates(node2);
    if (center == null) {
      return -1;
    }
    return distanceBetween(node1, center[0], center[1]);
  }

  public static double distanceBetween(@NotNull Node node1, double x, double y) {
    if (node1 == null) {
      return -1;
    }
    double[] center = getCenterCoordinates(node1);
    if (center == null) {
      return -1;
    }
    return distanceBetween(center[0], center[1], x, y);
  }

  public static double distanceBetween(double x1, double y1, double x2, double y2) {
    return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
  }

  public static double[] getCenterCoordinates(@NotNull Node node) {
    if (node == null || node.getBoundsInLocal() == null) {
      return null;
    }
    Bounds sceneBounds = node.localToScene(node.getBoundsInLocal());
    double centerX1 = (sceneBounds.getMinX() + sceneBounds.getMaxX()) / 2,
        centerY1 = (sceneBounds.getMinY() + sceneBounds.getMaxY()) / 2;
    if (centerX1 <= 0 || centerY1 <= 0) {
      return null;
    }
    return new double[] {centerX1, centerY1};
  }

  public static void specialComboBox(ComboBox<String> combo, LRes closedFormat, int fontSize) {
    combo.setButtonCell(
        new ListCell<String>() {
          @Override
          public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            setFont(Font.font(this.getFont().getName(), fontSize));
            setAlignment(Pos.CENTER);
            if (item != null) {
              setText(closedFormat.toString(item));
            }
          }
        });
    combo.setCellFactory(
        new Callback<ListView<String>, ListCell<String>>() {
          @Override
          public ListCell<String> call(ListView<String> param) {
            return new ListCell<String>() {
              @Override
              public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                  setText(item);
                  setFont(Font.font(this.getFont().getName(), fontSize));
                  setTextAlignment(TextAlignment.CENTER);
                  setAlignment(Pos.CENTER);
                } else {
                  setText(null);
                }
              }
            };
          }
        });
  }

  public abstract static class GetViewport {
    public boolean isPressed = false, isHover = false;

    public abstract Rectangle2D get(int index);
  }
}
