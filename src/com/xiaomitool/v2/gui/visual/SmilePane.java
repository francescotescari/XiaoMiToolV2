package com.xiaomitool.v2.gui.visual;

import com.xiaomitool.v2.gui.drawable.DrawableManager;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

public class SmilePane extends StackPane {
    private double width, height, startHeight, mX, sw;
    private GraphicsContext context;

    public SmilePane(double size) {
        this(size, size);
    }

    public SmilePane(double width, double height) {
        super();
        super.setPrefSize(width, height);
        this.width = width;
        this.height = height;
        this.startHeight = height / 3;
        this.mX = width / 4;
        this.sw = width / 2;
        build();
    }

    private void build() {
        ImageView smile = new ImageView(new Image(DrawableManager.getPng("smile").toString()));
        smile.setFitHeight(height);
        smile.setFitWidth(width);
        smile.setPreserveRatio(true);
        Canvas canvas = new Canvas(this.width, this.height);
        context = canvas.getGraphicsContext2D();
        context.setLineWidth(width / 28);
        context.setStroke(Color.rgb(31, 31, 32));
        super.getChildren().addAll(canvas, smile);
    }

    public void setHappiness(double percent) {
        if (percent < 0) {
            percent = 0;
        } else if (percent > 100) {
            percent = 100;
        }
        boolean sad = percent < 50;
        double angle = sad ? 30 : 210;
        double add = sad ? startHeight : ((((100 - percent) / 75) + (1d / 3)) * startHeight);
        double h = (Math.abs(percent - 50) / 50) * startHeight + 1;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                context.clearRect(0, 0, width, height);
                context.strokeArc(mX, startHeight + add, sw, h, angle, 120, ArcType.OPEN);
            }
        };
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }

    public GraphicsContext getContext() {
        return context;
    }
}
