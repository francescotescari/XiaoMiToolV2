package com.xiaomitool.v2.tasks;

import com.xiaomitool.v2.adb.AdbException;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.process.AdbRunner;
import com.xiaomitool.v2.utility.RunnableWithArg;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdbPushTask extends Task {
    private AdbRunner runner;
    private String device, destinationPath;
    private File sourceFile;
    public AdbPushTask(UpdateListener listener, String sourceFile, String destinationPath, String device){
        this(listener, new File(sourceFile), destinationPath, device);
    }
    public AdbPushTask(UpdateListener listener, File sourceFile, String destinationPath, String device) {
        super(listener);
        this.sourceFile = sourceFile;
        this.destinationPath = destinationPath;
        this.device = device;
    }

    @Override
    protected void startInternal() {
         runner = new AdbRunner();
        runner.setDeviceSerial(device);
        runner.addArgument("push");
        runner.addArgument(sourceFile.getAbsolutePath());
        final long fileSize = sourceFile.length();
        runner.addArgument(destinationPath);
        Pattern pattern = Pattern.compile("\\[(\\d+)/(\\d+)\\]");
        Pattern alternativePattern = Pattern.compile("\\[\\s*(\\d+)%\\]");

        runner.addSyncCallback(new RunnableWithArg() {
            @Override
            public void run(Object arg) {
                if (arg == null) {
                    update(-1);
                    return;
                }
                Matcher m = pattern.matcher(arg.toString());
                long totalSize = fileSize;
                if (!m.find()){
                    m = alternativePattern.matcher(arg.toString());
                    if (!m.find()) {
                        update(-1);
                        return;
                    }
                    totalSize = 100;
                }

                if (getTotalSize() <= 0 && totalSize > 0) {
                    setTotalSize(totalSize);
                }
                try {
                    Long done = Long.parseLong(m.group(1));
                    update(done);
                } catch (Throwable t){
                    update(-1);
                }
                Log.debug(arg.toString());
            }
        });
        try {
            runner.runWait(Integer.MAX_VALUE);
        } catch (IOException e) {
            error(e);
            return;
        }
        if (runner.getExitValue() != 0){
            error(new AdbException("Failed to push file to the device: exit code "+runner.getExitValue()));
            return;
        }
        finished(sourceFile);
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
        abort();
        if (runner != null){
            return runner.kill();
        }
        return true;
    }
}
