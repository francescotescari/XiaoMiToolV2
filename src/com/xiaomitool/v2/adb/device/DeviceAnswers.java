package com.xiaomitool.v2.adb.device;

import com.xiaomitool.v2.adb.AdbCommons;
import com.xiaomitool.v2.crypto.Hash;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.process.AdbRunner;
import com.xiaomitool.v2.rom.MiuiRom;
import com.xiaomitool.v2.utility.YesNoMaybe;
import com.xiaomitool.v2.utility.utils.StrUtils;
import com.xiaomitool.v2.utility.utils.ThreadUtils;
import com.xiaomitool.v2.xiaomi.miuithings.Branch;
import com.xiaomitool.v2.xiaomi.miuithings.MiuiVersion;
import com.xiaomitool.v2.xiaomi.miuithings.SerialNumber;
import com.xiaomitool.v2.xiaomi.miuithings.UnlockStatus;

import java.util.HashMap;

public class DeviceAnswers {
    public static final String HAS_TWRP = "has_twrp_recovery";

    private Device device;

     DeviceAnswers(Device device){
        this.device = device;
    }

    private final HashMap<String, YesNoMaybe> savedAnswers = new HashMap<>();
     public YesNoMaybe setAnswer(String key, YesNoMaybe answer){
         this.savedAnswers.put(key, answer);
         return answer;
     }

    private synchronized YesNoMaybe isInTwrpRecoveryInternal(){
         if (Device.Status.OFFLINE.equals(device.getStatus()) || !device.isConnected()){
             ThreadUtils.sleepSilently(1000);
             return YesNoMaybe.MAYBE;
         }
        if (!Device.Status.RECOVERY.equals(device.getStatus())){
            return YesNoMaybe.NO;
        }
        String random = StrUtils.randomWord(8).toLowerCase();
        String key_test = "xmt_rand";

        AdbRunner runner = AdbCommons.runner("shell twrp set "+key_test+" "+random,device.getSerial(),6);
        if (runner.getExitValue() != 0){
            return YesNoMaybe.MAYBE;
        }
        String output = runner.getOutputString();
        if (output == null){
            return YesNoMaybe.NO;
        }
        if (output.toLowerCase().contains("twrp does not appear to be running")){
            Log.warn("Twrp does not appear to be running");
            return YesNoMaybe.MAYBE;
        }
        runner = AdbCommons.runner("shell twrp get "+key_test,device.getSerial(),6);
        if (runner.getExitValue() != 0){
            return YesNoMaybe.MAYBE;
        }
        String output2 = runner.getOutputString();
        if (output2 == null){
            return YesNoMaybe.NO;
        }
        return output.contains(random) ? YesNoMaybe.YES : YesNoMaybe.NO;
    }

    public YesNoMaybe hasTwrpRecovery(){
         return getAnswer(HAS_TWRP);
    }

    public YesNoMaybe getAnswer(String id){
         return this.savedAnswers.get(id);
    }

    public synchronized YesNoMaybe isInTwrpRecovery(){
        return setAnswer(HAS_TWRP,isInTwrpRecovery(3));
    }
    private   YesNoMaybe isInTwrpRecovery(int trials){
        YesNoMaybe result = isInTwrpRecoveryInternal();
        while (YesNoMaybe.MAYBE.equals(result) && --trials > 0){
            try {
                Thread.sleep(1500);
                DeviceManager.waitRefresh();
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Log.debug(e);
            }
            result = isInTwrpRecoveryInternal();
        }
        Log.info("IsInTwrp recovery result: "+result);
        if (YesNoMaybe.YES.equals(result)){
            setAnswer(HAS_TWRP, YesNoMaybe.YES);
        }
        return result;

    }
    public UnlockStatus getUnlockStatus() {
        String product = device.getDeviceProperties().getCodename(true);
        if (DeviceGroups.hasUnlockedBootloader(product)) {
            return UnlockStatus.UNLOCKED;
        }
        UnlockStatus status = (UnlockStatus) device.getDeviceProperties().getFastbootProperties().get(DeviceProperties.X_LOCKSTATUS);
        if (status == null) {
            status = (UnlockStatus) device.getDeviceProperties().getAdbProperties().get(DeviceProperties.X_LOCKSTATUS);
        }
        return status == null ? UnlockStatus.UNKNOWN : status;
    }
    private static final String IS_MANUAL_MODE = "manualmode";
    public YesNoMaybe isRebootManualMode(){
         return getAnswer(IS_MANUAL_MODE);
    }
    public void setRebootManualMode(YesNoMaybe answer){
        setAnswer(IS_MANUAL_MODE, answer);
    }
    private static final String NEED_DEVICE_DEBUG = "needdevicedebug";
    public YesNoMaybe isNeedDeviceDebug(){
        return getAnswer(NEED_DEVICE_DEBUG);
    }
    public void setNeedDeviceDebug(YesNoMaybe answer){
        setAnswer(NEED_DEVICE_DEBUG, answer);
    }

