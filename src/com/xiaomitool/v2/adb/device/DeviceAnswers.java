package com.xiaomitool.v2.adb.device;

import com.xiaomitool.v2.adb.AdbCommons;
import com.xiaomitool.v2.crypto.Hash;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.process.AdbRunner;
import com.xiaomitool.v2.utility.YesNoMaybe;
import com.xiaomitool.v2.utility.utils.StrUtils;
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

    private YesNoMaybe isInTwrpRecoveryInternal(){
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

    public YesNoMaybe isInTwrpRecovery(){
        return setAnswer(HAS_TWRP,isInTwrpRecovery(3));
    }
    private  YesNoMaybe isInTwrpRecovery(int trials){
        YesNoMaybe result = isInTwrpRecoveryInternal();
        while (YesNoMaybe.MAYBE.equals(result) && trials > 0){
            --trials;
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Log.debug(e);
            }
            result = isInTwrpRecoveryInternal();
        }
        if (YesNoMaybe.YES.equals(result)){
            setAnswer(HAS_TWRP, YesNoMaybe.YES);
        }
        return result;

    }
    public UnlockStatus getUnlockStatus() {
        String product = (String) device.getDeviceProperties().get(DeviceProperties.CODENAME);
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

}
