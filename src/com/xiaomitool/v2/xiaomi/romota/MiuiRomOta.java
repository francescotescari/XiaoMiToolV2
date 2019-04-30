package com.xiaomitool.v2.xiaomi.romota;

import com.xiaomitool.v2.crypto.Hash;
import com.xiaomitool.v2.inet.CustomHttpException;
import com.xiaomitool.v2.inet.EasyHttp;
import com.xiaomitool.v2.inet.EasyResponse;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.rom.MiuiRom;
import com.xiaomitool.v2.rom.MiuiTgzRom;
import com.xiaomitool.v2.rom.MiuiZipRom;
import com.xiaomitool.v2.rom.RomException;
import com.xiaomitool.v2.utility.NotNull;
import com.xiaomitool.v2.utility.utils.InetUtils;
import com.xiaomitool.v2.utility.utils.SettingsUtils;
import com.xiaomitool.v2.utility.utils.StrUtils;
import com.xiaomitool.v2.xiaomi.XiaomiCrypto;
import com.xiaomitool.v2.xiaomi.XiaomiKeystore;
import com.xiaomitool.v2.xiaomi.XiaomiProcedureException;
import com.xiaomitool.v2.xiaomi.XiaomiUtilities;
import com.xiaomitool.v2.xiaomi.miuithings.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import com.xiaomitool.v2.rom.MiuiRom.Kind;


public class MiuiRomOta {

    public static @NotNull HashMap<Kind, MiuiZipRom> otaV3_request(RequestParams params) throws XiaomiProcedureException, CustomHttpException {
        return otaV3_request(params, false);
    }

    public static @NotNull HashMap<Kind, MiuiZipRom> otaV3_request(RequestParams params, boolean throwOnCode) throws XiaomiProcedureException, CustomHttpException {

        String key = XiaomiKeystore.KEY_MIOTAV3;
        String serviceToken = "";
        int mode = 1;
        String userId=XiaomiKeystore.getInstance().getUserId();
        try {
            String[] tokens = XiaomiKeystore.getInstance().requireServiceKeyAndToken(params.isInternational() ? "miuiota_intl" : "miuiromota");
            mode = 2;
            key = tokens[0];
            serviceToken = tokens[1];

        } catch (XiaomiProcedureException e) {
            if (e.getCode().equals(XiaomiProcedureException.ExceptionCode.NEED_LOGIN)){
                Log.debug("Need login");
            }
        }
        MiuiRom.Specie specie = params.getSpecie();
        boolean international = !SettingsUtils.Region.CN.equals(SettingsUtils.getRegion());
        if (international && specie != null){
            international = !specie.isChinese();
        }

        String url = international ? "https://update.intl.miui.com/updates/miotaV3.php" : "https://update.miui.com/updates/miotaV3.php";



        EasyHttp request = new EasyHttp().url(url);
        if (!serviceToken.isEmpty()){
            request.cookie("serviceToken", InetUtils.urlEncode(serviceToken));
        }
        if (userId != null && !userId.isEmpty()){
            try {
                params.setId(userId);
                String cuserId = XiaomiCrypto.cloudService_encrypt(userId,XiaomiKeystore.KEY_MIOTAV3);
                request.cookie("uid",InetUtils.urlEncode(cuserId));
                request.cookie("s","1");
            } catch (Exception e) {
                throw new XiaomiProcedureException(String.format("[otaV3_request] Encryption of user id failed: %s key=%s", e.getMessage(), XiaomiKeystore.KEY_MIOTAV3));
            }
        }
        String postData;
        String json = null;
        try {
            json = params.buildJson();
        } catch (Exception e) {
            throw new XiaomiProcedureException(e.getMessage());
        }
        Log.info("OTAV3 request: "+json);
        try {
            postData = XiaomiCrypto.cloudService_encrypt(json,key);

        } catch (Exception e) {
            throw new XiaomiProcedureException(String.format("[otaV3_request] Encryption failed: %s key=%s", e.getMessage(), key));
        }
        request.field("q",postData);
        request.field("t",serviceToken);
        request.field("s",mode+"");
        EasyResponse response;

             response = request.exec();

        if (!response.isAllRight()){
            String message = "Ota request response is invalid: ";
            if (response.isCodeRight()){
                message+="empty response";
            } else {
                message+="http code: "+response.getCode();
            }
            throw new XiaomiProcedureException("[otaV3_request] "+message);
        }
        String decrypted;
        try {
             decrypted = XiaomiCrypto.cloudService_decrypt(response.getBody(), key);
        } catch (Exception e) {
            throw  new XiaomiProcedureException("[otaV3_request] Response is invalid: decryption failed: "+e.getMessage());
        }
        Log.debug(decrypted);
        Log.info("OTAV3 response: "+decrypted);
        JSONObject jsonData;
        try {
             jsonData = new JSONObject(decrypted);
        } catch (JSONException e){
            throw new XiaomiProcedureException("[otaV3_request] Json parse failed: "+e.getMessage());
        }

        Kind[] entries = new Kind[]{Kind.LATEST, Kind.CURRENT, Kind.INCREMENTAL, Kind.PACKAGE};
        HashMap<Kind, MiuiZipRom> map = new HashMap<>();
        JSONArray array = jsonData.optJSONArray(Mirrors.DEFAULT_MIRROR_ENTRY);
        Mirrors mirrors = new Mirrors();;
        if (array != null){
            mirrors.setFromJson(array);
        }
        for (Kind entry : entries){
            JSONObject j = jsonData.optJSONObject(entry.toString());
            if (j == null){
                Log.debug("Missing total rom data: "+entry.toString());
                continue;
            }
            try {
                MiuiZipRom rom = parseZipRomFromJson(j, entry, params.getSpecie());
                rom.setMirrors(mirrors);
                map.put(entry, rom);
            } catch (XiaomiProcedureException e){
                Log.warn(e.getMessage());
                continue;
            }

        }
        if (throwOnCode){
            try {
                JSONObject res = jsonData.getJSONObject("Code");
                int code = res.getInt("code");
                String message = res.getString("message");
                if (code != 2000){
                    throw new XiaomiProcedureException("Ota response code is "+code+", description: "+message, XiaomiProcedureException.ExceptionCode.NOT_ALLOWED, message);
                }
            } catch (Throwable t){
                if (t instanceof XiaomiProcedureException){
                    throw (XiaomiProcedureException) t;
                }
                Log.error("Should have check ota response code, but parsing failed: "+t.getMessage());
                if (map.isEmpty() || (params.getPkg() != null && !params.getPkg().isEmpty() && !map.containsKey(Kind.PACKAGE))){
                    throw new XiaomiProcedureException("Failed to get response code: "+t.getMessage(), XiaomiProcedureException.ExceptionCode.NOT_ALLOWED, "Cannot get ota response code");
                }
            }

        }

        return map;

    }

