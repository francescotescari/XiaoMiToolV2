package com.xiaomitool.v2.rom.interfaces;

import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.procedure.RInstall;
import com.xiaomitool.v2.utility.utils.ArrayUtils;

import java.util.LinkedHashSet;

public interface StatedProcedure {
    LinkedHashSet<Device.Status> SET_FASTBOOT = ArrayUtils.createLinkedHashSet(Device.Status.FASTBOOT);
    LinkedHashSet<Device.Status> SET_RECOVERY = ArrayUtils.createLinkedHashSet(Device.Status.RECOVERY);
    LinkedHashSet<Device.Status> SET_SIDELOAD = ArrayUtils.createLinkedHashSet(Device.Status.SIDELOAD);
    LinkedHashSet<Device.Status> SET_DEVICE = ArrayUtils.createLinkedHashSet(Device.Status.DEVICE);

    RInstall getInstallProcedure();

    LinkedHashSet<Device.Status> getRequiredStates();
}
