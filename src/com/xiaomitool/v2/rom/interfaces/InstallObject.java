package com.xiaomitool.v2.rom.interfaces;

import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.procedure.RInstall;
import com.xiaomitool.v2.rom.Installable;
import com.xiaomitool.v2.utility.utils.ArrayUtils;
import javafx.scene.image.Image;

import java.util.HashSet;
import java.util.Set;

public interface InstallObject extends StatedProcedure {





    String getTitle();
    String getText();
    Image getIcon();

    String toLogString();
    Installable.Type getInstallType();
    boolean isProcedure();


}
