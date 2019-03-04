package com.xiaomitool.v2.procedure.install;

import com.xiaomitool.v2.adb.AdbUtils;
import com.xiaomitool.v2.adb.FastbootCommons;
import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.adb.device.DeviceManager;
import com.xiaomitool.v2.adb.device.DeviceProperties;
import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.gui.controller.LoginController;
import com.xiaomitool.v2.gui.visual.ButtonPane;
import com.xiaomitool.v2.inet.CustomHttpException;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.*;
import com.xiaomitool.v2.procedure.device.RebootDevice;
import com.xiaomitool.v2.process.ProcessRunner;
import com.xiaomitool.v2.rom.Installable;
import com.xiaomitool.v2.utility.Pointer;
import com.xiaomitool.v2.utility.RunnableWithArg;
import com.xiaomitool.v2.utility.YesNoMaybe;
import com.xiaomitool.v2.utility.utils.FileUtils;
import com.xiaomitool.v2.utility.utils.ThreadUtils;
import com.xiaomitool.v2.xiaomi.XiaomiKeystore;
import com.xiaomitool.v2.xiaomi.XiaomiProcedureException;
import com.xiaomitool.v2.xiaomi.miuithings.UnlockStatus;
import com.xiaomitool.v2.xiaomi.unlock.UnlockCommonRequests;
import javafx.application.Platform;
import org.apache.commons.lang3.SystemUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.xiaomitool.v2.procedure.install.InstallException.Code.*;

public class FastbootInstall {
   /* public void flash_tgz(Installable rom, Device device, InstallListener listener) throws InstallException, InterruptedException {
        ProcedureRunner runner = new ProcedureRunner(listener.getGuiListener());
        runner.run(Procedures.requireAccessibile());
        runner.run(Procedures.fetchResources(rom, listener));
        runner.run(Procedures.reboot(device, Device.Status.FASTBOOT));
        runner.run(Procedures.waitResources(rom));
        Pointer pointer = new Pointer();
        runner.run(findBuildFlashFile(rom.getFinalFile(),pointer));
        runner.run(runFlashAllFile(device, (File) pointer.pointed));
    }*/

   private static final String FLASH_ALL_PATH = "fast_flash_all_file";
    private static final String FLASH_SCRIPT_FILE = "fast_flash_script_file";



