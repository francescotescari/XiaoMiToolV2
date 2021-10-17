package com.xiaomitool.v2.process;

import com.xiaomitool.v2.resources.ResourcesManager;
import java.util.LinkedList;
import java.util.List;

public class FastbootRunner extends ProcessRunner {
  private static long feedbackDisabled = 0;
  private String deviceSerial = null;

  public FastbootRunner() {
    super(ResourcesManager.getFastbootPath());
  }

  public void setDeviceSerial(String deviceSerial) {
    this.deviceSerial = deviceSerial;
  }

  @Override
  protected List<String> buildFinalArgumentsList() {
    LinkedList<String> list = new LinkedList<>();
    list.add(executable.toString());
    if (deviceSerial != null) {
      list.add("-s");
      list.add(deviceSerial);
    }
    isFeedback = !arguments.contains("devices");
    if (!isFeedback) {
      isFeedback = feedbackDisabled++ % 10 == 0;
    }
    list.addAll(arguments);
    return list;
  }
}
