package com.xiaomitool.v2.resources;


import org.apache.commons.lang3.SystemUtils;

import java.nio.file.Path;

public class ResourcesConst {
    public static final String OSNAME_WINDOWS = "windows";
    public static final String OSNAME_LINUX = "linux";
    public static final String OSNAME_MACOS = "macos";
    public static final String OSNAME_GENERIC = "generic";

    public static  String getOSName(){
        if (SystemUtils.IS_OS_WINDOWS){
            return OSNAME_WINDOWS;
        } else if(SystemUtils.IS_OS_MAC){
            return OSNAME_MACOS;
        } else {
            return OSNAME_LINUX;
        }
    }

    public static boolean isWindows(){
        return SystemUtils.IS_OS_WINDOWS;
    }
    public static boolean isMac(){
        return SystemUtils.IS_OS_MAC;
    }
    public static boolean isLinux(){
        return SystemUtils.IS_OS_LINUX;
    }



    public static String getOSExeExtension(){
        if (SystemUtils.IS_OS_WINDOWS){
            return ".exe";
        }
        return "";
    }

    public static String getShellPath(){
        if (isWindows()){
            return "cmd";
        } else {
            // add linux and mac
            return "/bin/sh";
        }
    }

    public static String[] getShellArgs(){
        if (isWindows()){
            return new String[]{"/C"};
        } else {
            // add linux and mac
            return new String[]{"-c"};
        }
    }

    public static String getOSLogString(){
        return System.getProperty("os.name") + " - "+ System.getProperty("os.arch") + " - " + System.getProperty("os.version");
    }

    public static String getOSExe(String exe) {
        return exe+getOSExeExtension();
    }


    // public static String IMAGE_TEST_B64 = "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";






}
