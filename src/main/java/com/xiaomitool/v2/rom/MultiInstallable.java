package com.xiaomitool.v2.rom;

import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.procedure.*;
import com.xiaomitool.v2.procedure.install.GenericInstall;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.tasks.UpdateListener;
import java.io.File;
import java.util.LinkedHashSet;
import java.util.Objects;

public abstract class MultiInstallable extends Installable {
  private Installable[] children;
  private int currentId = 0;

  public MultiInstallable(Installable... children) {
    super(Type.MULTI, isOfficial(children), unique(children), true, true);
    this.children = children;
  }

  private static boolean isOfficial(Installable... children) {
    boolean official = true;
    for (Installable installable : children) {
      official &= installable.isOfficial();
    }
    return official;
  }

  private static String unique(Installable... children) {
    StringBuilder builder = new StringBuilder();
    for (Installable installable : children) {
      builder.append(installable.getUniqueId());
    }
    return builder.toString();
  }

  public void setCurrentId(int id) {
    this.currentId = id;
    this.setFinalFile(null);
    this.downloadedFile = null;
  }

  public Installable[] getChildren() {
    return children;
  }

  @Override
  public RInstall getInstallProcedure() {
    RInstall[] parts = new RInstall[children.length * 2 - 1];
    int i = 0;
    int current = 0;
    for (Installable child : children) {
      final int id = ++current;
      if (i >= parts.length) {
        break;
      }
      parts[i++] = child.getInstallProcedure();
      if (i <= parts.length - 2) {
        parts[i++] =
            RNode.sequence(
                new RInstall() {
                  @Override
                  public void run(ProcedureRunner runner)
                      throws InstallException, RMessage, InterruptedException {
                    setCurrentId(id);
                  }
                },
                Objects.equals(
                        children[current - 1].getInstallType(), children[current].getInstallType())
                    ? Procedures.doNothing()
                    : GenericInstall.satisfyAllRequirements(),
                GenericInstall.resourceFetchWait());
      }
    }
    return RNode.sequence(parts);
  }

  @Override
  public LinkedHashSet<Device.Status> getRequiredStates() {
    LinkedHashSet<Device.Status> hashSet = new LinkedHashSet<>();
    for (Installable child : children) {
      LinkedHashSet<Device.Status> cSet = child.getRequiredStates();
      if (cSet != null) {
        hashSet.addAll(cSet);
      }
    }
    return hashSet;
  }

  @Override
  protected Object downloadInternal(UpdateListener listener) throws Exception {
    Installable child = children[currentId];
    if (!child.needDownload) {
      if (child.downloadedFile != null) {
        return child.downloadedFile;
      } else if (child.getFinalFile() != null) {
        return child.getFinalFile();
      } else {
        return null;
      }
    }
    return getCurrentChild().downloadInternal(listener);
  }

  @Override
  protected Object extractInternal(UpdateListener listener) throws Exception {
    Installable child = children[currentId];
    if (!child.needExtraction) {
      if (child.getFinalFile() != null) {
        return child.getFinalFile();
      } else if (child.downloadedFile != null) {
        child.setFinalFile(child.downloadedFile);
        return child.getFinalFile();
      } else {
        return null;
      }
    }
    return getCurrentChild().extractInternal(listener);
  }

  @Override
  public String getDownloadUrl() {
    return getCurrentChild().getDownloadUrl();
  }

  @Override
  public File getFinalFile() {
    return getCurrentChild().getFinalFile();
  }

  @Override
  public File getDownloadedFile() {
    return getCurrentChild().getDownloadedFile();
  }

  @Override
  public boolean isNeedDownload() {
    return getCurrentChild().isNeedDownload();
  }

  @Override
  public boolean isNeedExtraction() {
    return getCurrentChild().isNeedExtraction();
  }

  public Installable getCurrentChild() {
    return children[currentId];
  }

  @Override
  public Installable orig() {
    return getCurrentChild();
  }
}
