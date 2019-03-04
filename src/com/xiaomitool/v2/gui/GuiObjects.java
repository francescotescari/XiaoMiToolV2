package com.xiaomitool.v2.gui;

import javafx.scene.Node;

import java.util.HashMap;

public class GuiObjects extends HashMap<String, Node> {
    private static final GuiObjects instance = new GuiObjects();

    public static final String LOGIN_LINK = "login_link";
    public static final String LOGIN_NUMBER  ="login_number";
    public static final String TRANSLATED_LINK = "translated_link";
    public static final String IMG_CLOSE = "img_close";
    public static final String IMG_MINIFY = "img_minify";
    public static final String IMG_SETTINGS = "img_settings";

    public static  Node getNode(String name){
        return instance.get(name);
    }
    public static void set(String name, Node node){
        instance.put(name,node);
    }

}
