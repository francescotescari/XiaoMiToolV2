package com.xiaomitool.v2.gui.visual;

import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.utility.BezierInterpolator;
import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class LoadingAnimation extends StackPane {
    private long duration = 2000;
    private double animationWidth= 200;
    private double circleRadius = 5;
    private Color color = WindowManager.XIAOMI_COLOR;
    private double circleNumber = 5;
    private double easeParameter = 0.7;


    public LoadingAnimation(){
        build();
    }
    public LoadingAnimation(double animationWidth){
        this.animationWidth = animationWidth;
        this.circleRadius = animationWidth/45+2;
        build();
    }

    public double getCircleRadius() {
        return circleRadius;
    }

    private  Transition buildTransition(Node circle){
        TranslateTransition transition = new TranslateTransition(Duration.millis(duration), circle);
        transition.setFromX(-1*animationWidth);
        transition.setToX(animationWidth);
        transition.setAutoReverse(false);
        transition.setCycleCount(-1);
        double wait  =0.07+circleNumber*0.04;
        Log.debug(wait);
        transition.setInterpolator(new BezierInterpolator.WaitBezierInterpolator(0,easeParameter,1-easeParameter,1,wait,wait));
        FadeTransition transition1 = new FadeTransition(Duration.millis(duration/2),circle);
        transition1.setFromValue(0);
        transition1.setToValue(1);
        transition1.setAutoReverse(true);
        transition1.setCycleCount(-1);
        transition1.setInterpolator(new BezierInterpolator.WaitBezierInterpolator(0,easeParameter,1,1,2*wait,0));
        ScaleTransition transition2 = new ScaleTransition(Duration.millis(duration/2),circle);

        transition2.setFromX(0.6);
        transition2.setFromY(0.6);
        transition2.setToX(1.4);
        transition2.setToY(1.4);
        transition2.setAutoReverse(true);
        transition2.setCycleCount(-1);
        transition2.setInterpolator(new BezierInterpolator.WaitBezierInterpolator(0,easeParameter,1,1,2*wait,0));
        return new ParallelTransition(transition, transition1, transition2);
    }

    private Circle buildCircle(){
        Circle circle = new Circle(circleRadius,color);
        circle.setOpacity(0);
        return circle;
    }

    private void build(){
        ParallelTransition parallelTransition = new ParallelTransition();
        for (int i = 0; i<circleNumber; ++i){
            Circle circle = buildCircle();
            Transition transition = buildTransition(circle);
            transition.setDelay(Duration.millis(i*duration/10));
            parallelTransition.getChildren().add(transition);
            super.getChildren().add(circle);
        }
        parallelTransition.play();
    }

    public long getDuration() {
        return duration;
    }

    public double getAnimationWidth() {
        return animationWidth;
    }

    public static class WithText extends VBox {
        private double animWidth = -1;
        public WithText(String text, double width){
            this.animWidth = width;
            build(text);
        }
        public WithText(String text){
            this(text,-1);
        }
        public WithText(LRes text){
            this(text.toString());
        }
        public WithText(LRes text, double animWidth){
            this(text.toString(),animWidth);
        }
        private Text t;
        private void build(String text){
             t = new Text(text);

            LoadingAnimation animation = animWidth > 0 ? new LoadingAnimation(animWidth) : new LoadingAnimation();
            double size = 8+animation.getAnimationWidth()/18;
            t.setFont(Font.font(size));
            super.setSpacing(size);
            super.setAlignment(Pos.CENTER);
            super.getChildren().addAll(t,animation);
            t.setTextAlignment(TextAlignment.CENTER);

        }
        public void setText(String text){
            if (t != null)
            t.setText(text);
        }

    }

}
