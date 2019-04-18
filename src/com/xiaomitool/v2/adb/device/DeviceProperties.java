package com.xiaomitool.v2.adb.device;

import com.xiaomitool.v2.adb.AdbCommons;
import com.xiaomitool.v2.adb.AdbUtils;
import com.xiaomitool.v2.adb.FastbootCommons;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.utility.YesNoMaybe;
import com.xiaomitool.v2.utility.utils.ThreadUtils;
import com.xiaomitool.v2.xiaomi.XiaomiUtilities;
import com.xiaomitool.v2.xiaomi.miuithings.Branch;
import com.xiaomitool.v2.xiaomi.miuithings.SerialNumber;
import com.xiaomitool.v2.xiaomi.miuithings.UnlockStatus;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class DeviceProperties {
    public static final String X_SCREEN_WIDTH = "xmt.screen_x";
    public static final String X_SCREEN_HEIGHT = "xmt.screen_y";
    public static final String X_HAS_SU = "xmt.has_su";
    public static final String X_LOCKSTATUS = "xmt.lockstatus";
    public static final String CODENAME = "ro.product.device";
    public static final String FULL_VERSION = "ro.build.version.incremental";
    public static final String X_SERIAL_NUMBER = "xmt.serial_number";
    public static final String X_BRANCH = "xmt.branch";
    public static final String LANGUAGE = "ro.product.locale.language";
    public static final String REGION = "ro.miui.region";
    public static final String ROMZONE = "ro.rom.zone";
    public static final String CODEBASE = "ro.build.version.release";
    public static final String BRAND = "ro.product.brand";
    public static final String MODEL = "ro.product.model";
    public static final String FASTBOOT_PRODUCT = "product";


    class AdbProperties extends Properties {
        @Override
        protected boolean parseInternal() {
            findProps();
            findScreenDimension();
            findSu();
            findUnlockStatusDevice();
            findSerialNum();
            String product = this.get(CODENAME,"").toString();
            if (product.isEmpty()){
                return false;
            }
            findFeatures(product,false);
            Log.debug("Adb properties parsed");

            return true;
        }
        private void findProps(){
            List<String> propList = AdbCommons.getProps(serial);
            if (propList == null){
                Log.warn("Failed to get adb device properties via getprop");
                return;
            }
            this.putAll(AdbUtils.parseGetProp(propList));
        }
        private void findScreenDimension(){
            String res = AdbCommons.command("shell wm size",serial);
            if (res == null){
                Log.warn("Failed to get screen dimensions");
                return;
            }
            int[] screenDims =  AdbUtils.parseWmSize(res);
            if (screenDims != null){
                this.put(X_SCREEN_WIDTH, screenDims[0]);
                this.put(X_SCREEN_HEIGHT,screenDims[1]);
            }
        }
        private void findSu(){
            boolean hasSU = false;
            if (AdbCommons.fileExists("/system/xbin/su", serial)){
                hasSU = true;
            } else {
                hasSU = AdbCommons.fileExists("/system/bin/su", serial);
            }
            this.put(X_HAS_SU,hasSU);
        }
        private void findUnlockStatusDevice(){

            this.put(X_LOCKSTATUS,DeviceGroups.hasUnlockedBootloader(getCodename(true)) ? UnlockStatus.UNLOCKED : UnlockStatus.fromString((String) this.get("ro.secureboot.lockstate")));
        }
        private boolean findFeatures(String product, boolean usePull){
            if (product.contains(" ")) {
                return false;
            }
            String file = "/system/etc/device_features/" + product + ".xml";
            if (usePull) {

                File features = AdbCommons.simplePull(serial, file, "features.xml");
                if (features == null) {
                    return false;
                }
                try {
                    HashMap<String, String> props = AdbUtils.parseFeaturesFile(features);
                    if (props.size() == 0) {
                        throw new Exception("returned hashmap is empty");
                    }
                    this.putAll(props);
                    return true;
                } catch (Exception e) {
                    Log.warn("Cannot parse features file: " + e.getMessage());
                    return false;
                }
            } else {
                String features = AdbCommons.cat(serial, file);
                if (features == null) {
                    return findFeatures(product, true);
                }
                try {
                    HashMap<String, String> props = AdbUtils.parseFeaturesFile(features);
                    if (props.size() == 0) {
                        throw new Exception("Returned hashmap is empty");
                    }
                    this.putAll(props);
                    return true;
                } catch (Exception e) {
                    Log.warn("Cannot parse features file: " + e.getMessage());
                    return findFeatures(product, true);
                }
            }
        }
        private boolean findSerialNum(){
            String sn = AdbCommons.cat(serial, "/proc/serial_num");
            if (sn == null){
                sn = (String) this.get("ro.boot.cpuid",null);
                if (sn == null){
                    return false;
                }
            }
            SerialNumber serial = SerialNumber.fromHexString(sn);
            if (serial != null){
                this.put(X_SERIAL_NUMBER, serial);
                return true;
            }
            return false;
        }
    }
    class FastbootProperties extends Properties {

        @Override
        protected boolean parseInternal() {
            boolean result;
            result = findUnlockState();
            findVars();
            findSerialNumber();
            return result;
        }

        private boolean findVars(){
            boolean result = false;
            HashMap<String, String> getvar = new HashMap<>();
            List<String> allVars = FastbootCommons.getvars(serial);
            if (allVars != null){
                getvar = AdbUtils.parseFastbootVars(allVars);
                if (getvar.size() > 0){
                    this.putAll(getvar);
                    return true;
                }
            }
            String[] toGetVars = new String[]{"product","unlocked","token"};
            for (String var : toGetVars){
                String out = FastbootCommons.getvar(var, serial);
                if (out == null){
                    continue;
                }
                if (out.isEmpty()){
                    continue;
                }
                    this.put(var, out);
                    result = true;

            }
            return result;
        }

        private boolean findSerialNumber(){
            String token = get("token","").toString();
            if (token.isEmpty()){
                return false;
            }
            SerialNumber sn = SerialNumber.fromFastbootToken(token);
            if (sn != null){
                this.put(X_SERIAL_NUMBER, sn);
                return true;
            }
            return false;
        }

        private boolean findUnlockState(){
            try {
                if (DeviceGroups.hasUnlockedBootloader(getCodename(true))){
                    this.put(X_LOCKSTATUS, UnlockStatus.UNLOCKED);
                    return true;
                }
            } catch (Throwable t){
                Log.debug(t.getMessage());
            }
            List<String> output = FastbootCommons.oemDeviceInfo(serial);
            if (output == null){
                return false;
            }
            UnlockStatus status = UnlockStatus.fromString(AdbUtils.parseFastbootOemInfo(output));
            if (status != UnlockStatus.UNKNOWN){
                this.put(X_LOCKSTATUS, status);
                return true;
            }
             output = FastbootCommons.oemLks(serial);
            if (output == null){
                return false;
            }
            status = UnlockStatus.fromString(AdbUtils.parseFastbootOemLks(output));
            if (status != UnlockStatus.UNKNOWN){
                this.put(X_LOCKSTATUS, status);
                return true;
            }
            return false;
        }
    }

    class SideloadProperties extends Properties {

        @Override
        protected boolean parseInternal() {
            findProps();
            return true;
        }

        private boolean findProps() {
            String value;
            value = AdbCommons.raw(serial, "getdevice:");
            if (value != null) {
                this.put(CODENAME, value.trim());
            }
            ThreadUtils.sleepSilently(30);
            value = AdbCommons.raw(serial, "getversion:");
            if (value != null) {
                this.put(FULL_VERSION, value.trim());
            }
            ThreadUtils.sleepSilently(30);
            value = AdbCommons.raw(serial, "getsn:");
            if (value != null) {
                this.put(X_SERIAL_NUMBER, SerialNumber.fromHexString(value));
            }
            ThreadUtils.sleepSilently(30);
            value = AdbCommons.raw(serial, "getcodebase:");
            if (value != null) {
                this.put(CODEBASE, value.trim());
            }
            ThreadUtils.sleepSilently(30);
            value = AdbCommons.raw(serial, "getbranch:");
            if (value != null) {
                this.put(X_BRANCH, Branch.fromCode(value.trim()));
            }
            ThreadUtils.sleepSilently(30);
            value = AdbCommons.raw(serial, "getlanguage:");
            if (value != null) {
                this.put(LANGUAGE, value.trim());
            }
            ThreadUtils.sleepSilently(30);
            value = AdbCommons.raw(serial, "getregion:");
            if (value != null) {
                this.put(REGION, value.trim());
            }
            ThreadUtils.sleepSilently(30);
            value = AdbCommons.raw(serial, "getromzone:");
            if (value != null) {
                this.put(ROMZONE,value.trim());
            }
            return true;
        }
    }

    class RecoveryProperties extends Properties {

        @Override
        protected boolean parseInternal() {
            isTwrp();
            return true;
        }

        private void isTwrp(){
            if (YesNoMaybe.YES.equals(device.getAnswers().isInTwrpRecovery())){
                device.getAnswers().setAnswer(DeviceAnswers.HAS_TWRP, YesNoMaybe.YES);
            }

        }
    }

    public Object get(String key, Object defaultReturn){
        Object res = get(key);
        return res == null ? defaultReturn : res;
    }
    public Object get(String key){
        Object x = sideloadProperties.get(key);
        if (x == null){
            x = adbProperties.get(key);
        }
        if (x == null){
            x = fastbootProperties.get(key);
        }
        if (x == null){
            x = recoveryProperties.get(key);
        }
        return x;
    }



    private final Properties adbProperties, fastbootProperties, sideloadProperties, recoveryProperties;
    private String serial;
    private Device device;

    public DeviceProperties(Device device){
        this.device = device;
        this.serial = device.getSerial();
        adbProperties = new AdbProperties();
        fastbootProperties = new FastbootProperties();
        sideloadProperties = new SideloadProperties();
        recoveryProperties = new RecoveryProperties();
    }

    public Properties getAdbProperties() {
        return adbProperties;
    }

    public Properties getFastbootProperties() {
        return fastbootProperties;
    }

    public Properties getSideloadProperties() {
        return sideloadProperties;
    }

    public Properties getRecoveryProperties() {
        return recoveryProperties;
    }

    public String getCodename(boolean stripped){
        String codename = (String) this.get(CODENAME);
        return stripped ? XiaomiUtilities.stripCodename(codename) : codename;
    }
}