    public static RInstall findFlashAllFile(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner procedureRunner) throws InstallException, InterruptedException {
                Installable installable = Procedures.requireInstallable(procedureRunner);
                File outputDirectory = installable.getFinalFile();
                try {
                    String file = SystemUtils.IS_OS_WINDOWS ? "flash_all.bat" : "flash_all.sh";
                    Stream<Path> result = Files.find(outputDirectory.toPath(), 4, new BiPredicate<Path, BasicFileAttributes>() {
                        @Override
                        public boolean test(Path path, BasicFileAttributes basicFileAttributes) {
                            return path != null && path.toString().toLowerCase().endsWith(file);
                        }
                    });
                    List<Path> res = result.collect(Collectors.toList());
                    if (res.size() == 0){
                        throw new InstallException("File not found: "+file, FILE_NOT_FOUND, true);
                    }
                    res.sort(Comparator.comparingInt(o -> o.toString().length()));
                    Path flashAllPath = res.get(0);
                    procedureRunner.setContext(FLASH_ALL_PATH, flashAllPath);

                } catch (IOException e) {
                    throw new InstallException("IOException while finding flash_all file: "+e.getMessage(),FILE_NOT_FOUND, true);
                }
            }
        };
    }
    public static RInstall buildFlashFile(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner procedureRunner) throws InstallException, InterruptedException {
                Path flashAllFile = (Path) procedureRunner.requireContext(FLASH_ALL_PATH);
                if (flashAllFile == null || !Files.exists(flashAllFile)){
                    throw new InstallException("Failed to obtain flash_all file",FILE_NOT_FOUND, false);
                }
                String content;
                File flash_all = flashAllFile.toFile();
                try {
                    content= FileUtils.readAll(flash_all);
                } catch (IOException e) {
                    throw new InstallException("Failed to read flash_all file",InstallException.Code.IO_ERROR, true);
                }
                String[] contentLines = content.split("\\n");
                String outFile = SystemUtils.IS_OS_WINDOWS ? "flash_xiaomitool.bat" : "flash_xiaomitool.sh";
                int lines = contentLines.length;
                StringBuilder builder = new StringBuilder(SystemUtils.IS_OS_WINDOWS ? "@echo off"+System.lineSeparator() : "");

                Log.debug("Total lines: "+lines);
                Pattern p = Pattern.compile("fastboot.+flash\\s+(\\w+)",Pattern.CASE_INSENSITIVE);
                for (String line : contentLines){
                    if(line.trim().toLowerCase().startsWith("pause")){
                        continue;
                    }
                    Matcher m = p.matcher(line);

                    if (m.find()){
                        builder.append("echo [Flashing ").append(m.group(1)).append("]").append(System.lineSeparator());
                    }
                    builder.append(line).append('\n');
                }
                File flash_xiaomitool = new File(flash_all.getParentFile(),outFile);
                try {
                    FileUtils.writeAll(flash_xiaomitool,builder.toString());
                } catch (IOException e) {
                    throw new InstallException("Failed to write to flash_xiaomitool file: "+e.getMessage(),IO_ERROR, true);
                }
                procedureRunner.setContext(FLASH_SCRIPT_FILE, flash_xiaomitool);
            }
        };


    }

    public static RInstall findBuildFlashFile(){
        return RNode.sequence(findFlashAllFile(), buildFlashFile());
    }

    @ExportFunction("install_fastboot_rom")
    public static RInstall findBuildRunFlashAll(){
        return RNode.sequence(RebootDevice.requireFastboot(), findBuildFlashFile(),runFlashScriptFile(), GenericInstall.updateDeviceStatus(null, false, false));
    }
    public static RInstall runFlashScriptFile(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner procedureRunner) throws InstallException, InterruptedException, RMessage {
                File flashAllFile = (File) procedureRunner.requireContext(FLASH_SCRIPT_FILE);
                Device device = Procedures.requireDevice(procedureRunner);
                if (flashAllFile == null){
                    throw new InstallException("Null flash_all file",FILE_NOT_FOUND, false);
                }
                flashAllFile.setExecutable(true);
                ProcessRunner runner = new ProcessRunner(flashAllFile.toPath());
                runner.addArgument("-s");
                runner.addArgument(device.getSerial());
                //Procedures.requireAccessibile().run(procedureRunner);
                Pattern p = Pattern.compile("\\[(Flashing \\w+)\\]",Pattern.CASE_INSENSITIVE);
                Pointer lastLine = new Pointer();
                lastLine.pointed = "";
                runner.addSyncCallback(new RunnableWithArg() {
                    @Override
                    public void run(Object arg) {
                        String line = (String) arg;
                        Matcher m = p.matcher(line);
                        if (m.find()){
                            procedureRunner.text(m.group(1));
                        }
                        lastLine.pointed = line;
                    }
                });
                device.requireAccess();
                try {
                    runner.runWait(1800);
                } catch (IOException e) {
                    device.releaseAccess();
                    throw new InstallException("Failed to run flash_all file: "+e.getMessage(),IO_ERROR, true);
                }
                device.releaseAccess();
                int exitCode = runner.getExitValue();
                if (exitCode != 0){
                    throw new InstallException("Fastboot flash all failed: "+lastLine.pointed, FASTBOOT_FLASH_FAILED, true);
                }
            }
        };

    }

    @ExportFunction("unlock_bootlaoder")
    public static RInstall unlockBootloader(){
        return RNode.sequence(RebootDevice.requireFastboot(), new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                XiaomiKeystore keystore = XiaomiKeystore.getInstance();
                if (!keystore.isLogged()){
                    LoginController.loginRunnable().run();
                    if (!keystore.isLogged()){
                        throw new InstallException("Login is required for this action. Please login with your Xiaomi account", InstallException.Code.INFO_RETRIVE_FAILED, true);
                    }
                }
                String token = FastbootCommons.getvar("token", device.getSerial());
                if (token == null){
                    throw new InstallException("Failed to get the device unlock token", InstallException.Code.INFO_RETRIVE_FAILED, true);
                }
                try {
                    String info = UnlockCommonRequests.userInfo();
                    if (info != null){
                        //TODO
                        Log.debug(info);
                    }
                    String alert = UnlockCommonRequests.deviceClear((String) device.getDeviceProperties().get(DeviceProperties.CODENAME));
                    if (alert != null){
                        //TODO
                        Log.debug(alert);
                    }
                } catch (XiaomiProcedureException e) {
                    throw new InstallException(e);
                } catch (CustomHttpException e) {
                    throw new InstallException(e);
                }
                ButtonPane buttonPane = new ButtonPane(LRes.CONTINUE, LRes.CANCEL);
                buttonPane.setContentText(LRes.UNLOCK_BOOTLOADER_WARN);
                WindowManager.setMainContent(buttonPane,false);
                int click = buttonPane.waitClick();
                WindowManager.removeTopContent();
                if (click != 0){
                    throw new InstallException("Unlock procedure aborted, cannot continue", InstallException.Code.ABORTED, true);
                }
                token = AdbUtils.parseFastbootVar("token",FastbootCommons.getvar("token", device.getSerial()));
                if (token == null){
                    throw new InstallException("Failed to get the device unlock token", InstallException.Code.INFO_RETRIVE_FAILED, true);
                }
                try {
                    String unlockData = null;
                    if (false) { //TODO REMOVE FAKE UNLOCK
                        unlockData = UnlockCommonRequests.ahaUnlock(token, (String) device.getDeviceProperties().get(DeviceProperties.CODENAME), (String) device.getDeviceProperties().getFastbootProperties().get("", ""), (String) device.getDeviceProperties().getFastbootProperties().get("", ""), "");
                    } else {
                        unlockData = "{\"code\":0,\"encryptData\":\"86B700178A7F1BF7886D7A3FA05EBDEECA3095AB4D091C2750A313F1310EF2E898DB92ED3CA7E1FD09E17A92D93BEF63C15653ECB848E672B9C892C4A172F117D671F64A49846A768E95FEAD6643F09D723AC90847D738B8AC12BDB29C8DE2B3AB9A855729A655BDF8A5A0E69B8AC102E78C673D836A808D400157F7FF305BE567AAB5D640FCFBA3229B63F77D81DDABCF2CC01B57E7400D0081547B99F92E2F470CB5B7DF2F43318F0F9205A315FFD19981FC9662371BA4C157A528B448B1BAF5D1D950E349504240DB4BA4AE57C57B85B24834F6130651D555437C24BE0EFFEB7E1277925AFB1D631B66012BBA78CE6161F7C6AF50021BF0849BF15AFD6F42\",\"description\":\"私钥签名成功\",\"uid\":\"1606054557\"}";
                    }
                    if (unlockData == null){
                        throw new InstallException("Failed to get the unlock data required", InstallException.Code.INFO_RETRIVE_FAILED, true);
                    }
                    Log.debug(unlockData);
                    JSONObject json = new JSONObject(unlockData);
                    int code = json.optInt("code",-100);
                    String description = json.optString("description", "null");
                    String encryptData = json.optString("encryptData",null);

                    if (code != 0 || encryptData == null){
                        throw new InstallException("The server responded, but the unlock is not permitted, code: "+code+", description: "+description, InstallException.Code.XIAOMI_EXCEPTION, true);
                    }
                    YesNoMaybe unlocked = FastbootCommons.oemUnlock(device.getSerial(), encryptData);
                    if (YesNoMaybe.NO.equals(unlocked)){
                        throw new InstallException("Failed to unlock the device, fastboot exit with status non zero or internal error", InstallException.Code.UNLOCK_ERROR, true);
                    }
                    device.getDeviceProperties().getFastbootProperties().put(DeviceProperties.X_LOCKSTATUS, UnlockStatus.UNKNOWN);
                    Thread.sleep(1000);
                    DeviceManager.refresh();
                    device.waitStatus(Device.Status.FASTBOOT, 4);
                    Device.Status status = device.getStatus();
                    if (Device.Status.FASTBOOT.equals(status)){
                        device.getDeviceProperties().getFastbootProperties().parse(true);
                    }
                    if (UnlockStatus.LOCKED.equals(device.getAnswers().getUnlockStatus())){
                        throw new InstallException("Failed to unlock the device, the procedure failed during the unlock command, the device doens't seem to be unlocked", UNLOCK_ERROR, true);
                    }

                } catch (XiaomiProcedureException e) {
                    throw new InstallException(e);
                } catch (CustomHttpException e) {
                    throw new InstallException(e);
                } catch (InstallException e){
                   throw e;
                } catch (Exception e){
                    throw new InstallException("Internal error while parsing unlock data: "+e.getMessage(), InstallException.Code.INTERNAL_ERROR, true);
                }

            }
        }, GenericInstall.updateDeviceStatus(true, null, false));
    }
}
