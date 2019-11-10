package com.xiaomitool.v2.procedure.install;

import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.procedure.Procedures;
import com.xiaomitool.v2.procedure.RInstall;
import com.xiaomitool.v2.procedure.RNode;
import com.xiaomitool.v2.procedure.device.ManageDevice;
import com.xiaomitool.v2.procedure.fetch.FastbootFetch;
import com.xiaomitool.v2.procedure.fetch.StockRecoveryFetch;
import com.xiaomitool.v2.procedure.uistuff.ConfirmationProcedure;


public class RecoverInstall {
    public static RInstall recoverFastboot(){
        return RNode.sequence(
                ConfirmationProcedure.suggestUnlockBootloader("Want to unlock boot?"),
                RNode.conditional(
                        ConfirmationProcedure.IS_DEVICE_UNLOCKED,
                                Procedures.doNothing(),
                                RNode.conditional(
                                        ConfirmationProcedure.WANT_TO_UNLOCK,
                                        RNode.sequence(
                                            FastbootInstall.unlockBootloader(),
                                            ManageDevice.waitRequireAccessible(10, Device.Status.FASTBOOT)),
                                        Procedures.doNothing()
                                )
                ),
                ManageDevice.requireUnlocked(),
                FastbootFetch.findBestRecoveryFastboot(),
                GenericInstall.resourceFetchWait(),
                GenericInstall.runInstallProcedure()
        );
    }

    public static RInstall recoverRecovery(){
        return RNode.sequence(
                StockRecoveryFetch.findBestRecoverRom(),
                GenericInstall.resourceFetchWait(),
                GenericInstall.runInstallProcedure()
        );
    }

}
