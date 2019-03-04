package com.xiaomitool.v2.procedure.device;

import com.xiaomitool.v2.adb.AdbException;
import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.adb.device.DeviceAnswers;
import com.xiaomitool.v2.adb.device.DeviceManager;
import com.xiaomitool.v2.engine.actions.ActionsDynamic;
import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.gui.visual.ButtonPane;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.procedure.*;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.procedure.install.TwrpInstall;
import com.xiaomitool.v2.utility.YesNoMaybe;

public class ManageDevice {
    public static RInstall refreshDevices(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                ActionsDynamic.MAIN_SCREEN_LOADING(LRes.LOADING).run();
                DeviceManager.refresh();
                WindowManager.removeTopContent();
            }
        };
    }

    public static RInstall checkIfTwrpInstalled(){
        return RNode.sequence(new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                try {
                    device.requireAccessibile();
                } catch (AdbException e) {
                    throw new InstallException(e);
                }
            }
        },
                RNode.fallback(RNode.sequence(RebootDevice.rebootRecovery(true, false), TwrpInstall.checkIfIsInTwrp(), new RInstall() {
                    @Override
                    public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                        Device device = Procedures.requireDevice(runner);
                        device.getAnswers().setAnswer(DeviceAnswers.HAS_TWRP, YesNoMaybe.YES);
                    }
                }), new RInstall() {
                    @Override
                    public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                        Device device = Procedures.requireDevice(runner);
                        device.getAnswers().setAnswer(DeviceAnswers.HAS_TWRP, YesNoMaybe.NO);
                    }
                }));

    }

    public static RInstall requireDeviceUsbDebug(){
        return RNode.sequence(RebootDevice.rebootNoWaitIfConnected(), new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                ActionsDynamic.WAIT_USB_DEBUG_ENABLE(device).run();
            }
        });
    }
    public static RInstall checkAccessible(){
        return checkAccessible(true);
    }
    public static RInstall checkNoUnauthOffline(boolean refresh){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                if (refresh) {
                    DeviceManager.refresh();
                }
                Device.Status status = device.getStatus();
                if (!(Device.Status.UNAUTHORIZED.equals(status) || Device.Status.OFFLINE.equals(status))){
                    return;
                }
                ActionsDynamic.REQUIRE_DEVICE_AUTH(device).run();

            }
        };
    }

    public static RInstall checkConnected(boolean refresh){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                if (refresh) {
                    DeviceManager.refresh();
                }
                if (device.isConnected()){
                    return;
                }
                ActionsDynamic.REQUIRE_DEVICE_CONNECTED(device).run();

            }
        };
    }

    public static RInstall checkAccessible(boolean refresh){
        return RNode.sequence(checkConnected(refresh), checkNoUnauthOffline(refresh));
    }


    public static RInstall waitDevice(int timeout){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);

                if (device.waitActive(timeout) == null){
                    throw new InstallException("Waited device for "+timeout+" seconds but it wasn't active", InstallException.Code.WAIT_DEVICE_TIMEOUT, true);
                }

            }
        };
    }
}
