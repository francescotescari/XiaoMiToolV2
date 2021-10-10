package com.xiaomitool.v2.adb.device;

import com.xiaomitool.v2.adb.AdbCommunication;
import com.xiaomitool.v2.engine.CommonsMessages;
import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.utility.MessageReceiver;
import com.xiaomitool.v2.utility.Pair;
import com.xiaomitool.v2.utility.WaitSemaphore;
import com.xiaomitool.v2.utility.utils.ArrayUtils;
import java.util.*;
import javafx.application.Platform;

public class DeviceManager {
  private static final HashMap<String, Device.Status> updatingDevices = new HashMap<>();
  private static final ArrayList<WaitSemaphore> refreshWaitSems = new ArrayList<>();
  private static final Integer BLOCKING_OBJ = 0;
  private static Device selectedDevice = null;
  private static HashMap<String, Device> deviceMap = new HashMap<>();
  private static HashSet<MessageReceiver> messageReceivers = new HashSet<>();
  private static Thread refreshingThread = null;

  public static void initScanThreads() {
    AdbCommunication.registerAutoScanDevices();
  }

  public static void stopScanThreads() {
    AdbCommunication.unregisterAutoScanDevices();
  }

  public static void addDevice(Device device) {
    deviceMap.put(device.getSerial(), device);
  }

  public static Device getSelectedDevice() {
    return selectedDevice;
  }

  public static void setSelectedDevice(Device device) {
    selectedDevice = device;
    if (device != null) {
      Platform.runLater(
          () -> {
            WindowManager.toast(LRes.SELECTED_DEVICE.toString(device.getSerial()));
          });
    }
  }

  public static void setSelectedDevice(String serial) {
    setSelectedDevice(deviceMap.get(serial));
  }

  public static void waitRefresh() throws InterruptedException {
    if (!AdbCommunication.isAutoScanRegistered()) {
      refresh(true);
      return;
    }
    if (Thread.currentThread().equals(refreshingThread)) {
      return;
    }
    WaitSemaphore waitSemaphore = new WaitSemaphore(0);
    synchronized (refreshWaitSems) {
      refreshWaitSems.add(waitSemaphore);
    }
    waitSemaphore.waitOnce();
  }

  public static void refresh(boolean blocking) {
    if (blocking) {
      refreshBlocking();
    } else {
      refreshInternal(false);
    }
  }

  private static synchronized void refreshBlocking() {
    refreshInternal(true);
  }

  private static void refreshInternal(boolean blocking) {
    List<Pair<String, Device.Status>> toUpdate;
    synchronized (updatingDevices) {
      refreshingThread = Thread.currentThread();
      updatingDevices.clear();
      AdbCommunication.refreshDevices(updatingDevices);
      for (Map.Entry<String, Device.Status> entry : updatingDevices.entrySet()) {
        Log.info("Connected device: " + entry.getKey() + " -> " + entry.getValue());
      }
      for (Device d : deviceMap.values()) {
        if (!Device.Status.EDL.equals(d.getStatus())
            && updatingDevices.get(d.getSerial()) == null) {
          d.setConnected(false);
        }
      }
      toUpdate = new ArrayList<>(updatingDevices.size());
      for (Map.Entry<String, Device.Status> entry : updatingDevices.entrySet()) {
        toUpdate.add(new Pair<>(entry.getKey(), entry.getValue()));
      }
    }
    for (Pair<String, Device.Status> entry : toUpdate) {
      setDeviceStatus(entry.getFirst(), entry.getSecond(), blocking);
    }
    synchronized (refreshWaitSems) {
      for (WaitSemaphore s : refreshWaitSems) {
        s.increase();
      }
      refreshWaitSems.clear();
    }
    refreshingThread = null;
  }

  public static int count() {
    int i = 0;
    for (Device d : deviceMap.values()) {
      if (d.isConnected()) {
        ++i;
      }
    }
    return i;
  }

  public static int count(Device.Status... wantedStatus) {
    if (wantedStatus == null || wantedStatus.length == 0) {
      return count();
    }
    int i = 0;
    for (Device d : deviceMap.values()) {
      if (d.isConnected() && ArrayUtils.in(wantedStatus, d.getStatus())) {
        ++i;
      }
    }
    return i;
  }

  protected static void setDeviceStatus(String serial, Device.Status status, boolean blocking) {
    Device device = deviceMap.get(serial);
    if (device == null) {
      device = new Device(serial, status, true);
      deviceMap.put(serial, device);
      message(CommonsMessages.NEW_DEVICE);
    }
    boolean newStatus = !status.equals(device.getStatus());
    if (newStatus) message(CommonsMessages.DEVICE_UPDATE_STATUS);
    device.setStatus(status, blocking);
    device.setConnected(true);
    if (newStatus) message(CommonsMessages.DEVICE_UPDATE_FINISH);
  }

  public static Device getFirstDevice() {
    if (deviceMap.size() == 0) {
      return null;
    }
    return deviceMap.entrySet().iterator().next().getValue();
  }

  private static void message(int message) {
    for (MessageReceiver messageReceiver : messageReceivers) {
      messageReceiver.message(message);
    }
  }

  public static Collection<Device> getDevices() {
    return deviceMap.values();
  }

  public static void addMessageReceiver(MessageReceiver receiver) {
    messageReceivers.add(receiver);
  }

  public static void removeMessageReceiver(MessageReceiver receiver) {
    messageReceivers.remove(receiver);
  }
}
