package com.xiaomitool.v2.tasks;

import com.xiaomitool.v2.adb.AdbException;
import com.xiaomitool.v2.process.AdbRunner;
import com.xiaomitool.v2.utility.RunnableWithArg;

public class TwrpInstallTask extends Task {
  private static final String[] TWRP_ERROR =
      new String[] {
        "Error installing zip file",
        "Errore durante l'installazione dello Zip",
        "å®‰è£zipåˆ·æ©ŸåŒ…|å‡ºéŒ¯",
        "æœºåŒ…æ–‡ä»¶"
      };
  protected AdbRunner adbRunner;
  private RunnableWithArg outputRunable;

  public TwrpInstallTask(
      UpdateListener listener,
      String device,
      String remoteFilename,
      RunnableWithArg onOutputCatch) {
    super(listener);
    this.adbRunner = new AdbRunner("shell", "-x", "twrp install " + remoteFilename);
    this.adbRunner.setDeviceSerial(device);
    this.outputRunable = onOutputCatch;
  }

  @Override
  protected void startInternal() throws Exception {
    if (this.outputRunable != null) {
      adbRunner.addSyncCallback(this.outputRunable);
    }
    adbRunner.runWait(3600);
    if (adbRunner.getExitValue() != 0) {
      throw new AdbException("Return code of adb install is not zero");
    }
    String output = adbRunner.getOutputString();
    if (output == null) {
      throw new AdbException("Failed to get output of twrp install");
    }
    output = output.toLowerCase();
    for (String errorStr : TWRP_ERROR) {
      if (output.contains(errorStr.toLowerCase())) {
        throw new AdbException("Failed to install zip: " + errorStr);
      }
    }
    finished(adbRunner.getOutputString());
  }

  @Override
  protected boolean canPause() {
    return false;
  }

  @Override
  protected boolean canStop() {
    return true;
  }

  @Override
  protected boolean pauseInternal() {
    return false;
  }

  @Override
  protected boolean stopInternal() {
    return adbRunner.kill();
  }
}
