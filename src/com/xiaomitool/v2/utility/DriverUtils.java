package com.xiaomitool.v2.utility;

import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.process.ProcessRunner;
import com.xiaomitool.v2.resources.ResourcesManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DriverUtils {
    private static final String SUCCESS = "result_success";
    private static final String FAIL = "result_fail";

    private static ProcessRunner getDriverRunner(){
        return new ProcessRunner(ResourcesManager.getToolPath("driver",true));
    }

    public static boolean installDriver(String infPath){
        return installDriver(Paths.get(infPath));
    }
    public static boolean installDriver(Path infPath){
        if (!Files.exists(infPath)){
            Log.error("Failed to install driver: path not found");
            return false;
        }
        return runCommand("install",60,"install",infPath.toString());
    }

    private static boolean runCommand(String cmdName, int timeout,  String... args){
        ProcessRunner runner = getDriverRunner();
        for (String arg : args){
            runner.addArgument(arg);
        }
        try {
            runner.runWait(timeout);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int returnCode = runner.getExitValue();
        if (returnCode != 0){
            Log.error("Driver tool "+cmdName+" returned with code: "+returnCode);
            Log.info(runner.getOutputString());
            return false;
        }
        String output = runner.getOutputString();
        if (output.contains(SUCCESS)){
            Log.debug("Driver "+cmdName+" success");
            return true;
        } else if (output.contains(FAIL)){
            Log.error("Driver "+cmdName+" failed. Tool fail message match");
            Log.info(output);
            return false;
        } else {
            Log.warn("Unknown driver "+cmdName+" outcome");
            Log.info(output);
            return true;
        }
    }

    public static boolean refresh(){

        return runCommand("refresh",10,"refresh");
    }

    public static boolean fixMtpDevices(){
        refresh();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return runCommand("fixmtp",60,"fixmtp");

    }


}
