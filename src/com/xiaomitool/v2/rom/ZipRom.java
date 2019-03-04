package com.xiaomitool.v2.rom;

import com.xiaomitool.v2.gui.visual.ChooserPane;
import com.xiaomitool.v2.procedure.RInstall;
import com.xiaomitool.v2.procedure.install.TwrpInstall;
import com.xiaomitool.v2.xiaomi.miuithings.Codebase;
import com.xiaomitool.v2.xiaomi.miuithings.MiuiVersion;

import java.io.File;

public abstract class ZipRom extends Installable {
    public ZipRom(String downloadUrl){
        this(downloadUrl, null,null);
    }

    public ZipRom(String downloadUrl, MiuiVersion version, Codebase codebase) {
        super(Type.RECOVERY, false, downloadUrl, true, false);
        this.downloadUrl = downloadUrl;
        this.miuiVersion = version;
        this.codebase = codebase;
    }

    public ZipRom() {
        super(Type.RECOVERY, false, "", false, false);
    }

    public ZipRom(File file) {
        super(Type.RECOVERY, false, file.toString(), false, false);
        this.setFinalFile(file);
    }

    @Override
    public RInstall getInstallProcedure() {
        return TwrpInstall.installZip();
    }

    /*@Override
    public ChooserPane.Choice getChoice() {
        return null;//TODO
    }*/
}
