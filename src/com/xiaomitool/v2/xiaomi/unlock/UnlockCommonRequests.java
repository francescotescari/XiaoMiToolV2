package com.xiaomitool.v2.xiaomi.unlock;

import com.xiaomitool.v2.inet.CustomHttpException;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.resources.ResourcesConst;
import com.xiaomitool.v2.utility.utils.StrUtils;
import com.xiaomitool.v2.xiaomi.XiaomiKeystore;
import com.xiaomitool.v2.xiaomi.XiaomiProcedureException;
import org.json.JSONObject;

import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class UnlockCommonRequests {
    private static final HashMap<Integer, LRes> UNLOCK_CODE_MEANING = buildUnlockCodeMeaning();
    private static final String SID = "miui_unlocktool_client";
    private static final String CLIENT_VERSION = "3.5.1128.45";
    private static final String NONCEV2 = "/api/v2/nonce";
    private static final String USERINFOV3 = "/api/v3/unlock/userinfo";
    private static final String DEVICECLEARV3 = "/api/v2/unlock/device/clear";
    private static final String AHAUNLOCKV3 = "/api/v3/ahaUnlock";
    private static final String FLASHUSERSTATUS = "/api/v2/flash/userStatus";
    private static final String AHAFLASH = "/api/v1/flash/ahaFlash";

    private static String OVERRIDE_CLIENT_VERSION = null;
    private static Map<String, Object> OVERRIDE_UNLOCK_REQUEST = null;

    private static String getClientVersion(){
        if (OVERRIDE_CLIENT_VERSION != null){
            return OVERRIDE_CLIENT_VERSION;
        }
        return CLIENT_VERSION;
    }

    public static void overrideClientVersion(String version){
        OVERRIDE_CLIENT_VERSION = version;
    }

    public static void overrideUnlockOptions(Map<String, Object> overrideFields){
        OVERRIDE_UNLOCK_REQUEST = overrideFields;
    }

    public static String getUnlockCodeMeaning(int code, JSONObject object) {
        LRes languageResource = UNLOCK_CODE_MEANING.get(code);
        if (languageResource == null) {
            return LRes.UNL_UNKNOWN_ERROR.toString(code);
        }
        String toReturn;
        if (code == 20036) {
            int hours = -1;
            {
                try {
                    hours = object.getJSONObject("data").getInt("waitHour");
                } catch (Throwable t) {
                    Log.error("Failed to get waitHour for unlock");
                }
                if (hours >= 0) {
                    int days = hours / 24;
                    int leftHours = hours % 24;
                    toReturn = languageResource.toString(days, leftHours);
                } else {
                    toReturn = LRes.UNL_ERR_20036_NOHOURS.toString();
                }
            }
        } else {
            toReturn = languageResource.toString();
        }
        return toReturn;
    }

    public static String getErrorDescription(JSONObject data){
        //TODO
        return "";
    }

    private static HashMap<Integer, LRes> buildUnlockCodeMeaning() {
        HashMap<Integer, LRes> map = new HashMap<>();
        map.put(10000, LRes.UNL_ERR_10000);
        map.put(10001, LRes.UNL_ERR_10001);
        map.put(10002, LRes.UNL_ERR_10002);
        map.put(10003, LRes.UNL_ERR_10003);
        map.put(10004, LRes.UNL_ERR_10004);
        map.put(10005, LRes.UNL_ERR_10005);
        map.put(10006, LRes.UNL_ERR_10006);
        map.put(20030, LRes.UNL_ERR_20030);
        map.put(20031, LRes.UNL_ERR_20031);
        map.put(20032, LRes.UNL_ERR_20032);
        map.put(20033, LRes.UNL_ERR_20033);
        map.put(20034, LRes.UNL_ERR_20034);
        map.put(20035, LRes.UNL_ERR_20035);
        map.put(20036, LRes.UNL_ERR_20036);
        map.put(20037, LRes.UNL_ERR_20037);
        map.put(20041, LRes.UNL_ERR_20041);
        return map;
    }

    public static String nonceV2() throws XiaomiProcedureException, CustomHttpException {
        UnlockRequest request = new UnlockRequest(NONCEV2);
        request.addParam("r", new String(StrUtils.randomWord(16).toLowerCase().getBytes(ResourcesConst.interalCharset()), ResourcesConst.interalCharset()));
        request.addParam("sid", SID);
        return request.exec();
    }

    public static String userInfo() throws XiaomiProcedureException, CustomHttpException {
        XiaomiKeystore keystore = XiaomiKeystore.getInstance();
        UnlockRequest request = new UnlockRequest(USERINFOV3);
        HashMap<String, String> pp = new LinkedHashMap<>();
        pp.put("clientId", "1");
        pp.put("clientVersion", getClientVersion());
        pp.put("language", "en");
        pp.put("pcId", keystore.getPcId());
        pp.put("region", "");
        pp.put("uid", keystore.getUserId());
        String data = new JSONObject(pp).toString(3);
        data = Base64.getEncoder().encodeToString(data.getBytes(ResourcesConst.interalCharset()));
        request.addParam("data", data);
        request.addNonce();
        request.addParam("sid", SID);
        return request.exec();
    }

    public static String deviceClear(String product) throws XiaomiProcedureException, CustomHttpException {
        XiaomiKeystore keystore = XiaomiKeystore.getInstance();
        UnlockRequest request = new UnlockRequest(DEVICECLEARV3);
        HashMap<String, String> pp = new LinkedHashMap<>();
        pp.put("clientId", "1");
        pp.put("clientVersion", getClientVersion());
        pp.put("language", "en");
        pp.put("pcId", keystore.getPcId());
        pp.put("product", product);
        pp.put("region", "");
        String data = new JSONObject(pp).toString(3);
        data = Base64.getEncoder().encodeToString(data.getBytes(ResourcesConst.interalCharset()));
        request.addParam("appId", "1");
        request.addParam("data", data);
        request.addNonce();
        request.addParam("sid", SID);
        return request.exec();
    }

    public static String flashUserStatus() throws XiaomiProcedureException, CustomHttpException {
        XiaomiKeystore keystore = XiaomiKeystore.getInstance();
        UnlockRequest request = new UnlockRequest(FLASHUSERSTATUS);
        HashMap<String, String> pp = new HashMap<>();
        pp.put("clientId", "qcomFlash");
        pp.put("clientVersion", getClientVersion());
        pp.put("pcId", keystore.getPcId());
        JSONObject object = new JSONObject(pp);
        String data = object.toString();
        data = Base64.getEncoder().encodeToString(data.getBytes(ResourcesConst.interalCharset()));
        request.addParam("data", data);
        request.addNonce();
        request.addParam("sid", SID);
        return request.exec();
    }

    public static String ahaFlash(String flashToken) throws XiaomiProcedureException, CustomHttpException {
        XiaomiKeystore keystore = XiaomiKeystore.getInstance();
        UnlockRequest request = new UnlockRequest(AHAFLASH);
        HashMap<String, String> pp = new HashMap<>();
        pp.put("pcId", keystore.getPcId());
        pp.put("clientVersion", getClientVersion());
        pp.put("clientId", "qcomFlash");
        pp.put("flashToken", flashToken);
        String data = new JSONObject(pp).toString();
        data = Base64.getEncoder().encodeToString(data.getBytes(ResourcesConst.interalCharset()));
        request.addParam("data", data);
        request.addNonce();
        request.addParam("sid", SID);
        return request.exec();
    }

    public static String ahaUnlock(String token, String product, String boardVersion, String deviceName, String socId) throws XiaomiProcedureException, CustomHttpException {
        if (StrUtils.isNullOrEmpty(product)) {
            throw new XiaomiProcedureException("Invalid input argument: null product");
        }
        XiaomiKeystore keystore = XiaomiKeystore.getInstance();
        UnlockRequest request = new UnlockRequest(AHAUNLOCKV3);
        HashMap<String, String> p2 = new LinkedHashMap<>();
        p2.put("boardVersion", boardVersion);
        p2.put("deviceName", deviceName);
        p2.put("product", product);
        p2.put("socId", socId);
        HashMap<String, Object> pp = new LinkedHashMap<>();
        pp.put("clientId", "2");
        pp.put("clientVersion", getClientVersion());
        pp.put("deviceInfo", p2);
        pp.put("deviceToken", token);
        pp.put("language", "en");
        pp.put("operate", "unlock");
        pp.put("pcId", keystore.getPcId());
        pp.put("region", "");
        pp.put("uid", keystore.getUserId());
        if (OVERRIDE_UNLOCK_REQUEST != null){
            for (Map.Entry<String, Object> entry : OVERRIDE_UNLOCK_REQUEST.entrySet()){
                pp.put(entry.getKey(), entry.getValue());
            }
        }
        String data = StrUtils.map2json(pp, 3);
        data = Base64.getEncoder().encodeToString(data.getBytes(ResourcesConst.interalCharset()));
        request.addParam("appId", "1");
        request.addParam("data", data);
        request.addNonce();
        request.addParam("sid", SID);
        return request.exec();
    }
}
