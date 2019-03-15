package com.xiaomitool.v2.gui.deviceView;

public interface Animatable {

    public  void animate(int times, long duration);


    public static class AnimationPayload {
        private double x,y;
        private int times;
        private boolean unique;
        public AnimationPayload(double x, double y, int times, boolean unique){
            this.x = x;
            this.y = y;
            this.times = times;
            this.unique = unique;
        }
        public double getX(){
            return x;
        }

        public double getY() {
            return y;
        }

        public int getTimes() {
            return times;
        }

        public boolean isUnique() {
            return unique;
        }
    }
}
