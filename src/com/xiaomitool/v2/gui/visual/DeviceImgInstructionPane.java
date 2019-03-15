package com.xiaomitool.v2.gui.visual;



import com.xiaomitool.v2.gui.GuiUtils;
import com.xiaomitool.v2.gui.deviceView.Animatable;
import com.xiaomitool.v2.gui.deviceView.AnimatableDeviceView;
import com.xiaomitool.v2.gui.deviceView.DeviceView;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.utility.RunnableWithArg;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.HashMap;
import java.util.List;

public class DeviceImgInstructionPane extends SidePane {


    private String[] texts;
    private Image[] images;
    private HashMap<Integer, Animatable.AnimationPayload> animations;

    public DeviceImgInstructionPane(double wantedHeight, double maxHeight, String[] texts, Image[] images, HashMap<Integer, Animatable.AnimationPayload> animations){
        this.texts = texts;
        this.images = images;
        this.animations = animations;
        build(wantedHeight,maxHeight);
    }


    private int index = 0;
    private Button prevButton, nextButton;
    private Text instructionText;
    private AnimatableDeviceView deviceView;
    private ButtonPane closeButtonPane;
    private Transition currentAnimation = null;
    private void build(double wantedHeight, double maxHeight){
        if (texts == null || images == null || texts.length != images.length || texts.length == 0){
            throw new IllegalArgumentException("Please, pass not null, not empty arrays with same num of elements");
        }
        ButtonPane buttonPane = new ButtonPane(LRes.PREV_STEP, LRes.NEXT_STEP);
        closeButtonPane = new ButtonPane(LRes.OK_FINISHED);
        List<CustomButton> btns = buttonPane.getButtons();
        prevButton = btns.get(0);
        nextButton = btns.get(1);
        prevButton.setDisable(true);
        instructionText = new Text();
        instructionText.setWrappingWidth(400);
        instructionText.setFont(Font.font(15));
        instructionText.setTextAlignment(TextAlignment.CENTER);
        buttonPane.setContent(instructionText);
        deviceView = new AnimatableDeviceView(DeviceView.DEVICE_18_9, wantedHeight) {
            @Override
            public boolean animate(int step) throws InterruptedException {
                if (animations != null) {
                    Animatable.AnimationPayload payload = animations.get(step);
                    if (payload != null) {
                        currentAnimation = this.buildCircleTransition(payload.getX(), payload.getY(), payload.getTimes(), true, payload.isUnique());
                    } else {
                        deviceView.removeCircleAnimation();
                    }
                }
                return index < texts.length -1;
            }
        };
        deviceView.setContent(images[index]);
        buttonPane.getIdClickReceiver().addListener(new RunnableWithArg() {
            @Override
            public void run(Object arg) {
                int click = (int) arg;
                if (click == 0){
                    index--;
                } else {
                    index++;
                }
                loadStep();
            }
        });
        closeButtonPane.setContent(buttonPane);
        StackPane stackPane = new StackPane(closeButtonPane);
        stackPane.setPadding(new Insets(20,0,30,20));
        loadStep();
        this.setLeft(stackPane);
        this.setRight(GuiUtils.center(DeviceView.crop(deviceView, wantedHeight, wantedHeight > maxHeight ? (wantedHeight - maxHeight)/2 : 0)));
    }

    public void animate() throws InterruptedException {
        deviceView.animate(index);
    }

    public void setOverlayLen(OverlayPane pane, double zoomRatio){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                deviceView.setLenOverlay(pane,300, zoomRatio);
            }
        };
        if (!Platform.isFxApplicationThread()){
            Platform.runLater(runnable);
        } else {
            runnable.run();
        }

    }

    private void loadStep(){
        Image toLoadImg = images[index];
        String toLoadText = LRes.STEP.toString()+" ["+(index+1)+"/"+(texts.length)+"]:\n"+texts[index];

        if (index == 0){
            prevButton.setDisable(true);
        } else {
            prevButton.setDisable(false);
        }
        if (index == texts.length -1){
            nextButton.setDisable(true);
        } else{
            nextButton.setDisable(false);
        }
        instructionText.setText(toLoadText);
        deviceView.setContent(toLoadImg);
        try {
            animate();
        } catch (InterruptedException e) {
            Log.warn("Animation error: "+e.getMessage());
        }
    }

    public int waitClick() throws InterruptedException {
        return this.closeButtonPane.waitClick();
    }

    public IDClickReceiver getIdClickReceiver(){
        return this.closeButtonPane.getIdClickReceiver();
    }




}
