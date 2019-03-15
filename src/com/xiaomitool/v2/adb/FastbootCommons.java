package com.xiaomitool.v2.adb;

import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.process.FastbootRunner;
import com.xiaomitool.v2.process.ProcessRunner;
import com.xiaomitool.v2.resources.ResourcesManager;
import com.xiaomitool.v2.utility.NotNull;
import com.xiaomitool.v2.utility.YesNoMaybe;
import com.xiaomitool.v2.utility.utils.ProcessUtils;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FastbootCommons {
    private static final int DEFAULT_TIMEOUT = 6;
    public static String command_string(String cmd){
        return command_string(cmd,null);
    }
    public static String command_string(String cmd, String device)  {
        ProcessRunner runner = command_fast(cmd, device,  DEFAULT_TIMEOUT);
        if (runner == null){
            return "";
        }
        return runner.getOutputString();
    }
    public static List<String> command_list(String cmd){
        return command_list(cmd, null);
    }
    public static List<String> command_list(String cmd, String device) {
        ProcessRunner runner =  command_fast(cmd, device,DEFAULT_TIMEOUT);
        if (runner == null){
            return new ArrayList<>();
        }
        return runner.getOutputLines();
    }
    public static FastbootRunner command_fast(String cmd, String device, int timeout)  {
        //Log.debug("Input: fastboot "+cmd);
        FastbootRunner runner =  new FastbootRunner();
        if (device != null){
            runner.setDeviceSerial(device);
        }

        List<String> list = new ArrayList<String>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(cmd);
        while (m.find()) {
            list.add(m.group(1));
        }

        for (String arg : list){
            runner.addArgument(arg);
        }
        try {
            runner.runWait(timeout);
        } catch (IOException e) {
            Log.error("Cannot execute fastboot command \"fastboot "+cmd+"\", reason: "+e.getMessage());
            return null;
        }
        return runner;
    }
    public static List<String> devices()  {
        return command_list("devices");
    }
    public static List<String> getvars(String device){
        FastbootRunner runner = command_fast("getvar all",device,DEFAULT_TIMEOUT);
        if (runner == null){
            return null;
        }
        if (runner.getExitValue() != 0){
            return null;
        }
        return runner.getOutputLines();
    }
    public static String getvar(String var, String device){
        FastbootRunner runner = command_fast("getvar "+var,device,DEFAULT_TIMEOUT);
        if (runner == null){
            return null;
        }
        if (runner.getExitValue() != 0){
            return "";
        }
        return runner.getOutputString();
    }
    public static List<String> oemDeviceInfo(String device){
        return command_list("oem device-info",device);
    }
    public static List<String> oemLks(String device){
        return command_list("oem lks", device);
    }
    public static String rebootBootloader(String device){
        return command_string("reboot-bootloader",device);
    }
    public static String reboot(String device){
        return command_string("reboot",device);
    }
    public static String oemEdl(String devce){
        return command_string("oem edl",devce);
    }

    public static String flash(String device, File finalFile, String partition){
        String path;
        try {
            path = finalFile.getCanonicalPath();
        } catch (IOException e) {
            path = finalFile.getAbsolutePath();
        }
        FastbootRunner runner =  command_fast(device, 120, "flash", partition, path);
        if (runner == null){
            return null;
        }
        String output = runner.getOutputString();
        if (runner.getExitValue() != 0){
            Log.debug("Output of flash is failed");
            if (output != null) {
                if (output.toLowerCase().contains("anti-rollback")){
                    return "err:anti-rollback";
                }
            }
            return null;
        }
        return output;
    }

    public static YesNoMaybe oemUnlock(String device, String token){
        FastbootRunner runner =  command_fast(device, 12, "oem", "unlock", token);
        if(runner.getExitValue() != 0){
            return YesNoMaybe.NO;
        } else {
            String output = runner.getOutputString();
            if (output.contains("OKAY") && !output.contains("FAIL")){
                return YesNoMaybe.YES;
            }
            return YesNoMaybe.MAYBE;
        }
    }

    private static @NotNull FastbootRunner command_fast(String device, int timeout, String... args) {
        FastbootRunner runner = new FastbootRunner();
        runner.setDeviceSerial(device);
        for (String arg : args){
            runner.addArgument(arg);
        }
        try {
            runner.runWait(timeout);
        } catch (IOException e) {
            return null;
        }
        return runner;
        /**/
    }

    public static String boot(String serial, File finalFile) {
        String path;
        try {
            path = finalFile.getCanonicalPath();
        } catch (IOException e) {
            path = finalFile.getAbsolutePath();
        }
       FastbootRunner runner = command_fast(serial,120,"boot",path);
        return ProcessUtils.getOutput(runner);
    }

    public static String flashDummy(String serial)  {
        Path dummyPath = ResourcesManager.getTmpPath().resolve("dummy_image.img");
        if (!Files.exists(dummyPath)){
            try (FileOutputStream outputStream = new FileOutputStream(dummyPath.toFile())) {
                byte[] data = new byte[1024];
                for (int i = 0; i < 8; ++i) {
                    outputStream.write(data);
                }
            } catch (IOException e){
                return null;
            }
        }
        return flash(serial,dummyPath.toFile(), "antirbpass");
    }
    public static boolean oemRebootRecovery(String serial){
        FastbootRunner runner = FastbootCommons.command_fast(serial,5,"oem","reboot-recovery");
        if (runner == null || runner.getExitValue() != 0){
            return false;
        }
        String out = runner.getOutputString();
        if (out == null || out.toLowerCase().contains("fail")){
            return false;
        }
        return true;
    }
}
