package com.xiaomitool.v2.engine.actions;

import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.adb.device.DeviceProperties;
import com.xiaomitool.v2.adb.device.Properties;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.utility.utils.NumberUtils;
import com.xiaomitool.v2.xiaomi.miuithings.UnlockStatus;
import javafx.application.Platform;
import javafx.scene.text.Text;

public class ActionsUtil {
    public static void setDevicePropertiesText(Device device, Text... texts){
        if (!Platform.isFxApplicationThread()){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    setDevicePropertiesText(device, texts);
                }
            });
            return;
        }
        if (texts.length < 10){
            return;
        }

        DeviceProperties props = device.getDeviceProperties();
        String brand, codename, model, serial, bootStatus, miuiVersion, androidVersion, recoveryAvailable, fastbootAvailable, serialNumber;
        brand = props.get(DeviceProperties.BRAND, LRes.UNKNOWN).toString();
        codename = props.get(DeviceProperties.CODENAME, LRes.UNKNOWN).toString();
        model =  props.get(DeviceProperties.MODEL, LRes.UNKNOWN).toString();
        serial = device.getSerial();
        UnlockStatus unlockStatus = (UnlockStatus) props.get(DeviceProperties.X_LOCKSTATUS);
        bootStatus = (UnlockStatus.UNLOCKED.equals(unlockStatus) ? LRes.UNLOCKED : (UnlockStatus.LOCKED.equals(unlockStatus) ? LRes.LOCKED : LRes.UNKNOWN)).toString();
        miuiVersion = props.get(DeviceProperties.FULL_VERSION, LRes.UNKNOWN).toString();
        androidVersion = props.get(DeviceProperties.CODEBASE, LRes.UNKNOWN).toString();
        Properties sideloadProperties =props.getSideloadProperties();
        recoveryAvailable = !UnlockStatus.UNLOCKED.equals(unlockStatus) ? (sideloadProperties.isParsed() ? LRes.YES : (sideloadProperties.isFailed() ? LRes.NO : LRes.UNKNOWN)).toString() : LRes.IRRELEVANT.toString() ;
        Properties fastbootProperties =props.getFastbootProperties();
        fastbootAvailable = fastbootProperties.isParsed() ? LRes.YES.toString() : (fastbootProperties.isFailed() ? LRes.NO.toString() : LRes.UNKNOWN.toString()) ;
        int sn =  (Integer) props.get(DeviceProperties.X_SERIAL_NUMBER,0);
        serialNumber = sn !=  0 ? NumberUtils.intToHex(sn) : LRes.UNKNOWN.toString();
        texts[0].setText(serial);
        texts[1].setText(brand);
        texts[2].setText(model);
        texts[3].setText(codename);
        texts[4].setText(miuiVersion);
        texts[5].setText(androidVersion);
        texts[6].setText(serialNumber);
        texts[7].setText(bootStatus);
        texts[8].setText(fastbootAvailable);
        texts[9].setText(recoveryAvailable);

    }
}