    private static final String OTA_FILENAME = "filename";
    private static final String OTA_VERSION = "version";
    private static final String OTA_CODEBASE = "codebase";
    private static final String OTA_MD5 = "md5";
    private static final String OTA_TOKEN = "Validate";
    private static final String OTA_BRANCH = "branch";
    private static final String OTA_DESCRIPTION_URL = "descriptionUrl";
    private static MiuiZipRom parseZipRomFromJson(JSONObject object, Kind kind, MiuiRom.Specie specie) throws XiaomiProcedureException {
        try {
            String filename = object.getString(OTA_FILENAME);
            String s_version = object.getString(OTA_VERSION);
            String s_codebase = object.getString(OTA_CODEBASE);
            String md5 = object.getString(OTA_MD5);
            String token = object.getString(OTA_TOKEN);
            String branch = object.getString(OTA_BRANCH);
            String descriptionUrl = object.optString(OTA_DESCRIPTION_URL, null);
            return new MiuiZipRom(filename, new MiuiVersion(s_version), Branch.fromCode(branch), new Codebase(s_codebase), md5, token, kind, descriptionUrl, specie);
        } catch (Exception e){
            return new MiuiZipRom(true);
            //throw new XiaomiProcedureException("Missing response rom data: "+e.getMessage());
        }
    }
    private static MiuiTgzRom parseTgzRomFromJson(JSONObject object, Kind kind, MiuiRom.Specie specie) throws XiaomiProcedureException {
        try {
            String filename = object.getString(OTA_FILENAME);
            String s_version = object.getString(OTA_VERSION);
            String s_codebase = object.getString(OTA_CODEBASE);
            String md5 = object.getString(OTA_MD5);
            String descriptionUrl = object.optString(OTA_DESCRIPTION_URL, null);
            return new MiuiTgzRom(filename, new MiuiVersion(s_version), new Codebase(s_codebase), md5, Kind.LATEST, descriptionUrl, specie);
        } catch (Exception e){
            throw new XiaomiProcedureException("Missing response rom data: "+e.getMessage());
        }
    }


