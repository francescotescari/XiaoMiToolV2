package com.xiaomitool.v2.adb.device;

import com.xiaomitool.v2.adb.AdbCommunication;
import com.xiaomitool.v2.engine.CommonsMessages;
import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.utility.MessageReceiver;
import javafx.application.Platform;

import java.util.*;


public class DeviceManager {


    private static Device selectedDevice = null;
    private static HashMap<String, Device> deviceMap = new HashMap<>();
    private static HashMap<String, Device.Status> updatingDevices;
    private static HashSet<MessageReceiver> messageReceivers = new HashSet<>();
    public static void initScanThreads(){
        AdbCommunication.registerAutoScanDevices();
    }

    public static void stopScanThreads() {
        AdbCommunication.unregisterAutoScanDevices();
    }

    public static void addDevice(Device device){
        deviceMap.put(device.getSerial(), device);
    }
    public static void setSelectedDevice(Device device){
        selectedDevice = device;
        if (device != null){
            Log.debug("Device selected: "+device.getSerial());
            Platform.runLater(()->{WindowManager.toast(LRes.SELECTED_DEVICE.toString(device.getSerial()));});

        }
    }
    public static void setSelectedDevice(String serial){
        setSelectedDevice(deviceMap.get(serial));
    }

    public static Device getSelectedDevice() {
        return selectedDevice;
    }

    public static synchronized void refresh(){
        updatingDevices = new HashMap<>();
        AdbCommunication.refreshDevices();
        for (Map.Entry<String, Device.Status> entry : updatingDevices.entrySet()){
            Log.info("Connected device: "+entry.getKey()+" -> "+entry.getValue());
            Log.debug(entry.getKey() + " - "+entry.getValue());
        }
        for (Device d : deviceMap.values()){
            if (!Device.Status.EDL.equals(d.getStatus()) && updatingDevices.get(d.getSerial()) == null) {
                d.setConnected(false);
                Log.debug(d.getSerial() + " is not in updating devices");
            }
        }
        for (Map.Entry<String, Device.Status> entry : updatingDevices.entrySet()){
            setDeviceStatus(entry.getKey(), entry.getValue());
        }

    }
    public static int count(){
        int i = 0;
        for (Device d : deviceMap.values()){
            if (d.isConnected()){
                ++i;
            }
        }
        return i;

    }
    public static int count(Device.Status wantedStatus){
        if (wantedStatus == null){
            return count();
        }
        int i = 0;
        for (Device d : deviceMap.values()){
            if (d.isConnected() && wantedStatus.equals(d.getStatus())){
                ++i;
            }
        }
        return i;
    }
    public static void setDeviceStatus(String serial, Device.Status status){
        Log.debug("Device status of "+serial+": "+status.toString());
        Device device = deviceMap.get(serial);
        if (device == null){
            device = new Device(serial, status);
            deviceMap.put(serial, device);
            message(CommonsMessages.NEW_DEVICE);


        }
        boolean newStatus = !status.equals(device.getStatus());
        if (newStatus)
        message(CommonsMessages.DEVICE_UPDATE_STATUS);
        device.setStatus(status);
        device.setConnected(true);
        if(newStatus)
        message(CommonsMessages.DEVICE_UPDATE_FINISH);

    }
    public static void cacheDeviceStatus(String serial, Device.Status status){
        updatingDevices.put(serial,status);
    }

    public static Device getFirstDevice(){
        if (deviceMap.size() == 0){
            return null;
        }
        return deviceMap.entrySet().iterator().next().getValue();
    }


    private static void message(int message){
        for (MessageReceiver messageReceiver : messageReceivers){
            messageReceiver.message(message);
        }
    }
    public static Collection<Device> getDevices(){
        return deviceMap.values();
    }
    public static void addMessageReceiver(MessageReceiver receiver){
        messageReceivers.add(receiver);
    }
    public static void removeMessageReceiver(MessageReceiver receiver){
        messageReceivers.remove(receiver);
    }


}
