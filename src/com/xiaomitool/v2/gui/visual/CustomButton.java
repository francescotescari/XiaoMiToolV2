package com.xiaomitool.v2.gui.visual;

import com.xiaomitool.v2.language.LRes;
import javafx.scene.Cursor;
import javafx.scene.control.Button;

public class CustomButton extends Button {
    public CustomButton(){
        super();
        init();
    }
    public CustomButton(String text){
        super(text);
        init();
    }
    public CustomButton(LRes lRes){
        this(lRes.toString());
    }
    private void init(){
        this.setCursor(Cursor.HAND);
    }
}
