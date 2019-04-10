package com.xiaomitool.v2.procedure.install;

import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.adb.device.DeviceAnswers;
import com.xiaomitool.v2.adb.device.DeviceManager;
import com.xiaomitool.v2.adb.device.DeviceProperties;
import com.xiaomitool.v2.engine.ToolManager;
import com.xiaomitool.v2.engine.actions.ActionsDynamic;
import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.gui.visual.ButtonPane;
import com.xiaomitool.v2.gui.visual.DonationPane;
import com.xiaomitool.v2.gui.visual.ProgressPane;
import com.xiaomitool.v2.gui.visual.SmilePane;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.logging.feedback.LiveFeedbackEasy;
import com.xiaomitool.v2.procedure.*;
import com.xiaomitool.v2.procedure.device.ManageDevice;
import com.xiaomitool.v2.procedure.device.RebootDevice;
import com.xiaomitool.v2.procedure.uistuff.ChooseProcedure;
import com.xiaomitool.v2.procedure.uistuff.ConfirmationProcedure;
import com.xiaomitool.v2.rom.Installable;
import com.xiaomitool.v2.rom.chooser.InstallationRequirement;
import com.xiaomitool.v2.rom.interfaces.StatedProcedure;
import com.xiaomitool.v2.tasks.UpdateListener;
import com.xiaomitool.v2.utility.CommandClass;
import com.xiaomitool.v2.utility.YesNoMaybe;
import com.xiaomitool.v2.utility.utils.InetUtils;
import com.xiaomitool.v2.utility.utils.StrUtils;
import com.xiaomitool.v2.xiaomi.miuithings.UnlockStatus;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.apache.commons.io.FilenameUtils;

import java.util.HashMap;
import java.util.Objects;

import static com.xiaomitool.v2.engine.CommonsMessages.NOOP;

public class GenericInstall {

