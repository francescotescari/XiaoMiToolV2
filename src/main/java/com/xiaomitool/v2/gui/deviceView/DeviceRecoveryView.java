package com.xiaomitool.v2.gui.deviceView;

import com.xiaomitool.v2.gui.GuiUtils;
import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.gui.drawable.DrawableManager;
import com.xiaomitool.v2.utility.utils.NumberUtils;
import javafx.animation.Animation;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class DeviceRecoveryView extends DeviceView implements Animatable {
  private Rectangle selectRectangle;
  private Image recoveryImage = new Image(DrawableManager.getPng("recovery").toString()),
      pcsuiteImage = new Image(DrawableManager.getPng("pcsuiteicon").toString());

  public DeviceRecoveryView(DeviceImage deviceImage, double wantedHeight) {
    super(deviceImage, wantedHeight, Color.BLACK, null);
    selectOption(0);
  }

  private Rectangle buildSelectRectangle() {
    Rectangle rectangle =
        new Rectangle(
            152 * scaleRatio,
            (736) * contentScaleRatio + NumberUtils.double2int(imageOffsetY),
            776 * scaleRatio,
            126 * scaleRatio);
    rectangle.setFill(Color.rgb(51, 180, 255));
    rectangle.setBlendMode(BlendMode.ADD);
    rectangle.setArcWidth(6);
    rectangle.setArcHeight(6);
    return rectangle;
  }

  public void selectOption(int index) {
    if (selectRectangle == null) {
      selectRectangle = buildSelectRectangle();
    }
    try {
      imageWrapPane.getChildren().add(selectRectangle);
    } catch (Throwable t) {
    }
    double layoutY =
        NumberUtils.double2int((736 + 161 * index) * scaleRatio)
            + NumberUtils.double2int(imageOffsetY);
    selectRectangle.setLayoutY(layoutY);
  }

  public void animateSelectThird(long duration, Runnable callback) {
    setContent(recoveryImage, true);
    this.selectOption(0);
    this.setClickVolumeDown(1)
        .setOnFinished(
            event -> {
              selectOption(1);
              setClickVolumeDown(1)
                  .setOnFinished(
                      event1 -> {
                        selectOption(2);
                        setClickPower(1)
                            .setOnFinished(
                                event2 -> {
                                  setContent(pcsuiteImage, true);
                                  GuiUtils.waitGui(duration, callback);
                                });
                      });
            });
  }

  public void animateSelectThird(long duration) {
    final DeviceRecoveryView view = this;
    this.animateSelectThird(
        duration,
        new Runnable() {
          @Override
          public void run() {
            if (GuiUtils.hasParent(view, WindowManager.getMainPane())) {
              animateSelectThird(duration);
            }
          }
        });
  }

  public void animateTurnOnSelectThird(long duration, Runnable callback) {
    setContent(Color.BLACK);
    Animation anim = this.setClickPower(3);
    Animation anim2 = this.setClickVolumeUp(3);
    anim.setOnFinished(
        new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent event) {
            setContent(recoveryImage, true);
            GuiUtils.waitGui(
                500,
                new Runnable() {
                  @Override
                  public void run() {
                    animateSelectThird(duration, callback);
                  }
                });
          }
        });
  }

  public void animateTurnOnSelectThird(long duration) {
    final DeviceRecoveryView view = this;
    animateTurnOnSelectThird(
        duration,
        new Runnable() {
          @Override
          public void run() {
            if (GuiUtils.hasParent(view, WindowManager.getMainPane())) {
              animateTurnOnSelectThird(duration);
            }
          }
        });
  }

  @Override
  public void animate(int times, long duration) {
    animateSelectThird(duration);
  }
}
