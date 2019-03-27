package com.xiaomitool.v2.xiaomi;

import com.xiaomitool.v2.crypto.Hash;
import com.xiaomitool.v2.inet.CustomHttpException;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.UUID;

public class XiaomiKeystore {
    public static void clear(){
        instance = new XiaomiKeystore();
    }
    private static XiaomiKeystore instance;
    public static String KEY_MIOTAV3 = "bWl1aW90YXZhbGlkZWQxMQ==";
    private HashMap<String, XiaomiServiceEntry> serviceMap = new HashMap<>();
    private String userId;
    private String passToken;
    private String deviceId;
    private String pcId;
    public String getUserId() {
        return userId;
    }

    public String getPassToken() {
        return passToken;
    }
    public String getDeviceId(){

         if (this.deviceId == null){
             this.deviceId = generateDeviceId();
         }
         return this.deviceId;
    }


    public void setCredentials(String userId, String passToken, String deviceId){
        this.userId = userId;
        this.passToken = passToken;
        this.deviceId = deviceId;
    }
    public void setCredentials(String userId, String passToken){
        setCredentials(userId,passToken,generateDeviceId());
    }

    public static String generateDeviceId(){
        return "wb_"+UUID.randomUUID().toString();
    }

    public String[] requireServiceKeyAndToken(String sid) throws XiaomiProcedureException, CustomHttpException {
        XiaomiServiceEntry entry = serviceMap.get(sid);
        if (entry == null){
            entry = new XiaomiServiceEntry(sid, this);
            serviceMap.put(sid, entry);
        }
        return entry.requireSSandST();
    }
    public HashMap<String, String> requireServiceCookies(String sid) throws XiaomiProcedureException, CustomHttpException {
        XiaomiServiceEntry entry = serviceMap.get(sid);
        if (entry == null){
            entry = new XiaomiServiceEntry(sid, this);
            entry.requireSSandST();
            serviceMap.put(sid, entry);
        }
        return entry.getCookies();

    }
    public boolean isLogged(){
        return this.getUserId() != null && this.getPassToken() != null;
    }


    public static XiaomiKeystore getInstance(){
        if (instance == null){
            instance = new XiaomiKeystore();
        }
        return instance;
    }
    public String getPcId(){
       if (pcId == null){
           pcId = DigestUtils.md5Hex(getDeviceId());
       }
       return pcId;
    }
    public String getJson(){
        if (isEmpty()){
            return null;
        }
        JSONObject object = new JSONObject();
        object.put("passToken",passToken);
        object.put("userId",userId);
        object.put("deviceId",deviceId);
        return object.toString();
    }
    public void setCredentials(JSONObject object){
        passToken = object.optString("passToken",null);
        userId = object.optString("userId",null);
        deviceId = object.optString("deviceId",null);
    }
    public boolean isEmpty(){
        return (passToken == null || userId == null || deviceId == null || passToken.isEmpty() || userId.isEmpty());
    }

    public void setDeviceId(String pcId) {
        this.pcId = pcId;
    }

}
