package com.xiaomitool.v2.adb.device;


import com.xiaomitool.v2.adb.AdbCommons;
import com.xiaomitool.v2.adb.AdbException;
import com.xiaomitool.v2.adb.FastbootCommons;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.utility.NotNull;
import com.xiaomitool.v2.utility.WaitSemaphore;
import com.xiaomitool.v2.utility.YesNoMaybe;
import com.xiaomitool.v2.utility.utils.ThreadUtils;

public class Device {

    public enum Status {
        FASTBOOT("fastboot"),
        DEVICE("device"),
        UNAUTHORIZED("unauthorized"),
        OFFLINE("offline"),
        EDL("edl"),
        RECOVERY("recovery"),
        SIDELOAD("sideload"),
        UNKNOWN("unknown");
        private String text;
        Status(String text){
            this.text = text;
        }
        @Override
        public String toString(){
            return this.text;
        }
        public static Status fromString(String text){
            if (text == null){
                return UNKNOWN;
            }
            for (Status s : Status.values()){
                if (text.equals(s.toString())){
                    return s;
                }
            }
            return UNKNOWN;
        }
    }

    private String serial;
    private Status status;
    private boolean isConnected = true, firstOffline = true;
    private final DeviceProperties deviceProperties ;
    private final WaitSemaphore deviceActiveSem = new WaitSemaphore(), canBeAccessed = new WaitSemaphore(1, "can_be_accessed");
    private final DeviceAnswers deviceAnswers;

    public Device(String serial){
        this(serial,Status.UNKNOWN);
    }
    public Device(String serial, String state){
        this(serial, Status.fromString(state));
    }
    public Device(String serial, Status state){
        this(serial, state, false);
    }
    public Device(String serial, Status state, boolean blocking){
        this.serial = serial;
        deviceProperties = new DeviceProperties(this);
        deviceAnswers = new DeviceAnswers(this);
        setStatus(state, blocking);
    }
    public void setStatus(Status status, boolean blocking) {
        try {
            obtainAccess();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            if (!status.equals(this.status)) {
                Log.debug("Device " + serial + " status: " + status.toString());
            }

            this.status = status;
            Runnable doParse = () -> setStatusParse(status);
            if (blocking){
                releaseAccess();
                try {
                    doParse.run();
                } finally {
                    requireAccess();
                }
            } else {
                new Thread(doParse).start();
            }
            getAnswers().updateStatus(status);
        } finally {
            releaseAccess();
        }


    }

    private synchronized void setStatusParse(Status status) {
        synchronized (deviceProperties) {
            if (Status.FASTBOOT.equals(status) && !deviceProperties.getFastbootProperties().isParsed()) {
                ThreadUtils.sleepSilently(2000);
                deviceProperties.getFastbootProperties().parse();
            } else if (Status.SIDELOAD.equals(status)) {
                deviceProperties.getSideloadProperties().parse();
            } else if (Status.DEVICE.equals(status)) {
                getAnswers().setNeedDeviceDebug(YesNoMaybe.NO);
                deviceProperties.getAdbProperties().parse();
            } else if (Status.RECOVERY.equals(status)) {
                deviceProperties.getRecoveryProperties().parse();
            }
        }

    }
    public Status getStatus() {
        return status;
    }

