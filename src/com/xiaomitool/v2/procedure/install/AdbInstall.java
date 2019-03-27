package com.xiaomitool.v2.procedure.install;

import com.xiaomitool.v2.adb.AdbException;
import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.gui.visual.ProgressPane;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.ProcedureRunner;
import com.xiaomitool.v2.procedure.Procedures;
import com.xiaomitool.v2.procedure.RInstall;
import com.xiaomitool.v2.procedure.RMessage;
import com.xiaomitool.v2.rom.Installable;
import com.xiaomitool.v2.tasks.AdbPushTask;
import com.xiaomitool.v2.tasks.TaskManager;
import com.xiaomitool.v2.tasks.UpdateListener;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.time.Duration;

public class AdbInstall {
    public static final String FILE_TO_PUSH = "ftopush";
    public static final String DESTINATION_PATH = "dpath";
    public static final String OUTPUT_DST_PATH = "outdpath";

    public static RInstall pushInstallableFile(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Installable installable = Procedures.requireInstallable(runner);
                runner.setContext(FILE_TO_PUSH, installable.getFinalFile());
                Log.debug(installable.getFinalFile());
                pushFile().run(runner);
            }
        };

    }

    public static RInstall pushFile(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                File fileToPush = (File) runner.requireContext(FILE_TO_PUSH);
                String destinationpath = (String) runner.getContext(DESTINATION_PATH);
                if (destinationpath == null){
                    destinationpath = "/sdcard/";
                }
                Log.info("Starting adb file push from "+fileToPush+" to "+destinationpath);
                String outputPath = destinationpath.endsWith("/") ? (destinationpath+FilenameUtils.getName(fileToPush.toString())) : destinationpath;
                Log.info("Predicted destination path: "+outputPath);
                Device device = Procedures.requireDevice(runner);
                ProgressPane.DefProgressPane defProgressPane = new ProgressPane.DefProgressPane();
                UpdateListener listener = defProgressPane.getUpdateListener(200);
                defProgressPane.setContentText(LRes.ADB_PUSHING_FILE);
                listener.addOnStart(new UpdateListener.OnStart() {
                    @Override
                    public void run(long totalSize) {
                        defProgressPane.setText(LRes.STARTING_TASK);
                    }
                });
                AdbPushTask pushTask = new AdbPushTask(listener,fileToPush, outputPath, device.getSerial());
                WindowManager.setMainContent(defProgressPane, false);
                TaskManager.getInstance().startSameThread(pushTask);
                pushTask.waitFinished();
                WindowManager.removeTopContent();
                if (pushTask.getError() != null){
                    throw new InstallException(new AdbException("Failed to push file on device: "+pushTask.getError().getMessage()));
                }
                Log.info("Adb push task success");
                runner.setContext(OUTPUT_DST_PATH, outputPath);

            }
        };

    }
}