    public static void latestTest(RequestParams params) throws XiaomiProcedureException, CustomHttpException {
        String device = params.getModDevice();
        Branch branch = params.getBranch();
        String region = params.getFastbootRegion();
        String n = params.getCarrier();
        String lang = params.getLanguage();
        HashMap<String, String> map = new HashMap<>();
        map.put("d",device);
        map.put("b",branch.toString());
        map.put("r",region);
        map.put("n",n);
        map.put("l",lang);
        JSONObject object = new JSONObject(map);


            String[] tokens = XiaomiKeystore.getInstance().requireServiceKeyAndToken(params.isInternational() ? "miuiota_intl" : "miuiromota");

            String key = tokens[0];
            String serviceToken = tokens[1];
            String userId = XiaomiKeystore.getInstance().getUserId();







        EasyHttp request = null;
        if (userId != null && !userId.isEmpty()){
            try {
                params.setId(userId);
                String cuserId = XiaomiCrypto.cloudService_encrypt(userId,XiaomiKeystore.KEY_MIOTAV3);
                String url = "http://update.miui.com/updates/mi-update-full-romV2.php?uid="+InetUtils.urlEncode(userId)+"&token="+InetUtils.urlEncode(serviceToken)+"&b=X&c=9.0&d=dipper&i=5bca40358906bb0b088e39eba3c95364&l=en_US&security="+key+"&v=MIUI-8.8.30";//
                request = new EasyHttp().url(url);
                request.cookie("uid",InetUtils.urlEncode(cuserId));

            } catch (Exception e) {
                throw new XiaomiProcedureException(String.format("[otaV3_request] Encryption of user id failed: %s key=%s", e.getMessage(), XiaomiKeystore.KEY_MIOTAV3));
            }
        }
        if (!serviceToken.isEmpty()){
           ;
            request.cookie("serviceToken", serviceToken);
        }
        String postData;
        String json = null;
        try {
            json = params.buildJson();
        } catch (Exception e) {
            throw new XiaomiProcedureException(e.getMessage());
        }
        try {
            postData = XiaomiCrypto.cloudService_encrypt(json,XiaomiKeystore.KEY_MIOTAV3);

        } catch (Exception e) {
            throw new XiaomiProcedureException(String.format("[otaV3_request] Encryption failed: %s key=%s", e.getMessage(), key));
        }
        request.field("q",postData);
        request.field("t","");

        EasyResponse response;

        response = request.exec();



    }

    public static MiuiTgzRom latestFastboot_request(RequestParams params) throws XiaomiProcedureException, CustomHttpException {
        String device = params.getModDevice();
        Branch branch = params.getBranch();
        String region = params.getFastbootRegion();
        String n = params.getCarrier();
        String lang = params.getLanguage();
        String url = String.format("http://update.miui.com/updates/miota-fullrom.php?d=%s&b=%s&r=%s&n=%s&l=%s", device, branch.getCode(), region, n, lang);
        String result;
            result = EasyHttp.get(url).getBody();
            Log.debug(result);
        Log.info("OTA Fastboot response: "+result);
        JSONObject json, jsonrom;
        MiuiTgzRom rom;
        try {
             json = new JSONObject(result);
            jsonrom = json.getJSONObject("LatestFullRom");
            rom = parseTgzRomFromJson(jsonrom,Kind.LATEST, params.getSpecie());
        } catch (Exception e){
            throw new XiaomiProcedureException("[latestFastboot_request] Failed to parse json: "+e.getMessage());
        }


        Mirrors mirrors = new Mirrors();
        try {
            JSONArray array = json.getJSONArray(Mirrors.DEFAULT_MIRROR_ENTRY);
            mirrors.setFromJson(array);
        } catch (Exception e){
            Log.warn("Failed to parse mirrors in rom response");
        }
        rom.setMirrors(mirrors);
        rom.setBranch(params.getBranch());
        return rom;
    }


