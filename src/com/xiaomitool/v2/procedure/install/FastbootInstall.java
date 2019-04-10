package com.xiaomitool.v2.procedure.install;

import com.xiaomitool.v2.adb.AdbException;
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
import com.xiaomitool.v2.process.ShellRunner;
import com.xiaomitool.v2.resources.ResourcesConst;
import com.xiaomitool.v2.resources.ResourcesManager;
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
import java.util.HashMap;
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
                    Log.info("Searching file "+file+" in the extracted directory");
                    Stream<Path> result = Files.find(outputDirectory.toPath(), 4, new BiPredicate<Path, BasicFileAttributes>() {
                        @Override
                        public boolean test(Path path, BasicFileAttributes basicFileAttributes) {
                            return path != null && path.toString().toLowerCase().endsWith(file);
                        }
                    });
                    List<Path> res = result.collect(Collectors.toList());
                    Log.info("Possible flash_all files found: "+res);
                    if (res.size() == 0){
                        throw new InstallException("Flash all file not found in extracted dir: "+file, FILE_NOT_FOUND, true);
                    }
                    res.sort(Comparator.comparingInt(o -> o.toString().length()));
                    Path flashAllPath = res.get(0);
                    Log.info("Choosen flash_all file: "+flashAllPath);
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
                Log.info("Building custom flash_all file");
                File flash_all = flashAllFile.toFile();
                try {
                    content= FileUtils.readAll(flash_all);
                } catch (IOException e) {
                    throw new InstallException("Failed to read flash_all file",InstallException.Code.IO_ERROR, true);
                }
                Log.info("Original flash_all file: ");
                Log.info(content);
                String[] contentLines = content.split("\\n");
                String outFile = ResourcesConst.isWindows() ? "flash_xiaomitool.bat" : "flash_xiaomitool.sh";
                int lines = contentLines.length;
                StringBuilder builder = new StringBuilder();
                if (ResourcesConst.isWindows()){
                    builder.append("@echo off").append(System.lineSeparator());
                    builder.append("echo Current dir: %~dp0").append(System.lineSeparator());
                }
                Log.debug("Total lines: "+lines);
                Pattern p = Pattern.compile("fastboot.+flash\\s+(\\w+)",Pattern.CASE_INSENSITIVE);
                builder.append("echo Fastboot flash starting").append(System.lineSeparator());
                for (String line : contentLines){
                    if(line.trim().toLowerCase().startsWith("pause")){
                        continue;
                    }
                    Matcher m = p.matcher(line);

                    if (m.find()){
                        builder.append("echo [Flashing ").append(m.group(1)).append("]").append(System.lineSeparator());
                    }
                    builder.append(line).append(System.lineSeparator());
                }
                String outputContent = builder.toString();
                File flash_xiaomitool = new File(flash_all.getParentFile(),outFile);
                Log.info("Custom flash_all file: "+flash_xiaomitool);
                Log.info("Custom flash_all file content: ");
                Log.info(outputContent);
                try {
                    FileUtils.writeAll(flash_xiaomitool,outputContent);
                } catch (IOException e) {
                    throw new InstallException("Failed to write to flash_xiaomitool file: "+e.getMessage(),IO_ERROR, true);
                }
                Log.info("Flash_all file generated success");
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
                Path flash_all_dir = flashAllFile.getParentFile().toPath();
                Log.info("Starting fastboot flash_all script: "+flashAllFile);
                try {
                    Log.info("Copying fastboot resources to flash_all file directory: "+flash_all_dir);
                    ResourcesManager.copyResourcesToDir(ResourcesManager.getFastbootFilesPath(), flash_all_dir);
                } catch (Exception e){
                    throw new InstallException("Failed to copy fastboot to flash_all dir: "+e.getMessage(), FASTBOOT_FLASH_FAILED, true);
                }
                Device device = Procedures.requireDevice(procedureRunner);
                if (flashAllFile == null){
                    throw new InstallException("Null flash_all file",FILE_NOT_FOUND, false);
                }
                flashAllFile.setExecutable(true);
                String flashAllFileShell = flashAllFile.toPath().getFileName().toString();
                if (!ResourcesConst.isWindows() && !flashAllFileShell.startsWith("/") && !flashAllFileShell.startsWith(".")){
                    flashAllFileShell = "./"+flashAllFileShell;
                }
                Log.info("Flash_all argument passed to shell: "+flashAllFileShell);
                ProcessRunner runner = new ShellRunner(flashAllFileShell);
                runner.setWorkingDir(flash_all_dir.toFile());
                runner.addArgument("-s");
                runner.addArgument(device.getSerial());
                //Procedures.requireAccessibile().run(procedureRunner);
                Pattern p = Pattern.compile("\\[(Flashing \\w+)\\]",Pattern.CASE_INSENSITIVE);
                Pointer lastLine = new Pointer();
                lastLine.pointed = "";
                ;
                runner.addSyncCallback(new RunnableWithArg() {
                    @Override
                    public void run(Object arg) {
                        String line = (String) arg;
                        Log.info(arg);
                        Matcher m = p.matcher(line);
                        if (m.find()){
                            String text = m.group(1);
                            procedureRunner.text(text);
                            if (text.toLowerCase().endsWith("system")){
                                procedureRunner.text(LRes.CAN_TAKE_COUPLE_MIN);
                            }

                        }
                        lastLine.pointed = line;
                    }
                });
                device.requireAccess();
                Log.info("Starting flash_all fastboot process");
                try {
                    runner.runWait(3000);
                } catch (IOException e) {
                    device.releaseAccess();
                    throw new InstallException("Failed to run flash_all file: "+e.getMessage(),IO_ERROR, true);
                }
                device.releaseAccess();
                int exitCode = runner.getExitValue();
                if (exitCode != 0){
                    throw new InstallException("Fastboot flash all failed, exit code: "+exitCode+", output: "+lastLine.pointed, FASTBOOT_FLASH_FAILED, true);
                }
                Log.info("Flash_all script run success");
            }
        };

    }

    private static final HashMap<String, String> UNLOCK_TOKEN_CACHE = new HashMap<>();

    @ExportFunction("unlock_bootlaoder")
    public static RInstall unlockBootloader(){
        return RNode.sequence(RebootDevice.requireFastboot(), new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Log.info("Starting unlock procedure");
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
                Log.info("First trial unlock token: "+token);
                String product = (String) device.getDeviceProperties().getFastbootProperties().get(DeviceProperties.FASTBOOT_PRODUCT);
                if (product == null){
                    product = FastbootCommons.getvar("product", device.getSerial());
                }
                try {
                    runner.text(LRes.UNLOCK_CHECKING_ACCOUNT);
                    String info = UnlockCommonRequests.userInfo();
                    if (info != null){
                        //TODO
                        Log.debug(info);
                        Log.info("Unlock request user info: "+info);
                    }
                    runner.text(LRes.UNLOCK_CHECKING_DEVICE);
                    String alert = UnlockCommonRequests.deviceClear(product);
                    if (alert != null){
                        //TODO
                        Log.debug(alert);
                        Log.info("Unlock request device clear: "+alert);
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
                    throw InstallException.ABORT_EXCEPTION;
                }
                Log.info("Unlock request confirmation success");
                while (true) {
                    token = FastbootCommons.getvar("token", device.getSerial());
                    if (token == null) {
                        throw new InstallException("Failed to get the device unlock token", InstallException.Code.INFO_RETRIVE_FAILED, true);
                    }
                    Log.info("Unlock request token: "+token);
                    try {
                        String unlockData = null;
                        runner.text(LRes.UNLOCK_REQUESTING_TOKEN);
                        unlockData = UnlockCommonRequests.ahaUnlock(token, product, "", "", "");
                        if (unlockData == null) {
                            throw new InstallException("Failed to get the unlock data required", InstallException.Code.INFO_RETRIVE_FAILED, true);
                        }
                        Log.info("Unlock request response: "+unlockData);
                        Log.debug(unlockData);
                        JSONObject json = new JSONObject(unlockData);
                        int code = json.optInt("code", -100);
                        String description = json.optString("description", "null");
                        String encryptData = json.optString("encryptData", null);
                        Log.debug(description);
                        if (code != 0 || encryptData == null) {
                           // throw new InstallException("The server responded, but the unlock is not permitted, code: " + code + ", description: " + description, InstallException.Code.XIAOMI_EXCEPTION, true);
                            ButtonPane unlockButtonPane = new ButtonPane(LRes.TRY_AGAIN, LRes.ABORT);
                            unlockButtonPane.setContentText(LRes.UNLOCK_ERROR_TEXT.toString(code, UnlockCommonRequests.getUnlockCodeMeaning(code,json)));
                            WindowManager.setMainContent(unlockButtonPane,false);
                            int choice = unlockButtonPane.waitClick();
                            WindowManager.removeTopContent();
                            if(choice == 0){
                                continue;
                            } else {
                                throw InstallException.ABORT_EXCEPTION;
                            }
                        } else {
                            UNLOCK_TOKEN_CACHE.put(token, encryptData);
                        }
                        runner.text(LRes.UNLOCK_UNLOCKING_DEVICE);
                        YesNoMaybe unlocked = FastbootCommons.oemUnlock(device.getSerial(), encryptData);
                        if (YesNoMaybe.NO.equals(unlocked)) {
                            throw new InstallException("Failed to unlock the device, fastboot exit with status non zero or internal error", InstallException.Code.UNLOCK_ERROR, true);
                        }
                        device.getDeviceProperties().getFastbootProperties().put(DeviceProperties.X_LOCKSTATUS, UnlockStatus.UNKNOWN);
                        Thread.sleep(1000);
                        DeviceManager.refresh();
                        try {
                            device.waitStatus(Device.Status.FASTBOOT, 5);
                            Device.Status status = device.getStatus();
                            if (Device.Status.FASTBOOT.equals(status)) {
                                Log.info("Device is back in fastboot mode: parsing properties");
                                device.getDeviceProperties().getFastbootProperties().parse(true);
                                Log.info("UnlockStatus: "+device.getAnswers().getUnlockStatus());
                            }
                            if (UnlockStatus.LOCKED.equals(device.getAnswers().getUnlockStatus())) {
                                throw new InstallException("Failed to unlock the device, the procedure failed during the unlock command, the device doens't seem to be unlocked", UNLOCK_ERROR, true);
                            }
                            Log.info("This is the strangest way to suppose that the device is unlocked :/");
                        } catch (AdbException e){
                            //Device not in fastboot after 5 seconds = unlock success;
                            Log.debug("Successful unlock!");
                            Log.info("The device is not in fastboot after 5 seconds -> should be unlocked");
                        }
                        break;

                    } catch (XiaomiProcedureException e) {
                        throw new InstallException(e);
                    } catch (CustomHttpException e) {
                        throw new InstallException(e);
                    } catch (InstallException e) {
                        throw e;
                    } catch (Exception e){
                        throw new InstallException("Internal error while parsing unlock data: "+e.getMessage(), InstallException.Code.INTERNAL_ERROR, true);
                    }
                }

            }
        }, GenericInstall.updateDeviceStatus(true, null, false));
    }
}
