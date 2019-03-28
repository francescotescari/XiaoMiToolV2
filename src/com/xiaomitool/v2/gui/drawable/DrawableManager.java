package com.xiaomitool.v2.gui.drawable;

import com.xiaomitool.v2.logging.Log;
import javafx.scene.image.Image;

import java.net.URL;

public class DrawableManager {
    public static String FASTBOOT_LOGO = "fastboot.png";
    public static String MIUI10 = "miui10.png";
    public static String NO_CONNECTION = "no_connection.png";
    public static String DEVICE_AUTH = "device_auth.png";
    public static String ERROR = "error.png";
    public static URL getResource(String name){
        URL resourcePath = DrawableManager.class.getResource("./"+name);
        Log.debug("Resource: "+resourcePath);
        return resourcePath;
    }
    public static URL getPng(String name){
        if (!name.endsWith(".png")){
            name+=".png";
        }
        return getResource(name);
    }

    public static Image getResourceImage(String name){
        URL url = getResource(name);
        return url == null ? null : new Image(url.toString());
    }
}
