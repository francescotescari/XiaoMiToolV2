package com.xiaomitool.v2.gui.deviceView;

import javafx.scene.image.Image;
import org.apache.commons.io.input.NullInputStream;

public class DeviceImage {

    public static final DeviceImage EMPTY = new DeviceImage(null, 0, 0, 0, 0, 0, 0 ,null, null, null);
    private Image deviceImage;
        private double topOffset, leftOffset, heightInner, widthInner, heightOuter, widthOuter;
        private DeviceView.ButtonPosition volumeUp, volumeDown, power;

        public DeviceImage(Image image, double top, double left, double heightInner, double widthInner, double heightOuter, double widthOuter, DeviceView.ButtonPosition volumeUp, DeviceView.ButtonPosition volumeDown, DeviceView.ButtonPosition power) {
            this.deviceImage = image;
            this.topOffset = top;
            this.leftOffset = left;
            this.heightInner = heightInner;
            this.widthInner = widthInner;
            this.widthOuter = widthOuter;
            this.heightOuter = heightOuter;
            this.volumeDown = volumeDown;
            this.volumeUp = volumeUp;
            this.power = power;
        }

        public double getLeftOffset() {
            return leftOffset;
        }

        public double getTopOffset() {
            return topOffset;
        }

        public Image getDeviceImage() {
            return deviceImage == null ? new Image(new NullInputStream(0)) : deviceImage;
        }

        public double getInnerHeight() {
            return heightInner;
        }

        public double getInnerWidth() {
            return widthInner;
        }

        public double getOuterWidth() {
            return widthOuter;
        }

        public double getOuterHeight() {
            return heightOuter;
        }

        public DeviceView.ButtonPosition getVolumeDown() {
            return volumeDown;
        }

        public DeviceView.ButtonPosition getPower() {
            return power;
        }

        public DeviceView.ButtonPosition getVolumeUp() {
            return volumeUp;
        }

}
