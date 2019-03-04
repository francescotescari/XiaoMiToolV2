package com.xiaomitool.v2.gui.deviceView;


import com.xiaomitool.v2.gui.GuiUtils;
import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.gui.drawable.DrawableManager;
import com.xiaomitool.v2.logging.Log;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class DeviceRecoveryView extends DeviceView implements Animatable {
    private Rectangle selectRectangle;
    private ImageView recoveryImage =  new ImageView(new Image(DrawableManager.getPng("recovery").toString())), pcsuiteImage = new ImageView(new Image(DrawableManager.getPng("pcsuiteicon").toString()));
    public DeviceRecoveryView(DeviceImage deviceImage, double wantedHeight) {
        super(deviceImage, wantedHeight, Color.BLACK, null);

        this.setContent(recoveryImage);
        selectOption(1);
        //pcsuiteImage.setViewport(new Rectangle2D(0,-1*(deviceImage.getInnerHeight()-pcsuiteImage.getImage().getHeight())/2,pcsuiteImage.getImage().getWidth(), deviceImage.getInnerHeight()));

    }

    private Rectangle buildSelectRectangle(){
        Rectangle rectangle = new Rectangle(152*scaleRatio,736*scaleRatio,776*scaleRatio,126*scaleRatio);
        rectangle.setFill(Color.rgb(51,180, 255));
        rectangle.setBlendMode(BlendMode.ADD);
        rectangle.setArcWidth(6);
        rectangle.setArcHeight(6);
        return rectangle;
    }
    public void selectOption(int index){
        if (selectRectangle == null){
            selectRectangle = buildSelectRectangle();
            Log.debug("Rectangle built");

        }
        try {
            imageWrapPane.getChildren().add(selectRectangle);
        } catch (Throwable t){
            Log.debug("Rectangle already present");
        }
        Log.debug("Layout y: "+(161*index)*scaleRatio);
        selectRectangle.setLayoutY((161*index)*scaleRatio);
    }

    public void animateSelectThird(long duration){
        DeviceRecoveryView view = this;
        setContent(recoveryImage);
        this.selectOption(0);
        this.setClickVolumeDown(1).setOnFinished(event -> {
            selectOption(1);
            setClickVolumeDown(1).setOnFinished(event1 -> {
                selectOption(2);
                setClickPower(1).setOnFinished(event2 -> {
                    setContent(pcsuiteImage, true);
                    GuiUtils.waitGui(duration, new Runnable() {
                        @Override
                        public void run() {
                            if (GuiUtils.hasParent(view,WindowManager.getMainPane())){
                                animateSelectThird(duration);
                            }


                        }
                    });

                });
            });
        });
    }


    @Override
    public void animate(int times, long duration) {
        animateSelectThird(duration);
    }
}
