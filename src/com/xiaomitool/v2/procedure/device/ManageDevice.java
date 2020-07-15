package com.xiaomitool.v2.procedure.device;

import com.xiaomitool.v2.adb.AdbCommunication;
import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.adb.device.DeviceAnswers;
import com.xiaomitool.v2.adb.device.DeviceManager;
import com.xiaomitool.v2.adb.device.DeviceProperties;
import com.xiaomitool.v2.engine.actions.ActionsDynamic;
import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.procedure.*;
import com.xiaomitool.v2.procedure.fetch.GenericFetch;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.procedure.install.TwrpInstall;
import com.xiaomitool.v2.utility.Pointer;
import com.xiaomitool.v2.utility.YesNoMaybe;
import com.xiaomitool.v2.xiaomi.miuithings.UnlockStatus;

import java.util.HashMap;

public class ManageDevice {
    private static final String DEIVCE_CODENAME_KEY = "DDD_CODE";

    public static RInstall refreshDevices() {
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                ActionsDynamic.MAIN_SCREEN_LOADING(LRes.LOADING).run();
                DeviceManager.waitRefresh();
                WindowManager.removeTopContent();
            }
        };
    }

    public static RInstall checkIfTwrpInstalled() {
        return RNode.sequence(requireAccessible(), OtherProcedures.sleep(1000),
                RNode.fallback(RNode.sequence(RebootDevice.rebootRecovery(true, false, 30), TwrpInstall.checkIfIsInTwrp(), new RInstall() {
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

    public static RInstall requireDeviceUsbDebug() {
        return RNode.sequence(RebootDevice.rebootNoWaitIfConnected(), new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                ActionsDynamic.WAIT_USB_DEBUG_ENABLE(device).run();
            }
        });
    }

    public static RInstall requireAccessible() {
        return requireAccessible(true);
    }

    public static RInstall requireNoUnauthOffline(boolean refresh) {
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                if (refresh) {
                    DeviceManager.refresh(false);
                }
                Device.Status status = device.getStatus();
                if (!(Device.Status.UNAUTHORIZED.equals(status) || Device.Status.OFFLINE.equals(status))) {
                    return;
                }
                ActionsDynamic.REQUIRE_DEVICE_AUTH(device).run();
            }
        };
    }

    public static RInstall requireConnected(boolean refresh) {
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                if (refresh) {
                    DeviceManager.refresh(false);
                }
                if (device.isConnected()) {
                    return;
                }
                ActionsDynamic.REQUIRE_DEVICE_CONNECTED(device).run();
            }
        };
    }

    public static RInstall waitRequireAccessible(int timeout, Device.Status showTextExpectedDeviceStatus) {
        return RNode.sequence(RNode.setSkipOnException(waitDevice(timeout, showTextExpectedDeviceStatus)), requireAccessible(true));
    }

    public static RInstall requireAccessible(boolean refresh) {
        return RNode.sequence(requireConnected(refresh), requireNoUnauthOffline(refresh));
    }

    public static RInstall waitDevice(int timeout, Device.Status showTextExpectedDeviceStatus) {
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                if (showTextExpectedDeviceStatus != null) {
                    String connectionType = Device.Status.DEVICE.equals(showTextExpectedDeviceStatus) ? "ADB" : showTextExpectedDeviceStatus.toString();
                    runner.text(LRes.WAITING_DEVICE_ACTIVE.toString(connectionType));
                }
                int t = timeout;
                if (t < 2) {
                    t = 2;
                }
                t -= 2;
                Thread.sleep(1500);
                device.setConnected(false);
                DeviceManager.refresh(true);
                if (device.waitActive(t) == null) {
                    throw new InstallException("Waited device for " + timeout + " seconds but it seems still disconnected", InstallException.Code.WAIT_DEVICE_TIMEOUT);
                }
            }
        };
    }

    public static RInstall requireDeviceCodename(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                if (device.getDeviceProperties().getCodename(true) != null){
                    return;
                }
                selectDeviceCodename().run(runner);
            }
        };
    }

    public static RInstall selectDeviceCodename() {
        return RNode.sequence(GenericFetch.fetchDeviceCodename(DEIVCE_CODENAME_KEY), new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                HashMap<String, String> devicesCodename = (HashMap<String, String>) runner.getContext(DEIVCE_CODENAME_KEY);
                if (devicesCodename == null) {
                    GenericFetch.fetchDeviceCodename(DEIVCE_CODENAME_KEY).run(runner);
                    devicesCodename = (HashMap<String, String>) runner.requireContext(DEIVCE_CODENAME_KEY);
                }
                Pointer pointer = new Pointer();
                int res = ActionsDynamic.SELECT_DEVICE_CODENAME(pointer, devicesCodename).run();
                if (res == 0) {
                    throw InstallException.ABORT_EXCEPTION;
                }
                device.getDeviceProperties().userSet(DeviceProperties.CODENAME, pointer.pointed);
            }
        });
    }

    public static RInstall recoverSelectDevice() {
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                int res = ActionsDynamic.SEARCH_SELECT_DEVICES(Device.Status.FASTBOOT, Device.Status.RECOVERY, Device.Status.SIDELOAD).run();
                runner.setContext(Procedures.SELECTED_DEVICE, DeviceManager.getSelectedDevice());
            }
        };
    }

    public static RInstall requireUnlocked() {
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                if (!UnlockStatus.UNLOCKED.equals(device.getAnswers().getUnlockStatus())) {
                    throw new InstallException("Bootloader locked. It is necessary to unlock the bootlaoder to continue.", InstallException.Code.UNLOCK_ERROR);
                }
            }
        };
    }

    public static RInstall requireAdbCheckService() {
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                if (!AdbCommunication.isAutoScanRegistered()) {
                    AdbCommunication.registerAutoScanDevices();
                }
            }
        };
    }
}