    public static MiuiZipRom latestRecovery_request(RequestParams params) throws XiaomiProcedureException, CustomHttpException {
        String salt = "ZmVjZTk0MzY0ZjY=";
        String device = params.getModDevice();
        String codebase = params.getCodebase().toString();
        if (codebase.length() > 3){
            codebase = codebase.substring(0,3);
        }
        Branch b = params.getBranch();
        b = b == null ? Branch.STABLE : b.getDual();
        String branch =  Branch.STABLE.equals(b) ? "1" : "0";
       String url = "https://update.miui.com/updates/v1/latestverinfo.php";
       EasyResponse response;
        response = new MiuiSaltedRequest(salt).url(url).field("sid","miassistant").field("d",device).field("c",codebase).field("f",branch).exec();
        String body = response.getBody();
        Log.info("OTA Recovery response: "+body);
        Log.debug(body);
        JSONObject obj;
        try {
             obj = new JSONObject(body);
        }catch (JSONException e){
            throw new XiaomiProcedureException("[latestRecovery_request] Json parsing failed: "+e.getMessage());
        }
        int code = obj.optInt("code",0);
        if (code == 0){
            throw new XiaomiProcedureException("[latestRecovery_request] Invalid return code from json: zero or not present");
        }
        String downloadLink = obj.optString("data",null);
        if (downloadLink == null){
            throw new XiaomiProcedureException("[latestRecovery_request] Missing response data in json");
        }
        try {
            return new MiuiZipRom(downloadLink, params.getSpecie());
        } catch (RomException e) {
            throw new XiaomiProcedureException("[latestRecovery_request] Returned download url is malformed or not recognized");
        }
    }

    public static MiuiTgzRom latestFastboot2_request(RequestParams params) throws XiaomiProcedureException, CustomHttpException {
        String device = params.getModDevice();
        Branch branch = params.getBranch();

        String region = params.getFastbootRegion();
        String n = params.getCarrier();
        String url = String.format("https://update.miui.com/updates/v1/fullromdownload.php?d=%s&b=%s&r=%s&n=%s", device, branch.getCode(), region, n), dl;

            dl = InetUtils.getRedirectUrl(url,"http://en.miui.com/a-234.html");
        Log.info("OTA Fastboot2 response: "+dl);
        if (dl == null){
            throw new XiaomiProcedureException("[latestFastboot2_request] Null redirect url for "+url);
        }
        if (!XiaomiUtilities.isFastbootFile(dl)){
            throw new XiaomiProcedureException("[latestFastboot2_request] Redirect url is not a fastboot file");
        }
        try {
            return new MiuiTgzRom(dl, params.getSpecie());
        } catch (RomException e) {
            throw new XiaomiProcedureException("[latestFastboot2_request] Returned download url is malformed or not recognized");
        }


    }

    public static JSONObject deviceNames_request() throws XiaomiProcedureException {
        String salt = "ZjYyYWIxYzNhM2I=";
        String url = "https://update.miui.com/updates/v1/devinfo.php";
        EasyResponse response;
        try {
            response = new MiuiSaltedRequest(salt).url(url).field("sid","miassistant").field("t","1001").exec();
        } catch (CustomHttpException e) {
            throw new XiaomiProcedureException("[deviceNames_request] Request failed: "+e.getMessage(), XiaomiProcedureException.ExceptionCode.CONNECTION_ERROR);
        }
        String body = response.getBody();
        JSONObject obj;
        try {
            obj = new JSONObject(body);
        }catch (JSONException e){
            throw new XiaomiProcedureException("[deviceNames_request] Json parsing failed: "+e.getMessage());
        }
        int code = obj.optInt("code",0);
        if (code == 0){
            return null;
        }

        return obj.optJSONObject("data");
    }

}

class MiuiSaltedRequest extends EasyHttp {
    String salt;
    public MiuiSaltedRequest(String salt){
        this.salt = salt;
    }

    @Override
    public EasyResponse exec() throws CustomHttpException {
        String random = StrUtils.randomWord(16).toLowerCase();
        String sign = Hash.md5Hex(salt+random);
        super.field("r",random).field("s",sign);
        return super.exec();
    }
}