package com.xiaomitool.v2.gui.visual;


import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.gui.drawable.DrawableManager;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public  class TextStackPane extends StackPane {
    private Color background = WindowManager.DEFAULT_BACKGROUND_COLOR;
    private double paneWidth, paneHeight;
    public TextStackPane(double width, double height){
        this.paneWidth = width;
        this.paneHeight = height;
        build();
    }
    private double moved = 0;
    private VBox vBox;
    private HBox hBox;
    private Pane movingPane;
    private void build(){

        vBox = new VBox();
        // vBox.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,null,BorderStroke.THICK)));
        vBox.setAlignment(Pos.TOP_CENTER);
        vBox.setPrefHeight(-1);
        //GuiUtils.debug(vBox,Color.RED);
        hBox = new HBox(vBox);
        hBox.setPrefWidth(paneWidth);
        hBox.setAlignment(Pos.TOP_CENTER);
        hBox.setLayoutY(paneHeight);
        movingPane = new Pane(hBox);
        ImageView imageView = new ImageView(new Image(DrawableManager.getPng("overlay").toString(),paneWidth,paneHeight,false,true));
        imageView.setFitHeight(paneHeight);
        imageView.setFitWidth(paneWidth);
        // GuiUtils.debug(vBox,Color.RED);
        super.getChildren().addAll(movingPane,imageView);
        super.setPrefSize(paneWidth,paneHeight);
        super.setMaxSize(paneWidth,paneHeight);
        movingPane.setClip(new Rectangle(paneWidth,paneHeight));
        super.setClip(new Rectangle(paneWidth,paneHeight));
        super.setBackground(new Background(new BackgroundFill(background, null,null)));

    }

    public void addText(String text){
        if (!Platform.isFxApplicationThread()){
            Platform.runLater(() -> addText(text));
            return;
        }

        Text t = new Text(text);
        t.setTextAlignment(TextAlignment.CENTER);
        if (super.getWidth() > 0) {
            t.setWrappingWidth(super.getWidth()*0.9);
        } else {
            t.setWrappingWidth(paneWidth);
        }
        t.setFont(Font.font(16));
        StackPane textContainer = new StackPane(t);
        double toMove = t.getLayoutBounds().getHeight();
        toMove+=14;
        textContainer.setMaxHeight(toMove);
        textContainer.setMinHeight(toMove);
        textContainer.setPrefHeight(toMove);
        vBox.getChildren().add(textContainer);
        toMove+=moved+1;

        if (toMove > 0){
            buildTransition(hBox, toMove).play();
        }
        //Log.debug("Moved: "+moved+", toMove: "+toMove+", height: "+hBox.getHeight());
        moved = toMove;

        //buildTransition(t).play();
    }
    private Transition buildTransition(Node node, double toMove){
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(500),node);
        translateTransition.setFromY(node.getTranslateY());
        translateTransition.setToY(-1*toMove);
        translateTransition.setCycleCount(1);
        translateTransition.setAutoReverse(false);
        translateTransition.setInterpolator(Interpolator.EASE_OUT);
        return translateTransition;
    }

}
