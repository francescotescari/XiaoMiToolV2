package com.xiaomitool.v2.gui.deviceView;

import com.xiaomitool.v2.utility.RunnableWithArg;

public abstract class AnimatableDeviceView extends DeviceView implements Animatable {
    private RunnableWithArg animationCallback;

    public AnimatableDeviceView(DeviceImage deviceImage, double wantedHeight) {
        super(deviceImage, wantedHeight);
    }

    public void setAnimationCallback(RunnableWithArg animationCallback) {
        this.animationCallback = animationCallback;
    }

    @Override
    public void animate(int times, long duration) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < times; ++i) {
                    try {
                        int step = 0;
                        while (animate(step)) {
                            if (animationCallback != null) {
                                animationCallback.run(step);
                            }
                            step++;
                        }
                        Thread.sleep(duration);
                    } catch (InterruptedException e) {
                        animationCallback.run(-1);
                        break;
                    }
                }
            }
        }).start();
    }

    public abstract boolean animate(int step) throws InterruptedException;
}
