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
    public static Thread trackDevicesThread, refreshDevicesThread;
    private static int cannotInterruptCount = 0; private static boolean isTrackDeviceActive=false;
    private static final int REFRESH_TIME_MS  = 2500;
    private static AdbRunner trackDeviceProcess;
    private static final Object sync = new Object();
    public static boolean canInterrupt(){
        return cannotInterruptCount == 0;
    }

    public static void startServer(){
        if (!canInterrupt()){
            Log.debug("Cannot interrupt adb connection");
        }
        AdbCommons.start_server();

    }
    public static void killServer(){
        if (!canInterrupt()){
            Log.debug("Cannot interrupt adb connection");
            return;
        }
        AdbCommons.kill_server();
    }
    public static void restartServer(){
        killServer();
        startServer();
    }




    public static void registerAutoScanDevices(){
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
                        DeviceManager.refresh();
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
                    DeviceManager.refresh();
                    try {
                        Thread.sleep(REFRESH_TIME_MS);
                    } catch (InterruptedException e) {

                    }
                }
            }
        };
        refreshDevicesThread = new Thread(scanFastbootAdb);
        refreshDevicesThread.start();
    }

    private static void refereshAdbDevices(){
            List<String> cmdOut = AdbCommons.devices();
            if (cmdOut == null){
                return;
            }

            for (Map.Entry<String, Device.Status> entry : AdbUtils.parseDevices(cmdOut).entrySet()){
                DeviceManager.cacheDeviceStatus(entry.getKey(), entry.getValue());
            }

    }
    private static void  refreshFastbootDevices(){
            List<String> cmdOut = FastbootCommons.devices();
            if (cmdOut == null){
                return;
            }
            for (Map.Entry<String, Device.Status> entry : AdbUtils.parseDevices(cmdOut).entrySet()){
                DeviceManager.cacheDeviceStatus(entry.getKey(), entry.getValue());
            }

    }

    public static void refreshDevices(){
        refereshAdbDevices();
        refreshFastbootDevices();
    }

    public static void getAllAccess() {
        cannotInterruptCount++;
        unregisterAutoScanDevices();
    }
    public static void giveAllAccess(){
        if (cannotInterruptCount > 0){
            cannotInterruptCount--;
        }
        if (canInterrupt()) {
            registerAutoScanDevices();
        }
    }
    public static void unregisterAutoScanDevices(){
        if (trackDeviceProcess != null){
            trackDeviceProcess.kill();
        }
        if (refreshDevicesThread != null && refreshDevicesThread.isAlive()){
            refreshDevicesThread.interrupt();
        }
        refreshDevicesThread = null;
        if (trackDevicesThread != null && trackDevicesThread.isAlive()){
            trackDevicesThread.interrupt();
        }
        trackDevicesThread = null;
    }
}
