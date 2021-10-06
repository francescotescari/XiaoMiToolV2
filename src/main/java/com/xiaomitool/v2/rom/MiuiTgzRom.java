package com.xiaomitool.v2.rom;

import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.procedure.RInstall;
import com.xiaomitool.v2.procedure.install.FastbootInstall;
import com.xiaomitool.v2.rom.interfaces.InstallObject;
import com.xiaomitool.v2.tasks.ExtractionTask;
import com.xiaomitool.v2.tasks.TaskManager;
import com.xiaomitool.v2.tasks.UpdateListener;
import com.xiaomitool.v2.xiaomi.miuithings.Codebase;
import com.xiaomitool.v2.xiaomi.miuithings.MiuiVersion;
import java.io.File;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiuiTgzRom extends MiuiRom {
  public MiuiTgzRom(
      String filename,
      MiuiVersion miuiVersion,
      Codebase codebase,
      String md5,
      Kind kind,
      String descriptionUrl,
      Specie specie) {
    super(Type.FASTBOOT, true, miuiVersion.toString() + "-" + filename, true, true);
    this.filename = filename;
    this.miuiVersion = miuiVersion;
    this.codebase = codebase;
    this.md5 = md5;
    this.kind = kind;
    this.descriptionUrl = descriptionUrl;
    this.specie = specie;
  }

  public MiuiTgzRom(String downloadUrl, Specie specie) throws RomException {
    super(Type.FASTBOOT, true, downloadUrl, true, true);
    this.downloadUrl = downloadUrl;
    Pattern p = Pattern.compile("http\\w{0,1}://[^/]+/([^/]+)/([^/]+)$");
    Matcher m = p.matcher(downloadUrl);
    if (!m.matches()) {
      throw new RomException("Not a valid download url: " + downloadUrl);
    }
    this.miuiVersion = new MiuiVersion(m.group(1));
    this.branch = this.miuiVersion.getBranch();
    this.filename = m.group(2);
    this.specie = specie;
    Pattern p2 = Pattern.compile("_([\\d.]{3,5})_(global|cn)");
    Matcher m2 = p2.matcher(this.filename);
    if (m2.find()) {
      this.codebase = new Codebase(m2.group(1));
    }
    this.unique = this.miuiVersion.toString() + "-" + filename;
  }

  public MiuiTgzRom(File localFile, boolean official) {
    super(Type.FASTBOOT, true, localFile.getAbsolutePath(), false, true);
    this.type = Type.FASTBOOT;
    this.isOfficial = official;
    this.downloadedFile = localFile;
    this.filename = localFile.getName();
  }

  public MiuiTgzRom(boolean isOfficial) {
    super(Type.FASTBOOT, isOfficial, "", false, true);
    setFake(true);
  }

  @Override
  protected Object extractInternal(UpdateListener listener) throws Exception {
    ExtractionTask task =
        new ExtractionTask(
            listener, getDownloadedFile(), null, ExtractionTask.ExtractionType.TGZ, true);
    TaskManager manager = TaskManager.getInstance();
    manager.startSameThread(task);
    if (!task.isFinished()) {
      throw new RomException("Extract task failed");
    }
    File extracted = (File) task.getResult();
    setFinalFile(extracted);
    return extracted;
  }

  @Override
  public LinkedHashSet<Device.Status> getRequiredStates() {
    return InstallObject.SET_FASTBOOT;
  }

  @Override
  public RInstall getInstallProcedure() {
    return FastbootInstall.findBuildRunFlashAll();
  }
}
