package com.xiaomitool.v2.tasks;

import com.xiaomitool.v2.adb.AdbException;
import com.xiaomitool.v2.process.AdbRunner;
import com.xiaomitool.v2.utility.RunnableWithArg;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TwrpInstallTask extends Task {
    private static final String[] TWRP_ERROR = new String[]{"Error installing zip file","Errore durante l'installazione dello Zip","å®‰è£zipåˆ·æ©ŸåŒ…|å‡ºéŒ¯","æœºåŒ…æ–‡ä»¶"};
    private RunnableWithArg outputRunable;


    protected AdbRunner adbRunner;
    public TwrpInstallTask(UpdateListener listener, String device, String remoteFilename, RunnableWithArg onOutputCatch){
        super(listener);
        this.adbRunner = new AdbRunner("shell", "-x","twrp install "+remoteFilename);
        this.adbRunner.setDeviceSerial(device);
        this.outputRunable = onOutputCatch;
    }
    @Override
    protected void startInternal() {
        if (this.outputRunable != null) {
            adbRunner.addSyncCallback(this.outputRunable);
        }
        try {
            adbRunner.runWait();
        } catch (IOException e) {
            error(e);
            return;
        }
        if (adbRunner.getExitValue() != 0){
            error(new AdbException("Return code of adb install is not zero"));
            return;
        }
        String output = adbRunner.getOutputString();
        if (output == null){
            error(new AdbException("Failed to get output of twrp install"));
            return;
        }
        output = output.toLowerCase();
        for (String errorStr : TWRP_ERROR){
            if (output.contains(errorStr.toLowerCase())){
                error(new AdbException("Failed to install zip: "+errorStr));
                return;
            }
        }
        finished(adbRunner.getOutputString());
    }

    @Override
    protected boolean canPause() {
        return false;
    }

    @Override
    protected boolean canStop() {
        return true;
    }

    @Override
    protected boolean pauseInternal() {
        return false;
    }

    @Override
    protected boolean stopInternal() {
        return adbRunner.kill();
    }
}
