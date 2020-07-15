package com.xiaomitool.v2.procedure.install;

import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.*;
import com.xiaomitool.v2.procedure.device.ManageDevice;
import com.xiaomitool.v2.procedure.device.RebootDevice;
import com.xiaomitool.v2.procedure.fetch.FastbootFetch;
import com.xiaomitool.v2.procedure.fetch.StockRecoveryFetch;
import com.xiaomitool.v2.procedure.uistuff.ChooseProcedure;
import com.xiaomitool.v2.procedure.uistuff.ConfirmationProcedure;
import com.xiaomitool.v2.utility.CommandClass;
import com.xiaomitool.v2.utility.YesNoMaybe;

public class RecoverInstall {
    public static RInstall recoverFastboot() {
        return RNode.sequence(
                RebootDevice.requireFastboot(),
                ConfirmationProcedure.suggestUnlockBootloader(LRes.SUGGEST_UNLOCK_BL_RECOVER.toString(LRes.CONTINUE.toString())),
                RNode.conditional(
                        ConfirmationProcedure.IS_DEVICE_UNLOCKED,
                        Procedures.doNothing(),
                        RNode.conditional(
                                ConfirmationProcedure.WANT_TO_UNLOCK,
                                RNode.sequence(
                                        FastbootInstall.unlockBootloader(),
                                        ManageDevice.waitRequireAccessible(10, Device.Status.FASTBOOT)),
                                Procedures.throwRMessage(CommandClass.Command.ALTERNATIVE)
                        )
                ),
                ManageDevice.requireUnlocked(),
                FastbootFetch.findBestRecoveryFastboot(),
                GenericInstall.resourceFetchWait(),
                GenericInstall.runInstallProcedure()
        );
    }

    public static RInstall recoverRecovery() {
        return RNode.sequence(
                RebootDevice.requireStockRecoveryManual(),
                ConfirmationProcedure.isThisMiuiVersionCantBeInstalled(),
                StockRecoveryFetch.findBestRecoverRom(),
                GenericInstall.resourceFetchWait(),
                GenericInstall.runInstallProcedure()
        );
    }

    public static RInstall recoverDeviceOrderSubProcedures(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                Device.Status status = device.getStatus();
                RInstall nextProcedure = null;
                RInstall altProcedure = null;
                if (Device.Status.FASTBOOT.equals(status)){
                    nextProcedure = recoverFastboot();
                    altProcedure = recoverRecovery();
                } else if (Device.Status.RECOVERY.equals(status)) {
                    Log.info("Device in recovery mode: TWRP is installed probably");
                    nextProcedure = recoverFastboot();
                    altProcedure = recoverRecovery();
                } else if (Device.Status.SIDELOAD.equals(status)) {
                    nextProcedure = recoverRecovery();
                    altProcedure = recoverFastboot();
                } else {
                    throw new InstallException("The device is not in fastboot or recovery mode ("+ status +"), you should not use this feature", InstallException.Code.CANNOT_INSTALL);
                }
                Procedures.pushRInstallOnStack(runner, ChooseProcedure.alternativeBackupMethod(nextProcedure, altProcedure, LRes.ERROR_WHILE_RECOVERING.toString()));
            }
        }.next();
    }
}
