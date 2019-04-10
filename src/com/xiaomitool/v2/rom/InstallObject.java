package com.xiaomitool.v2.rom;

import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.procedure.RInstall;
import com.xiaomitool.v2.utility.utils.ArrayUtils;
import javafx.scene.image.Image;

import java.util.HashSet;
import java.util.Set;

public interface InstallObject {

    public static final Set<Device.Status> SET_FASTBOOT = ArrayUtils.createHashSet(Device.Status.FASTBOOT);
    public static final Set<Device.Status> SET_RECOVERY = ArrayUtils.createHashSet(Device.Status.RECOVERY);
    public static final Set<Device.Status> SET_SIDELOAD = ArrayUtils.createHashSet(Device.Status.SIDELOAD);



    String getTitle();
    String getText();
    Image getIcon();
    Set<Device.Status> getRequiredStates();
    String toLogString();
    Installable.Type getInstallType();
    boolean isProcedure();
    RInstall getInstallProcedure();

}
