package com.xiaomitool.v2.rom;

import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.procedure.Procedures;
import com.xiaomitool.v2.procedure.RInstall;
import com.xiaomitool.v2.procedure.install.TwrpInstall;
import java.io.File;
import java.util.LinkedHashSet;
import javafx.scene.image.Image;

public class ApkFileInstallable extends Installable {
  private String name, packageName;
  private Device.Status installStatus;

  public ApkFileInstallable(String name, File apkFile, Device.Status installStatus) {
    super(Type.APK, false, "apk" + name, false, false, apkFile);
    this.name = name;
    this.installStatus = installStatus;
  }

  public ApkFileInstallable(String name, String downloadUrl, Device.Status installStatus) {
    super(Type.APK, false, "apk" + name, true, false, downloadUrl);
    this.name = name;
    this.installStatus = installStatus;
  }

  @Override
  public LinkedHashSet<Device.Status> getRequiredStates() {
    if (Device.Status.RECOVERY.equals(this.installStatus)) {
      return SET_RECOVERY;
    } else {
      return SET_DEVICE;
    }
  }

  @Override
  public String getTitle() {
    return this.name;
  }

  @Override
  public String getText() {
    return LRes.INSTALL_APK_APP.toString(name);
  }

  @Override
  public Image getIcon() {
    return null;
  }

  @Override
  public RInstall getInstallProcedure() {
    if (Device.Status.RECOVERY.equals(this.installStatus)) {
      return TwrpInstall.installApkViaTwrp();
    } else {
      return Procedures.featureNotAvailable();
    }
  }

  public String getPackageName() {
    return this.packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }
}
