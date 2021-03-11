package com.xiaomitool.v2.resources;

import org.apache.commons.lang3.SystemUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class ResourcesConst {
    public static final String OSNAME_WINDOWS = "windows";
    public static final String OSNAME_LINUX = "linux";
    public static final String OSNAME_MACOS = "macos";
    public static final String OSNAME_GENERIC = "generic";

    public static String getOSName() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return OSNAME_WINDOWS;
        } else if (SystemUtils.IS_OS_MAC) {
            return OSNAME_MACOS;
        } else {
            return OSNAME_LINUX;
        }
    }

    public static boolean isWin10() {
        return SystemUtils.IS_OS_WINDOWS_10;
    }

    public static boolean isWindows() {
        return SystemUtils.IS_OS_WINDOWS;
    }

    public static boolean isMac() {
        return SystemUtils.IS_OS_MAC;
    }

    public static boolean isLinux() {
        return SystemUtils.IS_OS_LINUX;
    }

    public static Charset interalCharset() {
        return StandardCharsets.UTF_8;
    }

    public static Charset uiCharset() {
        return Charset.defaultCharset();
    }

    public static String getOSExeExtension() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return ".exe";
        }
        return "";
    }

    public static String getShellPath() {
        if (isWindows()) {
            return "cmd";
        } else {
            return "/bin/sh";
        }
    }

    public static String[] getShellArgs() {
        if (isWindows()) {
            return new String[]{"/C"};
        } else {
            return new String[]{"-c"};
        }
    }

    public static String getOSLogString() {
        return System.getProperty("os.name") + " - " + System.getProperty("os.arch") + " - " + System.getProperty("os.version");
    }

    public static String getLocaleLogString() {
        return Locale.getDefault() + " - " + Charset.defaultCharset();
    }

    public static String getLogString() {
        return "OS info: " + getOSLogString() + " ||| Locale info: " + getLocaleLogString();
    }

    public static String getOSExe(String exe) {
        return exe + getOSExeExtension();
    }
}
