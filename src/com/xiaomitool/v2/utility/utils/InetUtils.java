package com.xiaomitool.v2.utility.utils;

import com.xiaomitool.v2.engine.ToolManager;
import com.xiaomitool.v2.inet.CustomHttpException;
import com.xiaomitool.v2.inet.CustomHttpRequest;
import com.xiaomitool.v2.inet.EasyHttp;
import com.xiaomitool.v2.inet.EasyResponse;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.utility.WaitSemaphore;
import org.apache.commons.lang3.SystemUtils;

import java.awt.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.List;

public class InetUtils {
    public static String urlEncode(String data){
        try {
            return URLEncoder.encode(data,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
    public static String getRedirectUrl(String url) throws CustomHttpException {
        return getRedirectUrl(url,null);
    }
    public static String getRedirectUrl(String url, String referer) throws CustomHttpException {
        EasyHttp request = new EasyHttp().url(url).setHeadOnly();
        if (referer != null){
            Log.debug("Set referer: "+referer);
            request = request.referer(referer);
        }
        EasyResponse response = request.exec();
        List<String> list = response.getHeaders().get("location");
        if (list == null){
            return null;
        }
        Log.debug("Redirect from "+url+" to "+list.get(0));
        return list.get(0);
    }

    public static void openUrlInBrowser(String url){
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {

                try {
                    Desktop.getDesktop().browse(new URI(url));
                    return;
                } catch (Exception e) {
                    Log.error("Failed to open url " + url + " using java.awt.Desktop: " + e.getMessage());
                    Log.debug(e);
                }
            }
            String cmd = "";
            if (SystemUtils.IS_OS_WINDOWS) {
                cmd = "rundll32 url.dll,FileProtocolHandler " + url;
            } else if (SystemUtils.IS_OS_MAC) {
                cmd = "open " + url;
            } else {
                cmd = "xdg-open " + url;
            }
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec(cmd);
            } catch (IOException e) {
                Log.error("Failed to open url " + url + " using runtime command: " + e.getMessage());
                Log.debug(e);
            }
        } catch (Throwable t){
            Log.error("Failed to open url " + url + " : " + t.getMessage());
        }

    }
    static boolean internetAvailable;
    static int tried = 0;
    public static synchronized boolean isInternetAvailable(){
        internetAvailable = false;
        tried = 0;
        WaitSemaphore waitSemaphore = new WaitSemaphore(0);
        new Thread(() -> {
            try {
                ++tried;
                InetAddress xmt = InetAddress.getByName("www.xiaomitool.com");
                if (xmt.isReachable(5000)){
                    Log.info("xiaomitool.com is reachable");
                   internetAvailable = true;
                   waitSemaphore.increase();
                   return;
                } else {
                    Log.warn("xiaomitool.com is not reachable");
                }
            } catch (IOException e) {
                Log.error("Failed to reach xmt host: "+e.getMessage());
            }
            if (tried >= 3){
                Log.error("Tried to reach three host and no one was reachable");
                waitSemaphore.increase();
            }
        }).start();
        new Thread(() -> {
            try {
                ++tried;
                InetAddress xmt = InetAddress.getByName("www.google.com");
                if (xmt.isReachable(5000)){
                    Log.info("google.com is reachable");
                    internetAvailable = true;
                    waitSemaphore.increase();
                    return;
                } else {
                    Log.warn("google.com is not reachable");
                }
            } catch (IOException e) {
                Log.error("Failed to reach google host: "+e.getMessage());
            }
            if (tried >= 3 ){
                Log.error("Tried to reach three host and no one was reachable");
                waitSemaphore.increase();
            }
        }).start();
        new Thread(() -> {
            try {
                ++tried;
                InetAddress xmt = InetAddress.getByName("www.miui.com");
                if (xmt.isReachable(5000)){
                    Log.info("miui.com is reachable");
                    internetAvailable = true;
                    waitSemaphore.increase();
                    return;
                } else {
                    Log.warn("miui.com is not reachable");
                }
            } catch (IOException e) {
                Log.error("Failed to reach miui host: "+e.getMessage());
            }
            if (tried >= 3){
                Log.error("Tried to reach three host and no one was reachable");
                waitSemaphore.increase();
            }
        }).start();
        try {
            waitSemaphore.waitOnce();
        } catch (InterruptedException e) {
            Log.error("Internet check failed because thread interrupted: "+e.getMessage());
        }
        return internetAvailable;
    }

    public static int checkForUpdates(String url, String version, String pcID)  {
        EasyResponse response = null;
        try {
            response = EasyHttp.get(url+"?v="+version+"&i="+pcID);
        } catch (CustomHttpException e) {
            Log.error("Failed to run update request: "+e.getMessage());
            return -6;
        }
        if (!response.isCodeRight()){
            Log.error("Update request returned with code: "+response.getCode());
            return -2;
        } else if (!response.isAllRight()){
            Log.error("Update request has empty body");
            return -3;
        }
        String body = response.getBody();
        if (body == null){
            Log.error("Update response object has null body");
            return -4;
        }
        body = body.trim();
        if ("ALLRIGHT".equals(body)){
            return 0;
        } else if ("UPDATE".equals(body)){
            return 1;
        } else  if ("INVALID".equals(body)){
            Log.error("Update request is not valid: v="+version+", i="+pcID);
            return -1;
        } else if ("BLOCK".equals(body)){

            ToolManager.exit(-1);
            return -100;
        }
        Log.error("Unknown response from update request: "+body);
        return -5;
    }

}
