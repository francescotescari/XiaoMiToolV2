package com.xiaomitool.v2.procedure.install;

import com.xiaomitool.v2.adb.AdbCommons;
import com.xiaomitool.v2.adb.AdbCommunication;
import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.engine.actions.ActionsDynamic;
import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.gui.visual.ProgressPane;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.*;
import com.xiaomitool.v2.procedure.device.RebootDevice;
import com.xiaomitool.v2.process.AdbRunner;
import com.xiaomitool.v2.resources.ResourcesConst;
import com.xiaomitool.v2.rom.Installable;
import com.xiaomitool.v2.rom.MiuiRom;
import com.xiaomitool.v2.tasks.AdbSideloadTask;
import com.xiaomitool.v2.tasks.Task;
import com.xiaomitool.v2.tasks.TaskManager;
import com.xiaomitool.v2.tasks.UpdateListener;
import com.xiaomitool.v2.utility.DriverUtils;
import com.xiaomitool.v2.utility.MTPUtils;
import com.xiaomitool.v2.utility.Pointer;
import com.xiaomitool.v2.utility.utils.StrUtils;
import com.xiaomitool.v2.xiaomi.miuithings.Branch;
import com.xiaomitool.v2.xiaomi.miuithings.DeviceRequestParams;
import com.xiaomitool.v2.xiaomi.miuithings.SerialNumber;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StockRecoveryInstall {
    public static final String SELECTED_MTP_DEVICE = "selected_mtp_device";

    static RInstall enableMtp() {
        return RNode.sequence(new RInstall() {
            @Override
            public void run(ProcedureRunner procedureRunner) throws InstallException, InterruptedException, RMessage {
                Device device = (Device) procedureRunner.requireContext(Procedures.SELECTED_DEVICE);
                HashMap<String, MTPUtils.MTPDevice> initDeviceMap;
                procedureRunner.text(LRes.SEARCHING_CONNECTED_MTP_DEVICES);
                Log.info("Searching MTP devices before mtp enable");
                try {
                    initDeviceMap = MTPUtils.list();
                } catch (IOException e) {
                    throw new InstallException("MTP list devices failed: " + e.getMessage(), InstallException.Code.MTP_FAILED, e);
                }
                procedureRunner.text(LRes.MTP_ENABLING_DEVICE);
                Log.info("Enabling MTP on the device");
                String out = AdbCommons.raw(device.getSerial(), "enablemtp:");
                if (out == null) {
                    throw new InstallException("Adb enablemtp command failed, maybe your device doesn't support it or device is not connected", InstallException.Code.ADB_EXCEPTION, "Last error: " + AdbCommons.getLastError(device.getSerial()));
                }
                Thread.sleep(2000);
                procedureRunner.text(LRes.SEARCHING_CONNECTED_MTP_DEVICES);
                Log.info("Searching MTP devices after mtp enable");
                HashMap<String, MTPUtils.MTPDevice> afterDeviceMap;
                try {
                    afterDeviceMap = MTPUtils.list();
                } catch (IOException e) {
                    throw new InstallException("MTP list devices failed: " + e.getMessage(), InstallException.Code.MTP_FAILED, e);
                }
                String newDeviceKey = null;
                List<MTPUtils.MTPDevice> xiaomiDevices = new ArrayList<>();
                for (String key : afterDeviceMap.keySet()) {
                    if (initDeviceMap.get(key) == null) {
                        Log.info("New MTP device found");
                        newDeviceKey = key;
                    }
                    if (key.toLowerCase().contains("vid_2717")) {
                        Log.info("New xiaomi MTP device found: " + key);
                        xiaomiDevices.add(afterDeviceMap.get(key));
                    }
                    Log.info("Skipping MTP device " + key + ", it was already in list");
                }
                if (newDeviceKey != null && newDeviceKey.toLowerCase().contains("vid_2717")) {
                    Log.info("Using new MTP device found: " + newDeviceKey);
                    procedureRunner.text(LRes.MTP_DEVICE_SELECTED);
                    procedureRunner.setContext(SELECTED_MTP_DEVICE, afterDeviceMap.get(newDeviceKey));
                    return;
                }
                Log.info("Searching right Xiaomi MTP device by root name == data");
                for (MTPUtils.MTPDevice device1 : xiaomiDevices) {
                    try {
                        String root = MTPUtils.getRoot(device1);
                        if (root != null && "data".equals(root.toLowerCase().trim())) {
                            Log.info("Xiaomi device with data == root name found: " + device1.id);
                            procedureRunner.text(LRes.MTP_DEVICE_SELECTED);
                            procedureRunner.setContext(SELECTED_MTP_DEVICE, device1);
                            return;
                        }
                    } catch (IOException e) {
                        Log.warn("Cannot get device root of " + device1.id + ": " + e.getMessage());
                    }
                }
                Log.warn("Xiaomi MTP device not found, might be a driver issue");
                Integer trial = (Integer) procedureRunner.consumeContext("TRIAL");
                if (trial == null) {
                    trial = 0;
                }
                if (trial < 2) {
                    fixMtpDevices().run(procedureRunner);
                    boolean result = (boolean) procedureRunner.consumeContext("FIXMTP");
                    if (result) {
                        procedureRunner.setContext("TRIAL", trial + 1);
                        Thread.sleep(1000);
                        this.run(procedureRunner);
                        return;
                    }
                }
                throw new InstallException("Cannot detect recovery mtp device, try updating the mtp driver on the device", InstallException.Code.MTP_FAILED);
            }
        }, GenericInstall.updateDeviceStatus(null, null, false));
    }

    static RInstall fixMtpDevices() {
        return new RInstall() {
            @Override
            public void run(ProcedureRunner procedureRunner) throws InstallException, InterruptedException {
                Log.info("Fixing MTP drivers");
                procedureRunner.text(LRes.DRIVER_FIXING_MTP);
                boolean res = DriverUtils.fixMtpDevices();
                procedureRunner.setContext("FIXMTP", res);
            }
        };
    }

    static RInstall sendFileViaMTP() {
        return new RInstall() {
            @Override
            public void run(ProcedureRunner procedureRunner) throws InstallException, InterruptedException {
                Installable installable = (Installable) procedureRunner.requireContext(Procedures.INSTALLABLE);
                File finalFile = installable.getFinalFile();
                if (finalFile == null) {
                    throw new InstallException("Null install file", InstallException.Code.INTERNAL_ERROR);
                }
                Path file = finalFile.toPath();
                procedureRunner.text(LRes.MTP_SENDING_FILE);
                if (!Files.exists(file)) {
                    throw new InstallException("File " + file.toString() + " doesn't exists!", InstallException.Code.FILE_NOT_FOUND);
                }
                Log.info("Sending file: " + file + " to the device using MTP");
                MTPUtils.MTPDevice device = (MTPUtils.MTPDevice) procedureRunner.requireContext(SELECTED_MTP_DEVICE);
                Task task = MTPUtils.getPushTask(device, file, "/");
                ProgressPane.DefProgressPane progressPane = new ProgressPane.DefProgressPane();
                progressPane.setContentText(LRes.MTP_SENDING_FILE);
                UpdateListener listener = progressPane.getUpdateListener(150);
                task.setListener(listener);
                WindowManager.setMainContent(progressPane, false);
                TaskManager.getInstance().startSameThread(task);
                WindowManager.removeTopContent();
                Exception error = task.getError();
                if (error != null) {
                    throw new InstallException("Failed to send file to mtp device: " + error.getMessage(), InstallException.Code.MTP_FAILED, error);
                }
                procedureRunner.text(LRes.FILE_SENT_TO_DEVICE);
                Log.info("File MTP sent to the device succesfully");
            }
        };
    }

    static RInstall sidelaodFile() {
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                Installable installable = Procedures.requireInstallable(runner);
                String token = installable.getInstallToken();
                runner.text(LRes.STARTING_MIUI_SIDELOAD);
                if (StrUtils.isNullOrEmpty(token)) {
                    throw new InstallException("Empty install token", InstallException.Code.SIDELOAD_INSTALL_FAILED, "Installable " + installable + " has no install token");
                }
                Log.info("Starting miui sidelaod of file: " + installable.getFinalFile());
                Log.info("Install token: " + token);
                AdbSideloadTask sideloadTask = new AdbSideloadTask(installable.getFinalFile(), token, device.getSerial());
                ProgressPane.DefProgressPane defProgressPane = new ProgressPane.DefProgressPane();
                UpdateListener listener = defProgressPane.getUpdateListener(300);
                final Pointer textInstalling = new Pointer();
                textInstalling.pointed = false;
                listener.addOnUpdate(new UpdateListener.OnUpdate() {
                    @Override
                    public void run(long downloaded, long totalSize, Duration latestDuration, Duration totalDuration) {
                        if (downloaded / totalSize >= 0.5 && !((boolean) textInstalling.pointed)) {
                            textInstalling.pointed = true;
                            defProgressPane.setContentText(LRes.SIDELOAD_INSTALLING_FILE.toString() + "\n" + LRes.DONT_REBOOT_DEVICE);
                        }
                    }
                });
                sideloadTask.setListener(listener);
                defProgressPane.setContentText(LRes.ADB_PUSHING_FILE + "\n" + LRes.DONT_REBOOT_DEVICE);
                defProgressPane.setText(LRes.STARTING_MIUI_SIDELOAD);
                WindowManager.setMainContent(defProgressPane, false);
                TaskManager.getInstance().startSameThread(sideloadTask);
                WindowManager.removeTopContent();
                Log.info("Sideload finished with progress: " + sideloadTask.getLatestUpdate() + "/" + sideloadTask.getTotalSize());
                if (!sideloadTask.isFinished()) {
                    Exception e = sideloadTask.getError();
                    throw new InstallException("MIUI sideload failed: " + e.getMessage(), InstallException.Code.SIDELOAD_INSTALL_FAILED, e);
                }
                Log.info("Sideload task finished succesfully");
            }
        };
    }

    static RInstall installMtpFile() {
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                Installable installable = Procedures.requireInstallable(runner);
                runner.text(LRes.MTP_PREPARE_INSTALL);
                Thread.sleep(2000);
                String token = installable.getInstallToken();
                if (token == null || token.isEmpty()) {
                    throw new InstallException("Empty install token", InstallException.Code.MTP_INSTALL_FAILED);
                }
                Log.info("Starting MTP installation");
                Log.info("Install token: " + token);
                AdbRunner adbRunner = new AdbRunner("raw", "mtpinstall:" + token);
                adbRunner.setDeviceSerial(device.getSerial());
                runner.text(LRes.MTP_INSTALLING_FILE);
                ProgressPane.DefProgressPane progressPane = new ProgressPane.DefProgressPane();
                progressPane.setProgress(-1d);
                progressPane.setContentText(LRes.MTP_INSTALLING_FILE.toString() + "\n" + LRes.DONT_REBOOT_DEVICE.toString());
                WindowManager.setMainContent(progressPane, false);
                LocalDateTime startTime = LocalDateTime.now();
                AdbCommunication.getAllAccess();
                try {
                    adbRunner.runWait(3600);
                } catch (IOException e) {
                    throw new InstallException("Failed to start mtpinstall process: " + e.getMessage(), InstallException.Code.INTERNAL_ERROR, e);
                }
                AdbCommunication.giveAllAccess();
                WindowManager.removeTopContent();
                Duration timeElapsed = Duration.between(startTime, LocalDateTime.now());
                Log.info("MTP installation duration: " + timeElapsed.getSeconds() + " seconds");
                if (adbRunner.getExitValue() != 0) {
                    throw new InstallException("MtpInstall process returned with code " + adbRunner.getExitValue(), InstallException.Code.MTP_INSTALL_FAILED, adbRunner.getOutputString());
                }
                String output = adbRunner.getOutputString();
                output = output == null ? "" : output.toLowerCase();
                Log.info("MTP installation output: " + output);
                if (output.contains("installation_aborted")) {
                    throw new InstallException("MtpInstallation was aborted by the device: probably wrong token", InstallException.Code.MTP_INSTALL_FAILED);
                }
                long seconds = timeElapsed.getSeconds();
                if (seconds < 8) {
                    throw new InstallException("MtpInstallation took only " + seconds + " seconds to complete, thus it can't be successful", InstallException.Code.MTP_INSTALL_FAILED, adbRunner.getOutputString());
                }
                runner.text(LRes.ROM_INSTALLED_ON_DEVICE);
            }
        };
    }

    static RInstall formatData() {
        return RNode.sequence(new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, InterruptedException {
                Device device = (Device) runner.requireContext(Procedures.SELECTED_DEVICE);
                runner.text(LRes.PARTITION_FORMATTING.toString("data"));
                Log.info("Formatting data via stock recovery");
                String out = AdbCommons.raw(device.getSerial(), "format-data:");
                if (out == null) {
                    throw new InstallException("Failed to wipe data: null output", InstallException.Code.ADB_EXCEPTION, AdbCommons.getLastError(device.getSerial()));
                }
                Log.info("Format data successful");
                runner.text(LRes.PARTITION_FORMATTED);
            }
        }, GenericInstall.updateDeviceStatus(null, null, false));
    }

    @ExportFunction("mtp_stockrecovery_install")
    public static RInstall mtpFlashRom() {
        return RNode.sequence(new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                if (!ResourcesConst.isWindows()) {
                    throw new InstallException("This operation is not supported by this os", InstallException.Code.OS_NOT_SUPPORTED);
                }
            }
        }, RebootDevice.requireStockRecovery(), enableMtp(), sendFileViaMTP(), installMtpFile(), formatData());
    }

    public static RInstall getStockRecoveryInfo() {
        return new RInstall() {
            @Override
            public void run(ProcedureRunner procedureRunner) throws InstallException, InterruptedException {
                procedureRunner.text(LRes.FETCHING_RECOVERY_INFO);
                Device device = (Device) procedureRunner.requireContext(Procedures.SELECTED_DEVICE);
                ActionsDynamic.REQUIRE_DEVICE_CONNECTED(device).run();
                String serial = device.getSerial();
                String sn = AdbCommons.raw(serial, "getsn:");
                String version = AdbCommons.raw(serial, "getversion:");
                String codebase = AdbCommons.raw(serial, "getcodebase:");
                String dev = AdbCommons.raw(serial, "getdevice:");
                String zone = AdbCommons.raw(serial, "getromzone:");
                String branch = AdbCommons.raw(serial, "getbranch:");
                if (dev == null || version == null || sn == null || codebase == null || branch == null) {
                    throw new InstallException("Failed to retrieve recovery information: null param", InstallException.Code.INFO_RETRIVE_FAILED);
                }
                int z;
                if (zone == null) {
                    z = 0;
                } else {
                    try {
                        z = Integer.parseInt(zone);
                        if (z != 1 && z != 2) {
                            throw new Exception();
                        }
                    } catch (Throwable t) {
                        z = MiuiRom.Specie.getZone(dev);
                    }
                }
                DeviceRequestParams deviceRequestParams = new DeviceRequestParams(dev, version, codebase, Branch.fromCode(branch), SerialNumber.fromHexString(sn), z);
                procedureRunner.setContext(Procedures.REQUEST_PARAMS, deviceRequestParams);
            }
        };
    }

    @ExportFunction("side_stockrecovery_install")
    static RInstall sideloadFlash() {
        return RNode.sequence(RebootDevice.requireStockRecovery(), sidelaodFile(), formatData());
    }

    @ExportFunction("stockrecovery_install")
    public static RInstall stockRecoveryInstall() {
        if (ResourcesConst.isWindows()) {
            return RNode.fallback(sideloadFlash(), mtpFlashRom());
        } else {
            return sideloadFlash();
        }
    }
}
