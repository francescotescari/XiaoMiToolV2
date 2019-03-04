package com.xiaomitool.v2.utility;

import javafx.animation.Interpolator;

public class BezierInterpolator extends Interpolator {
    private double p0,p1,p2,p3;
    public BezierInterpolator(double p0, double p1, double p2, double p3){
        this.p0 = p0;
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
    }

    @Override
    public double curve(double t) {
        return Math.pow(1-t,2)*t*3*p1+Math.pow(t,2)*(1-t)*3*p2+Math.pow(t,3)*p3;
    }
    public static class WaitBezierInterpolator extends BezierInterpolator {
        private double percentage = 0, waitBefore = 0, waitAfter = 0;
        public WaitBezierInterpolator(double p0, double p1, double p2, double p3, double waitBefore, double waitAfter) {
            super(p0, p1, p2, p3);
            percentage = 1-waitBefore-waitAfter;
            this.waitBefore = waitBefore;
            this.waitAfter = 1-waitAfter;
        }

        @Override
        public double curve(double t){

            if (t < waitBefore){

                return 0;
            } else  if (t > waitAfter){

                return 1;
            }

            return super.curve((t-waitBefore)/percentage);
        }
    }
}
