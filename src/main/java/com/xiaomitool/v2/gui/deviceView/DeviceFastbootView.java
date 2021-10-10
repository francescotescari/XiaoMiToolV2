package com.xiaomitool.v2.gui.deviceView;

import com.xiaomitool.v2.gui.GuiUtils;
import com.xiaomitool.v2.gui.drawable.DrawableManager;
import javafx.animation.Animation;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class DeviceFastbootView extends DeviceView implements Animatable {
  private static final Image fastbootImage =
      new Image(DrawableManager.getPng(DrawableManager.FASTBOOT_LOGO).toString());

  public DeviceFastbootView(DeviceImage deviceImage, double wantedHeight) {
    super(deviceImage, wantedHeight);
    setBackgroundColor(Color.BLACK);
    setContent(Color.BLACK);
  }

  @Override
  public void animate(int times, long duration) {
    Animation anim = this.setClickPower(3);
    Animation anim2 = this.setClickVolumeDown(3);
    anim.setOnFinished(
        new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent event) {
            setContent(fastbootImage, true);
            GuiUtils.waitGui(
                3000,
                new Runnable() {
                  @Override
                  public void run() {
                    if (times != 0) {
                      setContent(Color.BLACK);
                      animate(times - 1, duration);
                    }
                  }
                });
          }
        });
  }
}
