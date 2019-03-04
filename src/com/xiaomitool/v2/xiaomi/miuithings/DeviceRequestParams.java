package com.xiaomitool.v2.xiaomi.miuithings;

import com.xiaomitool.v2.adb.AdbException;
import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.adb.device.DeviceProperties;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.utility.Nullable;
import com.xiaomitool.v2.utility.utils.ObjUtils;
import com.xiaomitool.v2.xiaomi.XiaomiUtilities;


public class DeviceRequestParams extends DefaultRequestParams implements Cloneable {
    public DeviceRequestParams(String device, String version, String codebase, @Nullable Branch branch, String serialNumber, int zone){
        super(device,version,codebase,branch);
        super.serialNumber = serialNumber;
        super.zone = zone;

    }

    public static DeviceRequestParams readFromDevice(Device device) throws AdbException {
        return readFromDevice(device, false);
    }

    public static DeviceRequestParams readFromDevice(Device device, boolean requireOtaParameters) throws AdbException {
        DeviceProperties properties = device.getDeviceProperties();

        String codename = (String) properties.get(DeviceProperties.CODENAME);
        if (codename == null){
            throw new AdbException("Missing device codename in device properties");
        }
        String version = (String) properties.get(DeviceProperties.FULL_VERSION);
        String codebase = (String) properties.get(DeviceProperties.CODEBASE);
        Integer serialNumber = (Integer)  properties.get(DeviceProperties.X_SERIAL_NUMBER);
        String zone =  (String) properties.get(DeviceProperties.ROMZONE);
        if (requireOtaParameters) {
            try {

                ObjUtils.checkNotNull(version, "version");
                ObjUtils.checkNotNull(codebase, "codebase");
                ObjUtils.checkNotNull(serialNumber, "serialNumber");
                if (serialNumber == 0){
                    throw new NullPointerException("serial number is zero");
                }
            } catch (NullPointerException e){
                throw new AdbException("Missing required ota parameter in device properties: "+e.getMessage());
            }
        }
        int z;
        try {
            z = Integer.parseInt(zone.trim());
        } catch (Throwable e){
            z = codename.endsWith("_global") ? 2 : 1;
        }
        if (zone == null || zone.isEmpty()){
            z = 0;
        }
        MiuiVersion miuiVersion = new MiuiVersion(version);
        Branch branch = (Branch) properties.get(DeviceProperties.X_BRANCH);
        if (branch == null){
            branch = miuiVersion.getBranch();
        }
        return new DeviceRequestParams(codename,version,codebase,branch, XiaomiUtilities.snToString(serialNumber), z);
        

    }
    public DeviceRequestParams clone() throws CloneNotSupportedException {
        return (DeviceRequestParams) super.clone();
    }

}