    public static RInstall resourceDownload(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Log.debug("Downloading resource");
                Installable installable = Procedures.requireInstallable(runner);
                Log.info("Starting required resource download");
                if(!installable.isNeedDownload()){
                    Log.info("No need to download resources, skip");
                    Log.debug("No need to download");
                    return;
                }
                if (StrUtils.isNullOrEmpty(installable.getDownloadUrl())){
                    throw new InstallException("Download failed: empty or null download url", InstallException.Code.DOWNLOAD_FAILED, false);
                }
                Log.info("Starting download from: "+installable.getDownloadUrl());
                ProgressPane.DefProgressPane defProgressPane = new ProgressPane.DefProgressPane();
                defProgressPane.setContentText(LRes.DOWNLOADING_ROM_FILE.toString()+"\n"+FilenameUtils.getName(installable.getDownloadUrl()));
                UpdateListener listener = defProgressPane.getUpdateListener(1000);
                WindowManager.setMainContent(defProgressPane,false);
                try {
                    installable.download(listener);
                    Log.info("Download was success");
                }catch (Exception e){
                    throw new InstallException("Download task failed: "+e.getMessage(), InstallException.Code.DOWNLOAD_FAILED, true);
                } finally {
                    WindowManager.removeTopContent();
                }

            }
        };
    }
    public static RInstall resourceExtract(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Log.debug("Extracting resource");
                Installable installable = Procedures.requireInstallable(runner);
                Log.info("Starting extraction of resources");
                if(!installable.isNeedExtraction()){
                    Log.debug("No need to extract");
                    Log.info("There is no need to extract, skip");
                    return;
                }
                if (installable.getDownloadedFile() == null){
                    throw new InstallException("Extract failed: null downloaded file", InstallException.Code.EXTRACTION_FAILED, false);
                }
                Log.info("Extracting file: "+installable.getDownloadedFile());
                ProgressPane.DefProgressPane defProgressPane = new ProgressPane.DefProgressPane();
                defProgressPane.setContentText(LRes.EXTRACTING_ROM_FILE+"\n"+installable.getDownloadedFile().toString());
                UpdateListener listener = defProgressPane.getUpdateListener(333);
                WindowManager.setMainContent(defProgressPane,false);
                try {
                    installable.extract(listener);
                    Log.info("Extraction was success");
                }catch (Exception e){
                    throw new InstallException("Extraction task failed: "+e.getMessage(), InstallException.Code.EXTRACTION_FAILED, true);
                } finally {
                    WindowManager.removeTopContent();
                }

            }
        };
    }

    public static RInstall resourceFetchWait(){
        return RNode.sequence(resourceDownload(),resourceExtract());
    }

    public static RInstall runInstallProcedure(){
        return RNode.sequence(new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Installable installable = (Installable) runner.getContext(Procedures.INSTALLABLE);
                Boolean isProcedure = (Boolean) runner.getContext(ChooseProcedure.IS_CHOOSEN_PROCEDURE);
                if (installable == null && isProcedure != null && isProcedure == true){
                    Log.warn("There is no installable, a procedure was selected, this probably means that a procedure has already finished, skip the installation part");
                    return;
                }
                installable = Procedures.requireInstallable(runner);
                Procedures.pushRInstallOnStack(runner,installable.getInstallProcedure());
                Log.info("Installation procedure to run: "+installable.getInstallProcedure().toString(1));
            }
        }, Procedures.runStackedProcedures());
    }

    public static RInstall installationSuccess() {
        return RNode.sequence(RebootDevice.rebootNoWaitIfConnected(), new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, InterruptedException {
                WindowManager.setOnExitAskForFeedback(false);
                Installable installable = (Installable) runner.getContext(Procedures.INSTALLABLE);
                LiveFeedbackEasy.sendSuccess(String.valueOf(installable),runner.getStackStrace());
                Log.info("Installation succesful, showing donation message");
                DeviceManager.stopScanThreads();
                DonationPane donationPane = new DonationPane();
                WindowManager.setMainContent(donationPane);
                int msg = NOOP;
                while (msg == NOOP) {
                    msg = donationPane.waitClick();
                    if (msg == 0) {
                        ToolManager.exit(0);
                        return;
                    }
                }
                InetUtils.openUrlInBrowser(ToolManager.URL_DONATION);
                ButtonPane buttonPane = new ButtonPane(LRes.EXIT_TOOL);
                Text text = new Text(LRes.DONATE_THANKS_TEXT.toString());
                text.setFont(Font.font(16));
                text.setFill(Color.rgb(0, 51, 0));
                text.setTextAlignment(TextAlignment.CENTER);
                text.setWrappingWidth(WindowManager.getContentWidth() - 150);
                SmilePane smilePane = new SmilePane(200);
                smilePane.setHappiness(100);
                VBox vBox = new VBox(smilePane, text);
                vBox.setAlignment(Pos.CENTER);
                vBox.setSpacing(40);
                buttonPane.setContent(vBox);
                WindowManager.setMainContent(buttonPane);
                buttonPane.waitClick();
                ToolManager.exit(0);
            }
        });
    }
    private static final String KEY_STASHED_INSTALLABLE = "stashed_installable";
    public static RInstall satisfyAllRequirements(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Log.info("Starting satisfy requirements procedure");
                Installable stashed = (Installable) runner.getContext(KEY_STASHED_INSTALLABLE);
                Installable installable = Procedures.requireInstallable(runner);
                Device device = Procedures.requireDevice(runner);
                if (stashed == null){
                    Log.info("Stashing the installable to satisfy the requirements: "+installable.toLogString());
                    runner.setContext(KEY_STASHED_INSTALLABLE, installable);
                }
                Log.debug("Satisfying all requirements");
                StatedProcedure toSatisfy = InstallationRequirement.satisfyNextRequirement(Procedures.requireDevice(runner),installable);
                StatedProcedure copy = null;
                while (toSatisfy != null){
                    if (toSatisfy.getInstallProcedure() != null) {
                        if (YesNoMaybe.YES.equals(device.getAnswers().isNeedDeviceDebug()) && Procedures.stillNeedUsbDebug(runner, toSatisfy)) {
                            Log.warn("Satisfying the requirement resetted the phone, you need to enable usb debug again");
                            RebootDevice.rebootNoWaitIfConnected().run(runner);
                            ActionsDynamic.WAIT_USB_DEBUG_ENABLE(device).run();
                        }
                        Log.debug("Statisfying requrement: " + toSatisfy.toString());
                        Log.info("Next procedure to satisfy: " + toSatisfy.getInstallProcedure().toString(2));
                        toSatisfy.getInstallProcedure().run(runner);
                        copy = toSatisfy;
                    }
                    toSatisfy = InstallationRequirement.satisfyNextRequirement(Procedures.requireDevice(runner),installable);
                    if (Objects.equals(copy, toSatisfy)){
                        throw new InstallException("Trying to satisfy a requirement that should had been just satisfied: "+copy, InstallException.Code.INTERNAL_ERROR, false);
                    }
                    try {
                        Log.info("The device might be rebooting right now, lets wait it for 30 seconds");
                        ManageDevice.waitDevice(30, Device.Status.DEVICE).setFlag(RNode.FLAG_THROWRAWEXCEPTION, true).run(runner);
                    } catch (InstallException e){
                        Log.warn("Starting next requirement satisfaction without device active");
                    }
                }
                stashed = (Installable) runner.getContext(KEY_STASHED_INSTALLABLE);


                if (stashed != null){
                    if (YesNoMaybe.YES.equals(device.getAnswers().isNeedDeviceDebug()) && Procedures.stillNeedUsbDebug(runner, stashed)) {
                        Log.warn("Satisfying the requirement resetted the phone, you need to enable usb debug again before installing stashed installable");
                        RebootDevice.rebootNoWaitIfConnected().run(runner);
                        ActionsDynamic.WAIT_USB_DEBUG_ENABLE(device).run();
                    }

                    Log.info("Reloading the stashed installable: "+stashed.toLogString());
                    Log.debug("Reloading stashed installable: "+stashed);
                    Procedures.setInstallable(runner, stashed);
                }
            }
        };
    }

    public static RInstall updateDeviceStatus(Boolean isUnlocked, Boolean hasTwrp, Boolean hasUsbDebug){
        return updateDeviceStatus(isUnlocked, hasTwrp, hasUsbDebug, null);
    }


    public static RInstall updateDeviceStatus(Boolean isUnlocked, Boolean hasTwrp, Boolean hasUsbDebug, Boolean hasStockMiui){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Log.info("Updating the device status: isUnlocked: "+isUnlocked+", hasTwrp: "+hasTwrp+", hasUsbDebug: "+hasUsbDebug+", hasStockMiui: "+hasStockMiui);
                Device device = Procedures.requireDevice(runner);
                if (isUnlocked != null){
                    device.getDeviceProperties().getFastbootProperties().put(DeviceProperties.X_LOCKSTATUS, isUnlocked ? UnlockStatus.UNLOCKED : UnlockStatus.LOCKED);
                }
                if (hasTwrp != null){
                    device.getAnswers().setAnswer(DeviceAnswers.HAS_TWRP, hasTwrp ? YesNoMaybe.YES : YesNoMaybe.NO);
                }
                if (hasUsbDebug != null){
                    device.getAnswers().setNeedDeviceDebug(hasUsbDebug ? YesNoMaybe.NO : YesNoMaybe.YES);
                }
                if (hasStockMiui != null){
                    device.getAnswers().setAnswer(DeviceAnswers.HAS_STOCK_MIUI, hasStockMiui ? YesNoMaybe.YES : YesNoMaybe.NO);
                }
            }
        };
    }

    static final RInstall checkIfProcedureDone(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Installable installable = (Installable) runner.getContext(Procedures.INSTALLABLE);
                Boolean isProcedure = (Boolean) runner.getContext(ChooseProcedure.IS_CHOOSEN_PROCEDURE);
                Boolean skip = installable == null && isProcedure != null && isProcedure == true;
                Log.info("Has a procedure already run and we should skip installable procedure? "+skip);
                runner.setContext(KEY_BOOL_SHOULD_SKIP_INSTALL, skip);
            }
        };
    }

    private static final String KEY_BOOL_SHOULD_SKIP_INSTALL = "bool_should_skip_install";

    public static RInstall restartMain(RInstall startFromHere){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                final Device device = Procedures.requireDevice(runner);
                final Thread lastThread = Thread.currentThread();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ActionsDynamic.MAIN_SCREEN_LOADING(LRes.LOADING).run();
                            lastThread.interrupt();
                            ActionsDynamic.START_PROCEDURE(device, RNode.sequence(unstashContext(), startFromHere), runner).run();
                        } catch (InterruptedException e) {
                            Log.warn("Main tool runner thread interrutped: "+e.getMessage());
                        }
                    }
                }).start();
                Thread.sleep(1000*3600*24);
                Log.error("Not interrupted :(");
            }
        };
    }

    public static RInstall showUserAndRestart(String message, boolean throwUplevel){
        return showUserAndRestart(message, throwUplevel, null);
    }

    public static RInstall showUserAndRestart(String message, boolean throwUplevel, RInstall startFromHere){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                ButtonPane buttonPane = new ButtonPane(throwUplevel ? new LRes[]{LRes.OK_UNDERSTAND, LRes.TRY_AGAIN} : new LRes[]{LRes.OK_UNDERSTAND} );
                buttonPane.setContentText(message);
                WindowManager.setMainContent(buttonPane, false);
                int click = buttonPane.waitClick();
                WindowManager.removeTopContent();
                if (click != 0){
                    throw new RMessage(CommandClass.Command.UPLEVEL);
                }
                restartMain(startFromHere).run(runner);
            }
        };
    }

    public static RInstall main(){
        return RNode.sequence(
                RebootDevice.rebootNoWaitIfConnected(),
                ChooseProcedure.chooseRomCategory(),
                selectRomAndGo()
            );
        /*RebootDevice.rebootNoWaitIfConnected().run(runner);
        Log.debug("PRO0 CHOOSE CAT");
        ChooseProcedure.chooseRomCategory().run(runner);
        Log.debug("PRO0 CHOOSE ROM");
        ChooseProcedure.chooseRom().run(runner);
        Log.debug("PRO0 FETCH RESOURCE");
        ConfirmationProcedure.confirmInstallableProcedure().run(runner);
        ConfirmationProcedure.confirmInstallationStart().run(runner);
        runner.text(LRes.WAITING_DEVICE_ACTIVE);
        ManageDevice.waitDevice(60);
        GenericInstall.satisfyAllRequirements().run(runner);
        GenericInstall.resourceFetchWait().run(runner);
        DeviceManager.refresh();
        GenericInstall.runInstallProcedure().run(runner);
        GenericInstall.installationSuccess().run(runner);*/
    }

    private static HashMap<String, Object> context;
    private static RInstall stashContext(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                context = new HashMap<>();
                runner.stashEntireContext(context);
            }
        };
    }

    private static RInstall unstashContext(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                if (context == null){
                    return;
                }
                runner.reloadContext(context);
                context = null;
            }
        };
    }

    public static RInstall selectRomAndGo() {
        return RNode.sequence(
                stashContext(),
                ChooseProcedure.chooseRom(),
                checkIfProcedureDone(),
                RNode.conditional(KEY_BOOL_SHOULD_SKIP_INSTALL,
                        Procedures.doNothing(),
                        RNode.sequence(
                                ConfirmationProcedure.confirmInstallableProcedure(),
                                ConfirmationProcedure.confirmInstallationStart(),
                                RNode.sequence(
                                        ManageDevice.waitRequireAccessible(30, Device.Status.DEVICE),
                                        RNode.sequence(
                                                GenericInstall.satisfyAllRequirements(),
                                                GenericInstall.resourceFetchWait(),
                                                GenericInstall.runInstallProcedure())
                                )
                        )
                ),
                GenericInstall.installationSuccess());
    }
}
