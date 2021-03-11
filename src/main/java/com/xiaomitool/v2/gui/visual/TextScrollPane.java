package com.xiaomitool.v2.gui.visual;

import com.xiaomitool.v2.gui.GuiUtils;
import com.xiaomitool.v2.gui.WindowManager;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.concurrent.Callable;

public class TextScrollPane extends StackPane {
    private Color bgColor = WindowManager.DEFAULT_BACKGROUND_COLOR;

    public TextScrollPane(Text texts) {
        build(texts, null);
    }

    public TextScrollPane(Text text, String title) {
        build(text, title);
    }

    public TextScrollPane(Color color, Text texts, String title) {
        bgColor = color;
        build(texts, title);
    }

    private void build(Text text, String title) {
        ScrollPane scrollPane = new ScrollPane();
        text.wrappingWidthProperty().bind(scrollPane.widthProperty().subtract(20));
        scrollPane.setFitToWidth(true);
        Node toCenter;
        if (title == null) {
            toCenter = text;
        } else {
            VBox vbox = new VBox();
            vbox.setAlignment(Pos.CENTER);
            vbox.setSpacing(10);
            vbox.getChildren().addAll(buildTitle(title), text);
            toCenter = vbox;
        }
        StackPane content = new StackPane(toCenter);
        content.prefHeightProperty().bind(Bindings.createObjectBinding(new Callable<Number>() {
            @Override
            public Number call() throws Exception {
                return scrollPane.getViewportBounds().getHeight();
            }
        }, scrollPane.viewportBoundsProperty()));
        scrollPane.setContent(content);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: rgba(245,245,245,0); -fx-background-color: rgba(245,245,245,0);");
        super.setBackground(GuiUtils.backgroundFromColor(Color.TRANSPARENT));
        super.getChildren().add(scrollPane);
    }

    private Label buildTitle(String title) {
        Label label = new Label(title);
        label.setFont(Font.font(null, FontWeight.SEMI_BOLD, 20));
        label.setPrefHeight(40);
        label.setTextAlignment(TextAlignment.CENTER);
        return label;
    }
}
