package com.xiaomitool.v2.tasks;

import com.xiaomitool.v2.adb.AdbException;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.process.AdbRunner;
import com.xiaomitool.v2.utility.RunnableWithArg;
import com.xiaomitool.v2.utility.utils.NumberUtils;
import com.xiaomitool.v2.utility.utils.StrUtils;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdbSideloadTask extends Task {
    private static final Pattern PROGRESS_MATCH = Pattern.compile("\\[\\s*(\\d+)/\\s*(\\d+)\\]");
    private File fileToSideload;
    private String token;
    public AdbSideloadTask(File fileToSideload, String token){
        this.fileToSideload = fileToSideload;
        this.token = token;
    }

    @Override
    protected void startInternal() {
        AdbRunner runner = new AdbRunner();
        runner.addArgument("sideload");
        if (fileToSideload == null){
            this.error(new InstallException("null file to sideload", InstallException.Code.INTERNAL_ERROR, false));
        }
        runner.addArgument(fileToSideload.getAbsolutePath());

        if (!StrUtils.isNullOrEmpty(token)){
            runner.addArgument(token);
        }
        runner.addSyncCallback(new RunnableWithArg() {
            @Override
            public void run(Object arg) {
                String output = (String) arg;
                Matcher matcher = PROGRESS_MATCH.matcher(output);
                if (matcher.find()){
                    Long toDo = NumberUtils.parseLong(matcher.group(2));
                    Long done = NumberUtils.parseLong(matcher.group(1));
                    if (toDo == null || done == null || toDo < 0 || done < 0){
                        update(-1);
                        return;
                    }
                    if (getTotalSize() <= 0){
                        setTotalSize(toDo);
                    }
                    update(done);
                } else {
                    update(-1);
                }

            }
        });
        try {
            runner.runWait();
        } catch (IOException e) {
            this.error(e);
            return;
        }
        if (runner.getExitValue() != 0){
            error(new InstallException(new AdbException("adb sideload exited with status: "+runner.getExitValue())));
            return;
        }
        String out = runner.getOutputString();
        if (out == null){
            error(new AdbException("output of sideload is null"));
            return;
        }
        out = out.toLowerCase();
        if (out.contains("complete")){
            finished(fileToSideload);
            return;
        } else if (out.contains("abort")){
            error(new AdbException("output of sideload is abort, progress: "+getLatestUpdate()+"/"+getTotalSize()));
            return;
        }
        if(getLatestUpdate() >= 0 && getTotalSize() > 0){
            long percent = getLatestUpdate()/getTotalSize();
            if (percent < 90){
                error(new AdbException("sideload completed only for "+percent+"%, the installation might not completed succesfully"));
                return;
            }
        }
        finished(fileToSideload);

    }

    @Override
    protected boolean canPause() {
        return false;
    }

    @Override
    protected boolean canStop() {
        return false;
    }

    @Override
    protected boolean pauseInternal() {
        return false;
    }

    @Override
    protected boolean stopInternal() {
        return false;
    }
}
