package com.xiaomitool.v2.procedure.install;

import com.xiaomitool.v2.adb.AdbCommons;
import com.xiaomitool.v2.adb.AdbException;
import com.xiaomitool.v2.adb.FastbootCommons;
import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.gui.visual.ProgressPane;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.*;
import com.xiaomitool.v2.procedure.device.ManageDevice;
import com.xiaomitool.v2.procedure.device.OtherProcedures;
import com.xiaomitool.v2.procedure.device.RebootDevice;
import com.xiaomitool.v2.rom.Installable;
import com.xiaomitool.v2.tasks.TaskManager;
import com.xiaomitool.v2.tasks.TwrpInstallTask;
import com.xiaomitool.v2.tasks.UpdateListener;
import com.xiaomitool.v2.utility.NotNull;
import com.xiaomitool.v2.utility.RunnableWithArg;
import com.xiaomitool.v2.utility.YesNoMaybe;
import com.xiaomitool.v2.utility.utils.StrUtils;
import javafx.application.Platform;

import java.io.File;

public class TwrpInstall  {
    public static RInstall flashTwrp(){
        //Log.printStackTrace(new Exception());
        return RNode.sequence(RebootDevice.requireFastboot(), new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Installable installable =  Procedures.requireInstallable(runner);
                Device device = Procedures.requireDevice(runner);
                runner.text(LRes.FLASHING_STUFF.toString(LRes.TWRP_RECOVERY.toString()));
                Log.info("Flashing recovery via fastboot, file: "+installable.getFinalFile());
                String flashOutput = FastbootCommons.flash(device.getSerial(),installable.getFinalFile(),"recovery");
                if ("err:anti-rollback".equals(flashOutput)){
                    Log.warn("Anti rollback protection detected, flashing dummy");
                    FastbootCommons.flashDummy(device.getSerial());
                    flashOutput = FastbootCommons.flash(device.getSerial(),installable.getFinalFile(),"recovery");
                }


                if (flashOutput == null || flashOutput.startsWith("err:")){
                    throw new InstallException(new AdbException("Fastboot flash recovery failed, output:" + StrUtils.str(flashOutput)));
                }
                runner.text(LRes.BOOTING_STUFF.toString(LRes.TWRP_RECOVERY.toString()));
                Log.info("Booting bootable via fastboot, file: "+installable.getFinalFile());
                FastbootCommons.boot(device.getSerial(),installable.getFinalFile());
                Thread.sleep(3000);
                device.setConnected(false);
            }
        }, GenericInstall.updateDeviceStatus(null,true,null));
    }

    public static RInstall checkIfIsInTwrp() {
        return RNode.sequence(RebootDevice.requireRecovery(), OtherProcedures.sleep(2000), ManageDevice.requireAccessible(true),  new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                Log.info("Checking if device is in TWRP recovery");
                if (YesNoMaybe.NO.equals(device.getAnswers().isInTwrpRecovery())){
                    throw new InstallException("Failed to get twrp commands running, are you in twrp?", InstallException.Code.TWRP_INSTALL_FAILED, true);
                }
            }
        });
    }
private static final String ERASE_DATA_KEY = "erase_the_data";

    @ExportFunction("install_zip_viatwrp")
    public static RInstall installZip(){
        return RNode.sequence(RebootDevice.requireRecovery(), new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                String path = "/sdcard/xmt_push/";
                runner.text(LRes.CREATING_DEST_DIR);
                if (!AdbCommons.fileExists(path, device.getSerial())){
                    Log.warn("Device destination directory doesn't exists, creating: "+path);
                    if (AdbCommons.adb_shellWithOr("mkdir "+path, device.getSerial(), 4) == null){

                        path = "/sdcard/";
                        Log.warn("Failed to create destination dir, switching to: "+path);
                    }
                }
                runner.setContext(AdbInstall.DESTINATION_PATH, path);
            }
        }, AdbInstall.pushInstallableFile(), new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                Installable installable = Procedures.requireInstallable(runner);
                File file = installable.getFinalFile();
                boolean eraseData = true;
                if (file != null && file.exists()){
                    eraseData = file.length() > 300000000;
                }
                if (eraseData){
                    Log.warn("The file to install is a large zip file, likely a zip rom, we must erase data after that");
                } else {
                    Log.info("The file to install is a small zip file, likely a mod zip, we don't have to erase data after that");
                }
                runner.setContext(ERASE_DATA_KEY, Boolean.valueOf(eraseData));
                String outputPath = (String) runner.requireContext(AdbInstall.OUTPUT_DST_PATH);
                //ProgressPane.DefProgressPane progressPane = new ProgressPane.DefProgressPane();
                UpdateListener listener = new UpdateListener();
                listener.addOnStart(new UpdateListener.OnStart() {
                    @Override
                    public void run(long totalSize) {
                        runner.text(LRes.INSTALL_TWRP_ZIP);
                    }
                });
                TwrpInstallTask task = new TwrpInstallTask(listener, device.getSerial(), outputPath, new RunnableWithArg() {
                    @Override
                    public void run(Object arg) {
                        runner.text((String) arg);
                    }
                });
                TaskManager.getInstance().startSameThread(task);
                task.waitFinished();
                WindowManager.removeTopContent();
                if (task.getError() != null || !task.isFinished()){
                    throw new InstallException("Failed to install the zip file on the device: task failed: "+((task.getError() != null) ? task.getError().getMessage() : "not finished"), InstallException.Code.TWRP_INSTALL_FAILED, true);
                }


            }
        }, RNode.conditional(ERASE_DATA_KEY, wipeDataCacheOnTwrp(), wipeCacheOnTwrp()));
    }

    public static RInstall wipeCacheOnTwrp(){
        return RNode.sequence(RebootDevice.requireRecovery(), new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                boolean result = false;
                result |= (AdbCommons.adb_shell("twrp wipe cache",device.getSerial(), 8) != null);
               // result |= (AdbCommons.adb_shell("twrp wipe data", device.getSerial(), 10) != null);
                if (!result){
                    throw new InstallException("Failed to wipe cache: twrp command failed", InstallException.Code.WIPE_FAILED, true ) ;
                }
            }
        });
    }

    public static RInstall wipeDataCacheOnTwrp(){
        return RNode.sequence(RebootDevice.requireRecovery(), new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                boolean result = false;
                result |= (AdbCommons.adb_shell("twrp wipe cache",device.getSerial(), 8) != null);
                result |= (AdbCommons.adb_shell("twrp wipe data", device.getSerial(), 10) != null);
                if (!result){
                    throw new InstallException("Failed to wipe cache and data: twrp command failed", InstallException.Code.WIPE_FAILED, true ) ;
                }
            }
        }, GenericInstall.updateDeviceStatus(null,null,false));
    }


}