    public MiuiRom.Specie getCurrentSpecie() {
        String product =  device.getDeviceProperties().getCodename(false);
        MiuiVersion version = MiuiVersion.fromObject(device.getDeviceProperties().get(DeviceProperties.FULL_VERSION));
        Branch branch = version == null ? Branch.DEVELOPER : version.getBranch();
        branch = branch == null ? Branch.DEVELOPER : branch;
        return MiuiRom.Specie.fromStringBranch(product,branch);
    }

    public static final String HAS_STOCK_MIUI = "hasStockMiui";

    public void updateStatus(Device.Status status){
        if (Device.Status.SIDELOAD.equals(status)){
            this.setAnswer(HAS_TWRP, YesNoMaybe.NO);
        } else if (Device.Status.DEVICE.equals(status)){
            if (YesNoMaybe.YES.equals(getAnswer(HAS_STOCK_MIUI)) && YesNoMaybe.YES.equals(this.hasTwrpRecovery())){
                this.setAnswer(HAS_TWRP, YesNoMaybe.MAYBE);
            }
            setNeedDeviceDebug(YesNoMaybe.NO);
        } else if (Device.Status.UNAUTHORIZED.equals(status)){
            setNeedDeviceDebug(YesNoMaybe.NO);
        }
    }

    public SerialNumber getSerialNumber() {
        DeviceProperties properties = device.getDeviceProperties();
        SerialNumber sn;
        sn = (SerialNumber) properties.getSideloadProperties().get(DeviceProperties.X_SERIAL_NUMBER);
        if (sn != null && sn.isValid()){return sn;}
        sn = (SerialNumber) properties.getAdbProperties().get(DeviceProperties.X_SERIAL_NUMBER);
        if (sn != null && sn.isValid()){return sn;}
        sn = (SerialNumber) properties.getFastbootProperties().get(DeviceProperties.X_SERIAL_NUMBER);
        return sn;
    }

    public static final String NEED_WIPE_DATA = "need_wipe_data";
    public YesNoMaybe isTwrpDataEncrypted(){
        return isTwrpDataEncrypted(false);
    }

    private YesNoMaybe isTwrpDataEncrypted(boolean mount_data) {
        if (mount_data){
            AdbCommons.adb_shell("twrp mount data", device.getSerial(), 5);
        }
        String output = AdbCommons.adb_shell("(ls -l /data || echo no_line_first) && (ls -l /sdcard || echo no_line_again)", device.getSerial(), 4);
        boolean fail1 = false, fail2 = false;
        if (output == null){
            return YesNoMaybe.MAYBE;
        }
        output = output.trim();
        String[] lines = output.split("\n");
        for (String line : lines){
            if (line.contains("no_line_first")){
                fail1 = true;
            } else if (line.contains("no_line_again")){
                fail2 = true;
            }
        }
        if (fail1 && fail2){
            if (mount_data){
                return YesNoMaybe.MAYBE;
            } else {
                return isTwrpDataEncrypted(true);
            }
        }
        int size = lines.length;
        if (fail1 || fail2){
            size--;
        }

        if (size == 0){
            return YesNoMaybe.YES;
        } else if (size > 3){
            return YesNoMaybe.NO;
        }
        return YesNoMaybe.MAYBE;
    }

    public YesNoMaybe isWipeDataNeeded() {
        if ("unencrypted".equalsIgnoreCase((String) device.getDeviceProperties().getAdbProperties().get("ro.crypto.state"))){
            return YesNoMaybe.NO;
        }
        return getAnswer(NEED_WIPE_DATA);
    }
}
