package com.xiaomitool.v2.procedure.device;

import com.xiaomitool.v2.adb.AdbException;
import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.adb.device.DeviceAnswers;
import com.xiaomitool.v2.adb.device.DeviceManager;
import com.xiaomitool.v2.engine.actions.ActionsDynamic;
import com.xiaomitool.v2.gui.WindowManager;
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
        return RNode.sequence(requireAccessible(),
                RNode.fallback(RNode.sequence(RebootDevice.rebootRecovery(true, false, 15), TwrpInstall.checkIfIsInTwrp(), new RInstall() {
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
                        ActionsDynamic.HOWTO_GO_RECOVERY(device).run();
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
    public static RInstall requireAccessible(){
        return requireAccessible(true);
    }
    public static RInstall requireNoUnauthOffline(boolean refresh){
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

    public static RInstall requireConnected(boolean refresh){
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
                //ActionsDynamic.REQUIRE_DEVICE_AUTH(device).run();

            }
        };
    }

    public static RInstall waitRequireAccessible(int timeout, Device.Status showTextExpectedDeviceStatus){
        return RNode.sequence(RNode.setSkipOnException(waitDevice(timeout, showTextExpectedDeviceStatus)), requireAccessible(true));
    }

    public static RInstall requireAccessible(boolean refresh){
        return RNode.sequence(requireConnected(refresh), requireNoUnauthOffline(refresh));
    }


    public static RInstall waitDevice(int timeout, Device.Status showTextExpectedDeviceStatus){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                if (showTextExpectedDeviceStatus != null){
                    String connectionType = Device.Status.DEVICE.equals(showTextExpectedDeviceStatus) ? "ADB":  showTextExpectedDeviceStatus.toString();
                    runner.text(LRes.WAITING_DEVICE_ACTIVE.toString(connectionType));
                }
                int t = timeout;
                if (t < 2){
                    t = 2;
                }
                t-=2;
                Thread.sleep(1500);
                device.setConnected(false);
                DeviceManager.refresh();
                if (device.waitActive(t) == null){
                    throw new InstallException("Waited device for "+timeout+" seconds but it wasn't active", InstallException.Code.WAIT_DEVICE_TIMEOUT, true);
                }

            }
        };
    }
}
