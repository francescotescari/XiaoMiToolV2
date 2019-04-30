package com.xiaomitool.v2.utility.utils;

import com.xiaomitool.v2.crypto.AES;
import com.xiaomitool.v2.crypto.Hash;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.resources.ResourcesConst;
import com.xiaomitool.v2.resources.ResourcesManager;

import com.xiaomitool.v2.utility.NotNull;
import com.xiaomitool.v2.xiaomi.XiaomiKeystore;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsUtils extends HashMap<String, String> {
    public static final String PREF_DOWNLOAD_DIR = "pref_download_dir";
    public static final String PREF_EXTRACT_DIR = "pref_extract_dir";
    public static final String PREF_SAVE_SESSION = "pref_save_session";
    public static final String SESSION_TOKEN = "session_token";
    public static final String PC_ID = "pcId";
    public static final String REGION = "region";
    private static final Path settingsPath = ResourcesManager.getTmpPath().resolve("settings.app");
    private static boolean loaded = false, toSave = false;
    private static final SettingsUtils instance = new SettingsUtils();

    public static void load(){
        if (loaded){
            return;
        }
        loaded = true;
        if (Files.exists(settingsPath) && Files.isRegularFile(settingsPath)){
            try {
                loadFileSettings(settingsPath);

            } catch (IOException e) {
                Log.error("Failed to load settings file: "+e.getMessage());
            }
        }
    }

    private static void loadFileSettings(@NotNull Path file) throws IOException {
        String content = FileUtils.readAll(file.toFile());
        String[] lines = content.split("\\n");
        Pattern p  = Pattern.compile("^([^=]+)=(.*)$");
        for (String line : lines){
            line = line.trim();
            Matcher m = p.matcher(line);
            if (m.matches()){
                String val = m.group(2);
                if (!"null".equals(val)) {
                    instance.put(m.group(1), val);
                }
            }
        }
    }
    public static Path getDownloadPath(){
        String path = instance.get(PREF_DOWNLOAD_DIR);
        if (path != null){
            return Paths.get(path);
        }
        return ResourcesManager.getTmpPath();
    }
    public static void saveOpt(String key, String value){
        toSave = true;
        instance.put(key,value);
    }
    public static Path getExtractPath(){
        String path = instance.get(PREF_EXTRACT_DIR);
        if (path != null){
            return Paths.get(path);
        }
        return ResourcesManager.getTmpPath();
    }


    public static SettingsUtils getInstance() {
        return instance;
    }
    public static String getOpt(String key){
        return instance.get(key);
    }
    public static File getDownloadFile(String downloadUrl){
        load();
        String name = FilenameUtils.getName(downloadUrl);
        Log.debug(downloadUrl);
        return getDownloadPath().resolve(name).toFile();
    }
    public static File getExtractFile(File toExtract, int index){
        load();
        String basename = FilenameUtils.getBaseName(toExtract.getAbsolutePath());
        return getExtractPath().resolve(basename+(index == 0 ? ""  : "_"+index)).toFile();
    }

    public static final String lineSeparator = System.lineSeparator();
    public static final String fileSeparator = File.separator;
    public static void unset(String key){
        instance.remove(key);
    }
    public static void save(){
        if (!toSave){
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : instance.entrySet()){
            if (entry.getKey().startsWith("_")){
                continue;
            }
            builder.append(entry.getKey()).append('=').append(entry.getValue()).append('\n');
        }
        String data = builder.toString().trim();
        try {
            FileUtils.writeAll(settingsPath.toFile(), data);
        } catch (IOException e) {
            Log.error("Failed to save settings file: "+e.getMessage());
        }
    }
    public static void saveOptEncrpyted(String key, String value){
        String txtKey = StrUtils.randomWord(16);
        String md5 = Hash.md5Hex(txtKey);
        String encKey = md5.substring(0,16);
        String encIV = md5.substring(16,32);
        String encrypted;
        try {

            encrypted = Base64.getEncoder().encodeToString(AES.aes128cbc_encrypt(encKey.getBytes(StandardCharsets.ISO_8859_1),encIV.getBytes(StandardCharsets.ISO_8859_1),value.getBytes(ResourcesConst.interalCharset())));
        } catch (Exception e) {
            Log.error("Failed to save encrypted data: "+e.getMessage());
            return;
        }
        saveOpt(key, encrypted);
        saveOpt(key+".key",txtKey);
    }
    public static String getOptDecrypted(String key){
        String txtKey = getOpt(key+".key");
        if (txtKey == null){
            return null;
        }
        String md5 = Hash.md5Hex(txtKey);
        String encKey = md5.substring(0,16);
        String encIV = md5.substring(16,32);
        String decrypted, encrypted = getOpt(key);
        if (encrypted == null){
            return null;
        }
        try {
            decrypted = new String(AES.aes128cbc_decrypt(encKey.getBytes(StandardCharsets.ISO_8859_1),encIV.getBytes(StandardCharsets.ISO_8859_1),Base64.getDecoder().decode(encrypted)));
        } catch (Exception e) {
            Log.error("Failed to get encrypted data: "+e.getMessage());
            return null;
        }
        return decrypted;
    }
    public static enum Region {
        EU(LRes.REG_EUROPE,"eu","reg_europe.png","_eea_global","eea"),
        INDIA(LRes.REG_INDIA,"india","reg_india.png","_in_global","in"),
        CN(LRes.REG_CHINA,"cn","reg_china.png","","cn"),
        RUSSIA(LRes.REG_RUSSIA,"russia","reg_russia.png","_ru_global","global"),
        GLOBAL(LRes.REG_OTHER,"global","reg_global.png","_global","global"),
        OTHER(LRes.REG_OTHER, "other", "reg_global.png","","global");
        private LRes lRes;
        private final String toString, drawRes, suffix, fastboot_value;
        private Region(LRes lRes, String toString, String drawRes, String suffix, String fastboot_value){
            this.lRes = lRes;
            this.toString = toString;
            this.drawRes = drawRes;
            this.suffix = suffix;
            this.fastboot_value = fastboot_value;
        }

        public String getFastbootValue() {
            return fastboot_value;
        }

        public String getSuffix() {
            return suffix;
        }

        public String toHuman(){
            return lRes.toString();
        }
        @Override
        public String toString(){
            return toString;
        }
        public static Region fromString(String code){
            for (Region region : Region.values()){
                if (region.toString().equals(code)){
                    return region;
                }
            }
            return null;
        }

        public String getDrawable() {
            return drawRes;
        }
    }

    private static Region selectedRegion = null;
    public static Region getRegion(){
        if (selectedRegion == null){
            String code = instance.get(REGION);
            if (code != null){
                Region region = Region.fromString(code);
                selectedRegion = region;
            }
        }
        return selectedRegion;
    }

    public static void setRegion(Region region){
        selectedRegion = region;
        instance.put(REGION, region.toString());
    }

    public static String requirePCId(){
        String pcId = instance.get(PC_ID);
        if (StrUtils.isNullOrEmpty(pcId) || "null".equals(pcId)){
            pcId = XiaomiKeystore.getInstance().getPcId();
            if (StrUtils.isNullOrEmpty(pcId)){
                XiaomiKeystore.getInstance().setDeviceId(XiaomiKeystore.generateDeviceId());
                pcId = XiaomiKeystore.getInstance().getPcId();
            }
        }
        return pcId;
    }
    public static String requireHashedPCId(){
        return Hash.sha1Hex(requirePCId()).substring(20);
    }

    public static boolean isGlobalRegion(){
        return !Region.CN.equals(getRegion());
    }

}
