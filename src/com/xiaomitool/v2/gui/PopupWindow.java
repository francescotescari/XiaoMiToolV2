package com.xiaomitool.v2.gui;

import com.xiaomitool.v2.gui.drawable.DrawableManager;

import com.xiaomitool.v2.utility.Nullable;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


import java.net.URL;

public class PopupWindow {
    private double width, height;
    private Node content;
    public PopupWindow(){
        this(200,100);
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public PopupWindow(double width, double height){
        this.width =width;
        this.height = height;
    }

    public void setContent(Node content) {
        this.content = content;
    }

    public Node getContent() {
        return content;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public static class ImageTextPopup extends PopupWindow {
        private static final double IMG_HEIGHT = 40;

        public ImageTextPopup(String text, Icon icon){
            this(text,icon,null,null);
        }

        public ImageTextPopup(String text, Icon icon, @Nullable Double fontSize, @Nullable Double width) { ;
            Text t = new Text(text);
            if (width == null || width == 0){
                width = 300d;
            }
            if (fontSize == null || fontSize == 0){
                fontSize = 15d;
            }
            t.setFont(Font.font(fontSize));
            double origWidth = t.getLayoutBounds().getWidth();
            if (origWidth > 0 && origWidth/width < 2){
                width = origWidth;
            }

            t.setWrappingWidth(width);
            Bounds box = t.getLayoutBounds();
            if (4*box.getHeight() > 3*box.getWidth()){
                width = Math.sqrt(box.getHeight()*box.getWidth()*4/3);
                t.setWrappingWidth(width);
                width = t.getLayoutBounds().getWidth();
            }
            double height = t.getLayoutBounds().getHeight();
            if (height < IMG_HEIGHT+20){height=IMG_HEIGHT+20;}
            ImageView imageView = new ImageView(new Image(icon.getPng()));
            imageView.setFitHeight(IMG_HEIGHT);
            imageView.setPreserveRatio(true);
            StackPane imgPane = new StackPane(imageView);

            HBox hBox = new HBox(imgPane, new StackPane(t));
            hBox.setSpacing(10);
            hBox.setAlignment(Pos.CENTER);
            width = width+20+20+IMG_HEIGHT;
            setHeight(height+50);
            setWidth(width);
            setContent(hBox);
        }
    }

    public enum Icon {
        INFO("info.png"),
        WARN("caution.png"),
        ERROR("error.png");
        private URL png;
        Icon(String png){
           this.png = DrawableManager.getPng(png);
        }
        public String getPng() {
            return png.toString();
        }
    }
}
