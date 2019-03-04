package com.xiaomitool.v2.gui.drawable;

import java.net.URL;

public class DrawableManager {
    public static String FASTBOOT_LOGO = "fastboot.png";
    public static String MIUI10 = "miui10.png";
    public static String NO_CONNECTION = "no_connection.png";
    public static String DEVICE_AUTH = "device_auth.png";
    public static String ERROR = "error.png";
    public static URL getResource(String name){
        return DrawableManager.class.getResource(name);
    }
    public static URL getPng(String name){
        if (!name.endsWith(".png")){
            name+=".png";
        }
        return getResource(name);
    }
}
