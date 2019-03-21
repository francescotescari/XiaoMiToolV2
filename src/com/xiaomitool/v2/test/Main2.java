package com.xiaomitool.v2.test;

import com.xiaomitool.v2.adb.AdbCommons;
import com.xiaomitool.v2.adb.device.DeviceProperties;
import com.xiaomitool.v2.inet.CustomHttpException;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.language.Lang;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.GuiListener;
import com.xiaomitool.v2.procedure.ProcedureRunner;
import com.xiaomitool.v2.procedure.Procedures;
import com.xiaomitool.v2.procedure.fetch.AfhFetch;
import com.xiaomitool.v2.process.LineScanner;
import com.xiaomitool.v2.process.ProcessRunner;
import com.xiaomitool.v2.resources.ResourcesManager;
import com.xiaomitool.v2.utility.utils.InetUtils;
import com.xiaomitool.v2.xiaomi.XiaomiKeystore;
import com.xiaomitool.v2.xiaomi.XiaomiProcedureException;
import com.xiaomitool.v2.xiaomi.unlock.UnlockCommonRequests;
import org.apache.commons.codec.digest.DigestUtils;


import java.io.File;
import java.io.IOException;


public class Main2 {
    private static final String APP_KEY = "eGlhb21pX3RoaXJkX3BhcnR5";
    private static final String BASE_URL = "https://third-api.amemv.com/aweme/v1/third/hot/music/";
    private static StringBuilder builder = null;

    public static final String KEY_ACCESS_KEY = "access_key";
    public static final String KEY_APP_ID = "aid";
    public static final String KEY_APP_LANGUAGE = "app_language";
    public static final String KEY_APP_VERSION = "app_version";
    public static final String KEY_CHANNEL = "channel";
    public static final String KEY_DEVICE_ID = "device_id";
    public static final String KEY_DEVICE_PLATFORM = "device_platform";
    public static final String KEY_DEVICE_TYPE = "device_type";
    public static final String KEY_EFFECT_IDS = "effect_ids";
    public static final String KEY_PANEL = "panel";
    public static final String KEY_REGION = "region";
    public static final String KEY_SDK_VERSION = "sdk_version";
    public static final String KEY_SYS_LANGUAGE = "language";
    public static final String KEY_TYPE = "type";
    public static final String KEY_VERSION = "version";



    private static final String ACCESS_KEY = "f5c61e00bf9a11e79515bdb2ca03e788";
    private static final String APP_VERSION = "7.5.0";
   // private static final String BASE_URL = "https://effect.snssdk.com/effect/api/v3/effects";
    private static final String DEVICE_ID = "123456";
    private static final String DEVICE_TYPE = "Xiaomi";
    private static final String PLATFORM = "android";
    private static final String SDK_VERSION = "3.0.1";

    public static void a(String str, String str2) {

        addParam("app_version", APP_VERSION);
        addParam("device_id", DEVICE_ID);
        addParam(KEY_ACCESS_KEY, ACCESS_KEY);
        addParam(KEY_SDK_VERSION, SDK_VERSION);
        addParam("channel", str);
        addParam(KEY_DEVICE_PLATFORM, PLATFORM);
        addParam(KEY_DEVICE_TYPE, DEVICE_TYPE);
        addParam(KEY_PANEL, str2);
    }



    public static void main(String[] args) throws IOException {
        Log.debug(ResourcesManager.getFastbootPath().getFileName().toString());
    }

    private static void addParam(String key, String value){
        if (builder == null){
            builder = new StringBuilder(BASE_URL).append("?");
        }
        if (!builder.toString().endsWith("?")){
            builder.append("&");
        }
        builder.append(key).append("=").append(InetUtils.urlEncode(value));
    }
}