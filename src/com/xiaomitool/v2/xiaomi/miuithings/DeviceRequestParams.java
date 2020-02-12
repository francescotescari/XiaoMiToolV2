package com.xiaomitool.v2.xiaomi.miuithings;

import com.xiaomitool.v2.adb.AdbException;
import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.adb.device.DeviceProperties;
import com.xiaomitool.v2.rom.MiuiRom;
import com.xiaomitool.v2.utility.Nullable;
import com.xiaomitool.v2.utility.utils.ObjUtils;

public class DeviceRequestParams extends DefaultRequestParams implements Cloneable {
    public DeviceRequestParams(String device, String version, String codebase, @Nullable Branch branch, SerialNumber serialNumber, int zone) {
        super(device, version, codebase, branch);
        super.serialNumber = serialNumber;
        super.zone = zone;
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static DeviceRequestParams readFromDevice(Device device) throws AdbException {
        return readFromDevice(device, false);
    }

    public static DeviceRequestParams readFromDevice(Device device, boolean requireOtaParameters) throws AdbException {
        DeviceProperties properties = device.getDeviceProperties();
        String codename = properties.getCodename(false);
        if (codename == null) {
            throw new AdbException("Missing device codename in device properties");
        }
        String version = (String) properties.get(DeviceProperties.FULL_VERSION);
        String codebase = (String) properties.get(DeviceProperties.CODEBASE);
        if (codebase != null) {
            if (codebase.length() == 1) {
                codebase = codebase + ".0";
            } else if (codebase.endsWith(".")) {
                codebase += "0";
            }
        }
        String zone = (String) properties.get(DeviceProperties.ROMZONE);
        SerialNumber sn = device.getAnswers().getSerialNumber();
        if (requireOtaParameters) {
            try {
                ObjUtils.checkNotNull(version, "version");
                ObjUtils.checkNotNull(codebase, "codebase");
                ObjUtils.checkNotNull(sn, "serialNumber");
                if (!sn.isValid()) {
                    throw new NullPointerException("serial number is invalid: " + sn);
                }
            } catch (NullPointerException e) {
                throw new AdbException("Missing required ota parameter in device properties: " + e.getMessage());
            }
        }
        int z;
        if (zone == null || zone.isEmpty()) {
            z = 0;
        } else {
            try {
                z = Integer.parseInt(zone.trim());
            } catch (Throwable e) {
                z = MiuiRom.Specie.getZone(codename);
            }
        }
        MiuiVersion miuiVersion = new MiuiVersion(version);
        Branch branch = (Branch) properties.get(DeviceProperties.X_BRANCH);
        if (branch == null) {
            branch = miuiVersion.getBranch();
        }
        return new DeviceRequestParams(codename, version, codebase, branch, sn, z);
    }

    public DeviceRequestParams clone() throws CloneNotSupportedException {
        return (DeviceRequestParams) super.clone();
    }
}
