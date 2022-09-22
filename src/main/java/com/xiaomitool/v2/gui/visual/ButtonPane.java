package com.xiaomitool.v2.gui.visual;

import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.utility.NotNull;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ButtonPane extends VBox {
    private double buttonHeight = 50;
    private StackPane contentPane;
    private List<CustomButton> buttons = new ArrayList<>();
    private IDClickReceiver idClickReceiver = new IDClickReceiver();

    public ButtonPane(String... text) {
        this(Arrays.asList(text), -1);
    }

    public ButtonPane(LRes... text) {
        List<String> stringList = new ArrayList<>(text.length);
        for (LRes t : text) {
            stringList.add(t.toString());
        }
        build(stringList);
    }

    public ButtonPane(Iterable<String> text, int buttonHeight) {
        this.buttonHeight = buttonHeight > 0 ? buttonHeight : this.buttonHeight;
        build(text);
    }

    private void build(Iterable<String> buttonText) {
        super.setAlignment(Pos.BOTTOM_CENTER);
        HBox buttonPane = new HBox();
        buttonPane.setPrefHeight(buttonHeight);
        buttonPane.setMinHeight(buttonHeight);
        contentPane = new StackPane();
        contentPane.setPrefHeight(5000);
        buildButtons(buttonPane, buttonText);
        super.getChildren().addAll(contentPane, buttonPane);
    }

    private void buildButtons(HBox buttonPane, Iterable<String> buttonText) {
        buttonPane.setSpacing(20);
        buttonPane.setAlignment(Pos.CENTER);
        int i = 0;
        double fontSize = buttonHeight / 3.333;
        double size = findButtonSize(buttonText, fontSize) + 30;
        for (String text : buttonText) {
            CustomButton button = new CustomButton(text);
            button.setPrefHeight(0.7 * buttonHeight);
            button.setPrefWidth(size);
            button.setMinWidth(3 * buttonHeight);
            button.setFont(Font.font(fontSize));
            button.setText(text);
            buttonPane.getChildren().add(button);
            buttons.add(button);
            idClickReceiver.addNode(button);
            ++i;
        }
    }

    public List<CustomButton> getButtons() {
        return buttons;
    }

    public void setContentText(@NotNull LRes text) {
        setContentText(text.toString());
    }

    public void setContentText(String text) {
        Text t = new Text(text);
        t.setFont(Font.font(16));
        t.setWrappingWidth(WindowManager.getContentWidth() - 150);
        t.setTextAlignment(TextAlignment.CENTER);
        setContent(t);
    }

    private double findButtonSize(Iterable<String> text, double fontSize) {
        double max = 0;
        for (String t : text) {
            Text b = new Text(t);
            b.setFont(Font.font(fontSize));
            double w = b.getLayoutBounds().getWidth();
            max = Double.max(max, w);
        }
        return max;
    }

    public int waitClick() throws InterruptedException {
        return idClickReceiver.waitClick();
    }

    public IDClickReceiver getIdClickReceiver() {
        return idClickReceiver;
    }

    public void setContent(Node content) {
        this.contentPane.getChildren().add(content);
    }
}
