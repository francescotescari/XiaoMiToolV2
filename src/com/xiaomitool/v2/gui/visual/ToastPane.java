package com.xiaomitool.v2.gui.visual;

import com.xiaomitool.v2.utility.Pointer;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class ToastPane {
    private static final Background TOAST_BACK = new Background(new BackgroundFill(Color.gray(0.8), new CornerRadii(20), null));
    private static final Border TOAST_BORDER = new Border(new BorderStroke(Color.gray(0.3), BorderStrokeStyle.SOLID, new CornerRadii(20), BorderStroke.THIN));
    private double TOAST_WIDTH = 200;
    private int TOAST_DURATION = 3000;
    private double TOAST_RADIUS = 20;
    private double TOAST_SPACING = 10;
    private double TOAST_OFFSET = 0;
    private double lowHeight = TOAST_OFFSET;
    private OverlayPane overlayPane;

    public ToastPane(OverlayPane overlayPane, double width, double radius, double spacing, double offset, int duration) {
        this(overlayPane);
        this.TOAST_RADIUS = radius;
        this.TOAST_WIDTH = width;
        this.TOAST_SPACING = spacing;
        this.TOAST_DURATION = duration;
        this.TOAST_OFFSET = offset;
        this.lowHeight = offset;
    }

    public ToastPane(OverlayPane overlayPane) {
        this.overlayPane = overlayPane;
    }

    public void toast(String message) {
        lowHeight += TOAST_SPACING + TOAST_RADIUS;
        Text text = new Text(message);
        StackPane pane = new StackPane(text);
        pane.setPickOnBounds(false);
        double textHeight = text.getLayoutBounds().getHeight();
        pane.setBackground(TOAST_BACK);
        pane.setPrefWidth(TOAST_WIDTH);
        pane.setPadding(new Insets(TOAST_RADIUS / 2));
        pane.setLayoutX((overlayPane.getWidth() - TOAST_WIDTH) / 2);
        lowHeight += textHeight;
        Pointer currentHeight = new Pointer();
        currentHeight.pointed = lowHeight;
        pane.setLayoutY(overlayPane.getHeight() - lowHeight);
        pane.setBorder(TOAST_BORDER);
        overlayPane.getChildren().add(pane);
        for (Transition transition : getTransitions(pane)) {
            if (transition instanceof FadeTransition) {
                transition.setOnFinished(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        getParentPane().getChildren().remove(pane);
                        double current = (double) currentHeight.pointed;
                        double height = getParentPane().getHeight();
                        if (lowHeight == current || (current >= height / 2 && lowHeight >= height / 2)) {
                            lowHeight = TOAST_OFFSET;
                        }
                    }
                });
            }
            transition.play();
        }
    }

    private Pane getParentPane() {
        return overlayPane;
    }

    private Transition[] getTransitions(Pane pane) {
        Interpolator interpolator = new Interpolator() {
            @Override
            protected double curve(double t) {
                t = t * 10;
                return t < 1 ? t : 1;
            }
        };
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(TOAST_DURATION / 2), pane);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(0.75);
        fadeTransition.setCycleCount(2);
        fadeTransition.setAutoReverse(true);
        fadeTransition.setInterpolator(interpolator);
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(TOAST_DURATION / 2), pane);
        translateTransition.setFromY(15);
        translateTransition.setToY(0);
        translateTransition.setCycleCount(2);
        translateTransition.setAutoReverse(true);
        translateTransition.setInterpolator(interpolator);
        return new Transition[]{fadeTransition, translateTransition};
    }
}

