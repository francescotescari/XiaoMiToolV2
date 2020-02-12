package com.xiaomitool.v2.gui.visual;

import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.gui.drawable.DrawableManager;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.utility.NotNull;
import com.xiaomitool.v2.utility.Nullable;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class ErrorPane extends ButtonPane {
    private static Image ERROR_IMAGE;
    private StackPane textZone, titleZone;
    private VBox vBox;

    public ErrorPane(String... texts) {
        super(texts);
        build();
    }

    public ErrorPane(LRes... labels) {
        super(labels);
        build();
    }

    private Image getErrorImage() {
        if (ERROR_IMAGE == null) {
            ERROR_IMAGE = new Image(DrawableManager.getPng(DrawableManager.ERROR).toString(), 100, 100, true, true);
        }
        return ERROR_IMAGE;
    }

    private void build() {
        ImageView errImg = new ImageView(getErrorImage());
        errImg.setFitWidth(100);
        errImg.setFitHeight(100);
        titleZone = new StackPane();
        textZone = new StackPane();
        vBox = new VBox(new StackPane(errImg), titleZone, textZone);
        vBox.setSpacing(30);
        vBox.setAlignment(Pos.CENTER);
        ScrollPane scrollPane = new ScrollPane(vBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: rgba(245,245,245,0); -fx-background-color: rgba(245,245,245,0);");
        super.setContent(scrollPane);
    }

    public void setTitle(String text, @Nullable Paint paint) {
        Text t = new Text(text);
        t.setFont(Font.font(17));
        if (paint != null) {
            t.setFill(paint);
        }
        t.setTextAlignment(TextAlignment.CENTER);
        t.setWrappingWidth(WindowManager.getContentWidth() - 100);
        ObservableList<Node> children = titleZone.getChildren();
        children.clear();
        children.add(t);
    }

    public void appendContent(Node node) {
        vBox.getChildren().add(node);
    }

    public void setTitle(String text) {
        setTitle(text, null);
    }

    public void setTitle(@NotNull LRes lRes) {
        setTitle(lRes.toString());
    }

    public void setText(String text, @Nullable Paint paint) {
        Text t = new Text(text);
        t.setFont(Font.font(15));
        if (paint != null) {
            t.setFill(paint);
        }
        t.setTextAlignment(TextAlignment.CENTER);
        t.setWrappingWidth(WindowManager.getContentWidth() - 100);
        ObservableList<Node> children = textZone.getChildren();
        children.clear();
        children.add(t);
    }

    public void setText(String text) {
        setText(text, null);
    }

    public void setText(@NotNull LRes lRes) {
        setText(lRes.toString());
    }
}
