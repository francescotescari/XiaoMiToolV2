package com.xiaomitool.v2.adb;

import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.adb.device.DeviceManager;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.process.AdbRunner;
import com.xiaomitool.v2.utility.RunnableWithArg;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AdbCommunication {
    private static final int REFRESH_TIME_MS = 2500;
    private static final Object sync = new Object();
    public static Thread trackDevicesThread, refreshDevicesThread;
    private static int cannotInterruptCount = 0;
    private static boolean isTrackDeviceActive = false;
    private static AdbRunner trackDeviceProcess;

    public static boolean canInterrupt() {
        return cannotInterruptCount == 0;
    }

    public static void startServer() {
        if (!canInterrupt()) {
        }
        AdbCommons.start_server();
    }

    public static void killServer() {
        if (!canInterrupt()) {
            return;
        }
        AdbCommons.kill_server();
    }

    public static void restartServer() {
        killServer();
        startServer();
    }

    public static void registerAutoScanDevices() {
        Log.info("Starting autoscan threads");
        synchronized (sync) {
            if (refreshDevicesThread != null) {
                return;
            }
        }
        Runnable trackDevices = new Runnable() {
            @Override
            public void run() {
                trackDeviceProcess = new AdbRunner();
                trackDeviceProcess.addArgument("track-devices");
                trackDeviceProcess.addSyncCallback(new RunnableWithArg() {
                    @Override
                    public void run(Object arg) {
                        DeviceManager.refresh(true);
                    }
                });
                try {
                    isTrackDeviceActive = true;
                    trackDeviceProcess.runWait(Integer.MAX_VALUE);
                } catch (IOException e) {
                    Log.warn("Cannot register device scanner thread via track-devices");
                    isTrackDeviceActive = false;
                }
            }
        };
        trackDevicesThread = new Thread(trackDevices);
        trackDevicesThread.start();
        Runnable scanFastbootAdb = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    DeviceManager.refresh(true);
                    try {
                        Thread.sleep(REFRESH_TIME_MS);
                    } catch (InterruptedException e) {
                        Log.error("Refresh thread interrupted");
                        Log.printStackTrace(e);
                    }
                }
            }
        };
        refreshDevicesThread = new Thread(scanFastbootAdb);
        refreshDevicesThread.start();
    }

    private static void refereshAdbDevices(Map<String, Device.Status> updatingDevices) {
        List<String> cmdOut = AdbCommons.devices();
        if (cmdOut == null) {
            return;
        }
        synchronized (updatingDevices) {
            updatingDevices.putAll(AdbUtils.parseDevices(cmdOut));
        }
    }

    private static void refreshFastbootDevices(Map<String, Device.Status> updatingDevices) {
        List<String> cmdOut = FastbootCommons.devices();
        if (cmdOut == null) {
            return;
        }
        synchronized (updatingDevices) {
            updatingDevices.putAll(AdbUtils.parseDevices(cmdOut));
        }
    }

    public static void refreshDevices(Map<String, Device.Status> updatingDevices) {
        refereshAdbDevices(updatingDevices);
        refreshFastbootDevices(updatingDevices);
    }

    public static void getAllAccess() {
        cannotInterruptCount++;
        unregisterAutoScanDevices();
    }

    public static void giveAllAccess() {
        if (cannotInterruptCount > 0) {
            cannotInterruptCount--;
        }
        if (canInterrupt()) {
            registerAutoScanDevices();
        }
    }

    public static void unregisterAutoScanDevices() {
        Log.info("Stopping autoscan threads");
        if (trackDeviceProcess != null) {
            trackDeviceProcess.kill();
        }
        if (refreshDevicesThread != null && refreshDevicesThread.isAlive()) {
            refreshDevicesThread.interrupt();
        }
        refreshDevicesThread = null;
        if (trackDevicesThread != null && trackDevicesThread.isAlive()) {
            trackDevicesThread.interrupt();
        }
        trackDevicesThread = null;
    }

    public static boolean isAutoScanRegistered() {
        return refreshDevicesThread != null && refreshDevicesThread.isAlive();
    }
}
