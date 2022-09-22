package com.xiaomitool.v2.rom.interfaces;

import com.xiaomitool.v2.rom.Installable;
import javafx.scene.image.Image;

public interface InstallObject extends StatedProcedure {
    String getTitle();

    String getText();

    Image getIcon();

    String toLogString();

    Installable.Type getInstallType();

    boolean isProcedure();
}
