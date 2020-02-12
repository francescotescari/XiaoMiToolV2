package com.xiaomitool.v2.rom;

import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.procedure.RInstall;
import com.xiaomitool.v2.procedure.install.StockRecoveryInstall;
import com.xiaomitool.v2.rom.interfaces.InstallObject;
import com.xiaomitool.v2.utility.NotNull;
import com.xiaomitool.v2.xiaomi.miuithings.Branch;
import com.xiaomitool.v2.xiaomi.miuithings.Codebase;
import com.xiaomitool.v2.xiaomi.miuithings.MiuiVersion;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiuiZipRom extends MiuiRom {
    public MiuiZipRom(String filename, @NotNull MiuiVersion miuiVersion, Branch branch, Codebase codebase, String md5, String token, Kind kind, String descriptionUrl, Specie specie) {
        super(Type.RECOVERY, true, miuiVersion.toString() + "-" + filename, true, false);
        this.filename = filename;
        this.miuiVersion = miuiVersion;
        this.codebase = codebase;
        this.installToken = token;
        this.md5 = md5;
        this.kind = kind;
        this.branch = branch;
        this.descriptionUrl = descriptionUrl;
        this.downloadUrl = mirrors.resolve(miuiVersion.toString() + "/" + filename);
        this.specie = specie;
    }

    public MiuiZipRom(String downloadUrl, Specie specie) throws RomException {
        super(Type.RECOVERY, true, downloadUrl, true, false);
        this.specie = specie;
        this.downloadUrl = downloadUrl;
        Pattern p = Pattern.compile("http\\w{0,1}://[^/]+/([^/]+)/([^/]+)$");
        Matcher m = p.matcher(downloadUrl);
        if (!m.matches()) {
            throw new RomException("Not a valid download url: " + downloadUrl);
        }
        this.miuiVersion = new MiuiVersion(m.group(1));
        this.branch = this.miuiVersion.getBranch();
        this.filename = m.group(2);
        Pattern p2 = Pattern.compile("_([\\d.]+)\\.zip");
        Matcher m2 = p2.matcher(this.filename);
        if (m2.find()) {
            this.codebase = new Codebase(m2.group(1));
        }
        this.unique = this.miuiVersion.toString() + "-" + filename;
    }

    public MiuiZipRom(File localFile, boolean official) {
        super(Type.RECOVERY, official, localFile.getAbsolutePath(), false, false);
        this.downloadedFile = localFile;
        this.finalFile = localFile;
        this.filename = localFile.getName();
    }

    public MiuiZipRom(boolean isOfficial) {
        super(Type.RECOVERY, isOfficial, "", false, false);
        setFake(true);
    }

    @Override
    public LinkedHashSet<Device.Status> getRequiredStates() {
        return isOfficial() ? InstallObject.SET_SIDELOAD : InstallObject.SET_RECOVERY;
    }

    @Override
    public RInstall getInstallProcedure() {
        return StockRecoveryInstall.stockRecoveryInstall();
    }
}