    public String getSerial() {
        return serial;
    }
    public void requireAccess(){
        try {
            canBeAccessed.waitOnce();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void obtainAccess() throws InterruptedException {
        canBeAccessed.decrease();
    }
    public void releaseAccess(){
        canBeAccessed.increase();
    }

    public void setConnected(boolean connected) {
        Log.debug("Set connected for device "+ serial+": "+connected);
        isConnected = connected;
        if (connected){
            deviceActiveSem.setPermits(1);
        } else {
            deviceActiveSem.setPermits(0);
        }
    }
    public boolean isConnected() {
        return isConnected;
    }

    public DeviceProperties getDeviceProperties(){
        return deviceProperties;
    }

    public boolean reboot(Status toStatus) throws InterruptedException, AdbException {
        return reboot(toStatus,false);
    }
    public boolean reboot(Status toStatus, boolean force) throws AdbException, InterruptedException {
        return reboot(toStatus,force,true);
    }
    private boolean reboot(Status toStatus, boolean force, boolean wait) throws AdbException, InterruptedException {
        requireAccessibile();
        if (!force && toStatus.equals(this.status)) {
            Log.debug("Device doesn't need reboot: current status ("+this.status+") is the same as wanted status ("+toStatus+")");
            return true;
        }
        return rebootInternal(toStatus,wait);
    }
    public boolean rebootNoWait(Status toStatus, boolean force) throws InterruptedException, AdbException {
        return reboot(toStatus, force, false);
    }
    public boolean rebootNoWait(Status toStatus) throws AdbException, InterruptedException {
        return rebootNoWait(toStatus, false);
    }
    private boolean rebootInternal(Status toStatus, boolean wait) throws InterruptedException, AdbException {
        deviceActiveSem.setPermits(0);
        if (Status.FASTBOOT.equals(this.status)){
            return fastbootReboot(toStatus, wait);
        } else {
            return adbReboot(toStatus, wait);
        }
    }
    private boolean fastbootReboot(Status toStatus, boolean wait) throws AdbException, InterruptedException {
        if (Status.FASTBOOT.equals(toStatus)){
            FastbootCommons.rebootBootloader(serial);
            return wait ? rebootWait(toStatus) : true;
        } else if (Status.EDL.equals(toStatus)){
            FastbootCommons.oemEdl(serial);
            return wait ? rebootWait(toStatus) : true;
        } else if (Status.SIDELOAD.equals(toStatus) || Status.RECOVERY.equals(toStatus)){
            if (FastbootCommons.oemRebootRecovery(serial)){
                return wait ? rebootNoWait(toStatus) : true;
            }
        }
        if (!Status.DEVICE.equals(toStatus) && !wait){
            throw new AdbException("Cannot reboot from fastboot to "+toStatus.toString()+" without waiting");
        }
        FastbootCommons.reboot(serial);
        if (!wait){
            return true;
        }
        if (!rebootWait(Status.DEVICE)){
            if (isConnected && toStatus.equals(this.status)){
                return true;
            }
            return false;
        }
        return reboot(toStatus,false);

    }


    private boolean rebootWait(Status toStatus) throws AdbException {
        try {
            Thread.sleep(1000);
            deviceActiveSem.setPermits(0);
            firstOffline = true;
            return waitStatus(toStatus);
        } catch (InterruptedException e) {
            Log.warn("Thread interrupted (bad): "+e.getMessage());
            throw new AdbException("The reboot wait thread was interrupted: "+e.getMessage());
        }
    }

    private boolean adbReboot(Status toStatus, boolean wait) throws AdbException {
        String status = toStatus.toString();
        if (Status.FASTBOOT.equals(toStatus)){
            status = "bootloader";
        } else if (Status.SIDELOAD.equals(toStatus)){
            status = Status.RECOVERY.toString();
        }
        AdbCommons.reboot(serial,status);
        return !wait || rebootWait(toStatus);
    }
    public void requireAccessibile() throws AdbException, InterruptedException {
        requireAccess();
        if (!isConnected){
            throw new AdbException("The device cannot be managed right now: not connected");
        }
        if(!(Status.FASTBOOT.equals(this.status) || Status.SIDELOAD.equals(this.status) || Status.RECOVERY.equals(this.status) || Status.DEVICE.equals(this.status))){
            throw new AdbException("The device cannot be managed right now: not accessibile status: "+this.status.toString());
        }
    }

    public boolean waitStatus(@NotNull Status status, int timeout) throws InterruptedException, AdbException {
        if(!deviceActiveSem.waitOnce(Integer.max(0,timeout-2))){
            canBeAccessed.waitOnce(3);
            if (!deviceActiveSem.waitOnce(1)){
                throw new AdbException("Waiting device active timed out");
            }
        }
        if (status.equals(this.status)){
            return true;
        }
        if (status.equals(Status.OFFLINE) && firstOffline){
            setConnected(false);
            firstOffline = false;
            return waitStatus(status,7);
        }
        throw new AdbException("Waited for device status "+status.toString()+", got "+(this.status == null ? "null" : this.status.toString()));
    }

    public boolean waitStatus(@NotNull Status status) throws InterruptedException, AdbException {
        switch (status){
            case DEVICE:
                return waitStatus(status, 80);
            case FASTBOOT:
                return waitStatus(status, 13);
            case SIDELOAD:
                return waitStatus(status, 40);
            case RECOVERY:
                return waitStatus(status, 40);
                default:
                    return waitStatus(status, 120);
        }
    }
    public boolean isTurnedOn(){
        return needAuthorization() || Status.DEVICE.equals(this.status);
    }
    public boolean needAuthorization(){
        return Status.OFFLINE.equals(this.status) || Status.UNAUTHORIZED.equals(this.status);
    }

    public Status waitActive(int timeout){
        try {
            this.deviceActiveSem.waitOnce(timeout);
        } catch (InterruptedException e) {
            return null;
        }
        return this.getStatus();
    }

    public DeviceAnswers getAnswers() {
        return deviceAnswers;
    }
}
