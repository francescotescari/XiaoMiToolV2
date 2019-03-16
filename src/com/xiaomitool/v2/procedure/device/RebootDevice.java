package com.xiaomitool.v2.procedure.device;

import com.xiaomitool.v2.adb.AdbException;
import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.adb.device.DeviceManager;
import com.xiaomitool.v2.engine.CommonsMessages;
import com.xiaomitool.v2.engine.actions.ActionsDynamic;
import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.gui.visual.ButtonPane;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.*;
import com.xiaomitool.v2.procedure.install.InstallException;

public class RebootDevice {

    public static RInstall rebootDevice(boolean wait, boolean force){
        return RNode.sequence(ManageDevice.requireAccessible(),new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                boolean result = false;
                runner.text(LRes.REBOOTING_TO_MODE.toString(Device.Status.DEVICE.toString()));
                try {
                    if (wait) {
                        runner.text(LRes.WAITING_DEVICE_ACTIVE.toString(Device.Status.DEVICE.toString()));
                        result = device.reboot(Device.Status.DEVICE, force);
                    } else {
                        result = device.rebootNoWait(Device.Status.DEVICE, force);
                    }
                } catch (AdbException e){
                    throw new InstallException(e);
                }
                if (!result){
                    throw new InstallException("Failed to reboot device to recovery mode", InstallException.Code.REBOOT_FAILED, false);
                }
            }
        });
    }

    public static RInstall rebootRecovery(boolean wait, boolean force){
        return rebootRecovery(wait,force,-1);
    }
    public static RInstall rebootRecovery(boolean wait, boolean force, int timeout){
        return RNode.sequence(ManageDevice.requireAccessible(),rebootDeviceIfNoAdbAccessible(),new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                boolean result = false;
                runner.text(LRes.REBOOTING_TO_MODE.toString(Device.Status.RECOVERY.toString()));
                try {
                    if (wait) {
                        runner.text(LRes.WAITING_DEVICE_ACTIVE.toString(Device.Status.RECOVERY.toString()));
                        if (timeout <= 0) {
                            result = device.reboot(Device.Status.RECOVERY, force);
                        } else {
                            result = device.rebootNoWait(Device.Status.RECOVERY, force);
                            Thread.sleep(2000);
                            device.setConnected(false);
                            result = result && device.waitStatus(Device.Status.RECOVERY, timeout);
                        }
                    } else {
                        result = device.rebootNoWait(Device.Status.RECOVERY, force);
                    }
                } catch (AdbException e){
                    throw new InstallException(e);
                }
                if (!result){
                    throw new InstallException("Failed to reboot device to recovery mode", InstallException.Code.REBOOT_FAILED, false);
                }
            }
        });
    }

    public static RInstall rebootNoWaitIfConnected(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                try {
                    device.rebootNoWait(Device.Status.DEVICE,false);
                } catch (Throwable e) {
                    Log.debug("Device not rebooted, but it doesn't matter");
                }
            }
        };
    }

    public static RInstall requireRecovery(){
        return RNode.sequence(new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                ManageDevice.refreshDevices().run(runner);
                if (Device.Status.RECOVERY.equals(device.getStatus()) && device.isConnected()){
                    return;
                }
                boolean shouldRebootAgain = false;
                while (true) {
                    Log.debug(device.getStatus());
                    try {
                        rebootRecovery(true, false).setFlag(RNode.FLAG_THROWRAWEXCEPTION, true).run(runner);
                        return;
                    } catch (InstallException e) {
                        Log.debug("Executing got here");
                        Log.debug(e);
                        Log.debug(e.getMessage());
                        Log.debug(e.getCode());
                        ButtonPane buttonPane = new ButtonPane(LRes.TRY_AGAIN, LRes.ABORT);
                        buttonPane.setContentText(LRes.REBOOT_RECOVERY_FAILED.toString(e.getMessage()));

                        DeviceManager.addMessageReceiver(buttonPane.getIdClickReceiver());
                        WindowManager.setMainContent(buttonPane, false);

                        int click = buttonPane.waitClick();
                        shouldRebootAgain = false;
                        while (true){
                            if (click == CommonsMessages.DEVICE_UPDATE_FINISH){
                                if (Device.Status.RECOVERY.equals(device.getStatus()) && device.isConnected()){
                                    break;
                                }
                            } else if (click == 1){
                                throw InstallException.ABORT_EXCEPTION;
                            } else if (click == 0){
                                ManageDevice.refreshDevices().run(runner);
                                if (Device.Status.RECOVERY.equals(device.getStatus()) && device.isConnected()){
                                    break;
                                } else {
                                    shouldRebootAgain = true;
                                    break;
                                }
                            }
                            click = buttonPane.waitClick();
                        }
                        WindowManager.removeTopContent();
                        if (!shouldRebootAgain){
                            return;
                        }

                    } finally {
                        Log.debug("Finally got executed");
                    }
                }
            }
        });
    }

    public static RInstall rebootBootloader(boolean wait, boolean force){
        return RNode.sequence(ManageDevice.requireAccessible(),new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                boolean result = false;
                runner.text(LRes.REBOOTING_TO_MODE.toString(Device.Status.FASTBOOT.toString()));
                try {
                    if (wait) {
                        runner.text(LRes.WAITING_DEVICE_ACTIVE.toString(Device.Status.FASTBOOT.toString()));
                        result = device.reboot(Device.Status.FASTBOOT, force);
                    } else {
                        result = device.rebootNoWait(Device.Status.FASTBOOT, force);
                    }
                } catch (AdbException e){
                    throw new InstallException(e);
                }
                if (!result){
                    throw new InstallException("Failed to reboot device to fastboot mode", InstallException.Code.REBOOT_FAILED, false);
                }
            }
        });
    }

    public static RInstall requireFastboot(){
        return RNode.sequence(new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                ManageDevice.refreshDevices().run(runner);
                if ((Device.Status.FASTBOOT.equals(device.getStatus()) && device.isConnected())){
                    return;
                }
                boolean shouldRebootAgain = false;
                while (true) {
                    Log.debug(device.getStatus());
                    try {
                        rebootBootloader(true, false).setFlag(RNode.FLAG_THROWRAWEXCEPTION, true).run(runner);
                        return;
                    } catch (Exception e) {
                        ButtonPane buttonPane = new ButtonPane(LRes.TRY_AGAIN, LRes.ABORT);
                        buttonPane.setContentText(LRes.REBOOT_BOOTLOADER_FAILED.toString(e.getMessage()));

                        DeviceManager.addMessageReceiver(buttonPane.getIdClickReceiver());
                        WindowManager.setMainContent(buttonPane, false);

                        int click = buttonPane.waitClick();
                        shouldRebootAgain = false;
                        while (true){
                            if (click == CommonsMessages.DEVICE_UPDATE_FINISH){
                                if ((Device.Status.FASTBOOT.equals(device.getStatus()) && device.isConnected())){
                                    break;
                                }
                            } else if (click == 1){
                                throw InstallException.ABORT_EXCEPTION;
                            } else if (click == 0){
                                ManageDevice.refreshDevices().run(runner);
                                if ((Device.Status.FASTBOOT.equals(device.getStatus())  && device.isConnected())){
                                    break;
                                } else {
                                    shouldRebootAgain = true;
                                    break;
                                }
                            }
                            click = buttonPane.waitClick();
                        }
                        WindowManager.removeTopContent();
                        if (!shouldRebootAgain){
                            return;
                        }

                    }
                }
            }
        });
    }

    public static RInstall rebootStockRecovery(boolean force){
        return RNode.sequence(ManageDevice.requireAccessible(),rebootDeviceIfNoAdbAccessible(),new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                boolean result = ActionsDynamic.REBOOT_STOCK_RECOVERY(device,force).run() != 0;
                if (!result){
                    throw new InstallException("Failed to reboot device to stock recovery mode", InstallException.Code.REBOOT_FAILED, false);
                }
            }
        });
    }

    private static RInstall rebootDeviceIfNoAdbAccessible(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                Device.Status status = device.getStatus();
                if (!Device.Status.DEVICE.equals(status) && !Device.Status.SIDELOAD.equals(status) && !Device.Status.RECOVERY.equals(status)){
                    rebootDevice(true,false).run(runner);
                }

            }
        };
    }

    public static RInstall requireStockRecovery(){
        return RNode.sequence(new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                ManageDevice.refreshDevices().run(runner);
                if ((Device.Status.SIDELOAD.equals(device.getStatus()) && device.isConnected())){
                    return;
                }
                boolean shouldRebootAgain = false;
                while (true) {
                    Log.debug(device.getStatus());
                    try {
                        rebootStockRecovery(false).setFlag(RNode.FLAG_THROWRAWEXCEPTION, true).run(runner);
                        return;
                    } catch (Exception e) {
                        ButtonPane buttonPane = new ButtonPane(LRes.TRY_AGAIN, LRes.ABORT);
                        buttonPane.setContentText(LRes.REBOOT_STOCKRECOVERY_FAILED.toString(e.getMessage()));

                        DeviceManager.addMessageReceiver(buttonPane.getIdClickReceiver());
                        WindowManager.setMainContent(buttonPane, false);

                        int click = buttonPane.waitClick();
                        shouldRebootAgain = false;
                        while (true){
                            if (click == CommonsMessages.DEVICE_UPDATE_FINISH){
                                if ((Device.Status.SIDELOAD.equals(device.getStatus()) && device.isConnected())){
                                    break;
                                }
                            } else if (click == 1){
                                throw InstallException.ABORT_EXCEPTION;
                            } else if (click == 0){
                                ManageDevice.refreshDevices().run(runner);
                                if ((Device.Status.SIDELOAD.equals(device.getStatus())  && device.isConnected())){
                                    break;
                                } else {
                                    shouldRebootAgain = true;
                                    break;
                                }
                            }
                            click = buttonPane.waitClick();
                        }
                        WindowManager.removeTopContent();
                        if (!shouldRebootAgain){
                            return;
                        }

                    }
                }
            }
        });
    }
}