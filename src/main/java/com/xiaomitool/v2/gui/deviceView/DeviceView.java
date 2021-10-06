package com.xiaomitool.v2.gui.deviceView;

import com.mortennobel.imagescaling.MultiStepRescaleOp;
import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;
import com.xiaomitool.v2.gui.GuiUtils;
import com.xiaomitool.v2.gui.drawable.DrawableManager;
import com.xiaomitool.v2.gui.visual.OverlayPane;
import com.xiaomitool.v2.utility.Nullable;
import com.xiaomitool.v2.utility.Pointer;
import com.xiaomitool.v2.utility.utils.NumberUtils;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class DeviceView extends StackPane {
  public static final DeviceImage DEVICE_16_9 =
      new DeviceImage(
          new Image(DrawableManager.getPng("device16_9").toString()),
          220,
          35,
          1920,
          1080,
          2360,
          1160,
          new DeviceView.ButtonPosition(410, 1150, 10, 200),
          new DeviceView.ButtonPosition(610, 1150, 10, 200),
          new DeviceView.ButtonPosition(880, 1150, 10, 200));
  public static final DeviceImage DEVICE_18_9 =
      new DeviceImage(
          new Image(DrawableManager.getPng("device18_9").toString()),
          130,
          25,
          2160,
          1080,
          2420,
          1140,
          new DeviceView.ButtonPosition(300, 1130, 10, 200),
          new DeviceView.ButtonPosition(500, 1130, 10, 200),
          new DeviceView.ButtonPosition(800, 1130, 10, 200));
  private final ConcurrentLinkedQueue<Transition> circlesAnimation = new ConcurrentLinkedQueue<>();
  protected DeviceImage deviceImage;
  protected double wantedHeight, scaleRatio;
  protected Pane containerPane, imageWrapPane;
  protected double contentScaleRatio;
  protected double imageOffsetX, imageOffsetY;
  protected boolean keepRatio;
  private ImageView deviceBorders, displayedImageView;
  private Color background = Color.rgb(49, 53, 57), innerShadow = Color.BLACK;
  private StackPane contentPane;
  private Image displayedImage;

  public DeviceView(DeviceImage deviceImage, double wantedHeight) {
    this(deviceImage, wantedHeight, null, null);
  }

  public DeviceView(
      DeviceImage deviceImage,
      double wantedHeight,
      @Nullable Color backgroundColor,
      @Nullable Color innerShadowColor) {
    if (backgroundColor != null) {
      this.background = backgroundColor;
    }
    if (innerShadowColor != null) {
      this.innerShadow = innerShadowColor;
    }
    this.deviceImage = deviceImage;
    this.wantedHeight = wantedHeight;
    super.setPrefHeight(wantedHeight);
    build();
  }

  public static Pane crop(DeviceView deviceView, double height, double top) {
    return GuiUtils.crop(
        deviceView,
        0,
        top,
        deviceView.getWantedHeight() / deviceView.getOuterAspectRatio(),
        height);
  }

  public static Pane crop(DeviceView deviceView, double height) {
    return crop(deviceView, height, 0);
  }

  public double getWantedHeight() {
    return wantedHeight;
  }

  private void build() {
    deviceBorders = new ImageView(deviceImage.getDeviceImage());
    deviceBorders.setPreserveRatio(true);
    deviceBorders.setFitHeight(wantedHeight);
    scaleRatio = wantedHeight / deviceImage.getOuterHeight();
    contentPane = new StackPane();
    contentPane.setBackground(GuiUtils.backgroundFromColor(background));
    contentPane.setPrefHeight((scaleRatio * deviceImage.getInnerHeight()) + 4);
    contentPane.setPrefWidth((scaleRatio * deviceImage.getInnerWidth()) + 4);
    contentPane.setEffect(
        new InnerShadow(0.1 * deviceImage.getInnerWidth() * scaleRatio, innerShadow));
    contentPane.setLayoutX(deviceImage.getLeftOffset() * scaleRatio - 2);
    contentPane.setLayoutY(deviceImage.getTopOffset() * scaleRatio - 2);
    contentPane.setPadding(new Insets(2, 0, 0, 2));
    Pane paddingPane = new Pane(contentPane);
    StackPane superStack = new StackPane(paddingPane, deviceBorders);
    containerPane = new Pane(superStack);
    containerPane.setPrefHeight(wantedHeight + 4);
    super.getChildren().add(containerPane);
  }

  public void setContent(ImageView image) {
    setContent(image, false);
  }

  public void setContent(Image image) {
    setContent(new ImageView(image));
  }

  public void setContent(Image image, boolean keepratio) {
    setContent(new ImageView(image), keepratio);
  }

  public void setContent(URL url) {
    setContent(new Image(url.toString()));
  }

  public void setContent(URL url, boolean keepratio) {
    setContent(new Image(url.toString(), false), keepratio);
  }

  public double getOuterAspectRatio() {
    return deviceImage.getOuterHeight() / deviceImage.getOuterWidth();
  }

  public double getInnerAspectRatio() {
    return deviceImage.getInnerHeight() / deviceImage.getInnerWidth();
  }

  private double getInnerHeight() {
    return this.deviceImage.getInnerHeight() * this.scaleRatio;
  }

  private double getInnerWidth() {
    return this.deviceImage.getInnerWidth() * this.scaleRatio;
  }

  public void setContent(Color color) {
    java.awt.Color cCol =
        new java.awt.Color(
            (float) color.getRed(),
            (float) color.getGreen(),
            (float) color.getBlue(),
            (float) color.getOpacity());
    BufferedImage image =
        new BufferedImage(
            (int) deviceImage.getInnerWidth(),
            (int) deviceImage.getInnerHeight(),
            BufferedImage.TYPE_INT_ARGB);
    Graphics2D graph = image.createGraphics();
    graph.setPaint(cCol);
    graph.fillRect(0, 0, image.getWidth(), image.getHeight());
    setContent(SwingFXUtils.toFXImage(image, null));
  }

  public void setContent(ImageView image, boolean keepRatio) {
    this.keepRatio = keepRatio;
    displayedImage = image.getImage();
    double origImgRatio = displayedImage.getHeight() / displayedImage.getWidth();
    int resizeWidth, resizeHeight;
    Rectangle2D viewport;
    if (origImgRatio < this.getInnerAspectRatio()) {
      resizeWidth = NumberUtils.double2int(this.getInnerWidth());
      resizeHeight =
          NumberUtils.double2int(keepRatio ? resizeWidth * origImgRatio : this.getInnerHeight());
      imageOffsetY = keepRatio ? (this.getInnerHeight() - resizeHeight) / 2 : 0;
      imageOffsetX = 0;
      viewport =
          keepRatio
              ? new Rectangle2D(0, -1 * imageOffsetY, this.getInnerWidth(), this.getInnerHeight())
              : null;
    } else {
      resizeHeight = NumberUtils.double2int(this.getInnerHeight());
      resizeWidth =
          NumberUtils.double2int(keepRatio ? resizeHeight / origImgRatio : this.getInnerWidth());
      imageOffsetX = keepRatio ? (this.getInnerWidth() - resizeWidth) / 2 : 0;
      imageOffsetY = 0;
      viewport =
          keepRatio
              ? new Rectangle2D(-1 * imageOffsetX, 0, this.getInnerWidth(), this.getInnerHeight())
              : null;
    }
    contentScaleRatio = ((double) resizeHeight) / displayedImage.getHeight();
    BufferedImage srcImg = SwingFXUtils.fromFXImage(image.getImage(), null);
    BufferedImage img = new BufferedImage(resizeWidth, resizeHeight, srcImg.getType());
    if (true) {
      MultiStepRescaleOp resampleOp =
          new MultiStepRescaleOp(
              resizeWidth, resizeHeight, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      resampleOp.doFilter(srcImg, img, resizeWidth, resizeHeight);
    } else {
      ResampleOp resampleOp = new ResampleOp(resizeWidth, resizeHeight);
      resampleOp.setFilter(ResampleFilters.getBiCubicFilter());
      resampleOp.doFilter(srcImg, img, resizeWidth, resizeHeight);
    }
    Image resizedImage = SwingFXUtils.toFXImage(img, null);
    image.setImage(resizedImage);
    if (viewport != null) {
      image.setViewport(viewport);
    }
    image.setFitWidth(NumberUtils.double2int(this.getInnerWidth()));
    image.setFitHeight(NumberUtils.double2int(this.getInnerHeight()));
    contentPane.getChildren().clear();
    displayedImageView = image;
    imageWrapPane = new Pane(image);
    contentPane.getChildren().clear();
    contentPane.getChildren().add(GuiUtils.center(imageWrapPane));
  }

  public void setLenOverlay(OverlayPane pane, double size, double lensZoomRatio) {
    if (displayedImageView == null) {
      return;
    }
    Pane len = new Pane();
    len.setPrefSize(size + 2, size + 2);
    len.setPickOnBounds(false);
    len.setLayoutX(-size);
    len.setLayoutY(-size);
    len.setBorder(
        new Border(
            new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, new BorderWidths(1))));
    len.setVisible(false);
    ImageView imageView = new ImageView(displayedImage);
    imageView.setViewport(new Rectangle2D(0, 0, size, size));
    imageView.setPreserveRatio(true);
    imageView.setFitHeight(size);
    imageView.setFitWidth(size);
    imageView.setLayoutX(1);
    imageView.setLayoutY(1);
    len.getChildren().add(imageView);
    pane.getChildren().add(len);
    Pointer pointer = new Pointer();
    pointer.pointed = lensZoomRatio;
    displayedImageView.setOnMouseEntered(
        new EventHandler<MouseEvent>() {
          @Override
          public void handle(MouseEvent event) {
            len.setVisible(true);
          }
        });
    displayedImageView.setOnMouseExited(
        new EventHandler<MouseEvent>() {
          @Override
          public void handle(MouseEvent event) {
            len.setVisible(false);
          }
        });
    displayedImageView.setCursor(Cursor.CROSSHAIR);
    displayedImageView.setOnMouseMoved(
        new EventHandler<MouseEvent>() {
          @Override
          public void handle(MouseEvent event) {
            double x = event.getSceneX() + 1, y = event.getSceneY() + 1;
            if (x + size + 2 > pane.getWidth()) {
              x = event.getSceneX() - size - 2;
            }
            if (y + size + 2 > pane.getHeight()) {
              y = event.getSceneY() - size - 2;
            }
            if (x < 0) {
              x = 0;
            }
            if (y < 0) {
              y = 0;
            }
            len.setLayoutX(x);
            len.setLayoutY(y);
            double viewPortSize = (1 / ((double) pointer.pointed)) * size;
            imageView.setViewport(
                new Rectangle2D(
                    Double.max(
                        0,
                        Double.min(
                            event.getX() / scaleRatio - viewPortSize / 2,
                            displayedImage.getWidth() - viewPortSize)),
                    Double.max(
                        0,
                        Double.min(
                            displayedImage.getHeight() - viewPortSize,
                            event.getY() / scaleRatio - viewPortSize / 2)),
                    viewPortSize,
                    viewPortSize));
          }
        });
  }

  public Transition setClickVolumeDown(int times) {
    ButtonPosition pos = deviceImage.getVolumeDown();
    return setClick(pos, times);
  }

  public Pane getImagePane() {
    return containerPane;
  }

  public void setImageBackground(Background background) {
    if (contentPane != null) {
      contentPane.setBackground(background);
    }
  }

  public void setBackgroundColor(Color color) {
    setImageBackground(new Background(new BackgroundFill(color, null, null)));
  }

  public Transition setClickVolumeUp(int times) {
    ButtonPosition pos = deviceImage.getVolumeUp();
    return setClick(pos, times);
  }

  public Transition setClickPower(int times) {
    ButtonPosition pos = deviceImage.getPower();
    return setClick(pos, times);
  }

  private Transition setClick(ButtonPosition pos, int times) {
    if (pos == null) {
      return null;
    }
    Rectangle rectangle =
        new Rectangle(
            pos.left * scaleRatio,
            pos.top * scaleRatio,
            pos.width * scaleRatio,
            pos.height * scaleRatio);
    double centerX = (pos.left + pos.width / 2);
    double centerY = (pos.top + pos.height / 2);
    rectangle.setFill(Color.RED);
    containerPane.getChildren().add(rectangle);
    Transition transition = buildCircleTransition(centerX, centerY, times, false, false);
    transition
        .statusProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              if (Animation.Status.STOPPED.equals(newValue)) {
                containerPane.getChildren().removeAll(rectangle);
              }
            });
    return transition;
  }

  public double getScaleRatio() {
    return scaleRatio;
  }

  public Transition buildCircleTransition(double x, double y, int times) {
    return buildCircleTransition(x, y, times, true, true);
  }

  public void removeCircleAnimation() {
    synchronized (circlesAnimation) {
      for (Transition transition : circlesAnimation) {
        transition.stop();
      }
      circlesAnimation.clear();
    }
  }

  public Transition buildCircleTransition(
      double x, double y, int times, boolean addBorder, boolean unique) {
    if (!Platform.isFxApplicationThread()) {
      CompletableFuture<Transition> future = new CompletableFuture<>();
      Platform.runLater(
          new Runnable() {
            @Override
            public void run() {
              future.complete(buildCircleTransition(x, y, times, addBorder, unique));
            }
          });
      try {
        return future.get();
      } catch (InterruptedException e) {
        removeCircleAnimation();
        return null;
      } catch (ExecutionException e) {
        removeCircleAnimation();
        return null;
      }
    }
    if (unique) {
      removeCircleAnimation();
    }
    Circle circle =
        new Circle(
            x * scaleRatio
                + (addBorder ? deviceImage.getLeftOffset() * scaleRatio + imageOffsetX : 0),
            y * scaleRatio
                + (addBorder ? imageOffsetY + deviceImage.getTopOffset() * scaleRatio : 0),
            wantedHeight / 12);
    circle.setStroke(Color.RED);
    circle.setStrokeWidth(3);
    circle.setFill(Color.TRANSPARENT);
    circle.setVisible(false);
    circle.setOpacity(0);
    circle.setMouseTransparent(true);
    Transition transition = getCircleTransition(circle, times);
    containerPane.getChildren().add(circle);
    transition
        .statusProperty()
        .addListener(
            new ChangeListener<Animation.Status>() {
              @Override
              public void changed(
                  ObservableValue<? extends Animation.Status> observable,
                  Animation.Status oldValue,
                  Animation.Status newValue) {
                if (Animation.Status.STOPPED.equals(newValue)) {
                  circle.setVisible(false);
                  containerPane.getChildren().remove(circle);
                } else if (Animation.Status.RUNNING.equals(newValue)) {
                  circle.setVisible(true);
                }
              }
            });
    transition.play();
    synchronized (circlesAnimation) {
      this.circlesAnimation.add(transition);
    }
    return transition;
  }

  private Transition getCircleTransition(Circle circle, int times) {
    ScaleTransition transition = new ScaleTransition(Duration.millis(1500), circle);
    transition.setFromX(0);
    transition.setFromY(0);
    transition.setToX(1);
    transition.setToY(1);
    transition.setAutoReverse(false);
    transition.setCycleCount(times);
    FadeTransition fadeTransition = new FadeTransition(Duration.millis(1500), circle);
    fadeTransition.setFromValue(1);
    fadeTransition.setToValue(0);
    fadeTransition.setAutoReverse(false);
    fadeTransition.setCycleCount(times);
    fadeTransition.setInterpolator(
        new Interpolator() {
          @Override
          protected double curve(double t) {
            return t * t;
          }
        });
    return new ParallelTransition(transition, fadeTransition);
  }

  public static class ButtonPosition {
    public double top, left, width, height;

    public ButtonPosition(double top, double left, double width, double height) {
      this.top = top;
      this.left = left;
      this.width = width;
      this.height = height;
    }
  }
}
