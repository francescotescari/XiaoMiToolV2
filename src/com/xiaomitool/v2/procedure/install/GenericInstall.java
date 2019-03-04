package com.xiaomitool.v2.procedure.install;

import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.adb.device.DeviceAnswers;
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
import com.xiaomitool.v2.procedure.*;
import com.xiaomitool.v2.procedure.device.ManageDevice;
import com.xiaomitool.v2.procedure.device.RebootDevice;
import com.xiaomitool.v2.procedure.uistuff.ChooseProcedure;
import com.xiaomitool.v2.rom.Installable;
import com.xiaomitool.v2.rom.chooser.InstallationRequirement;
import com.xiaomitool.v2.tasks.UpdateListener;
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

import java.util.Objects;

import static com.xiaomitool.v2.engine.CommonsMessages.NOOP;

public class GenericInstall {

    public static RInstall resourceDownload(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Log.debug("Downloading resource");
                Installable installable = Procedures.requireInstallable(runner);
                if(!installable.isNeedDownload()){
                    Log.debug("No need to downlaod");
                    return;
                }
                if (StrUtils.isNullOrEmpty(installable.getDownloadUrl())){
                    throw new InstallException("Donwload failed: empty or null download url", InstallException.Code.DOWNLOAD_FAILED, false);
                }
                ProgressPane.DefProgressPane defProgressPane = new ProgressPane.DefProgressPane();
                defProgressPane.setContentText(LRes.DOWNLOADING_ROM_FILE.toString()+"\n"+FilenameUtils.getName(installable.getDownloadUrl()));
                UpdateListener listener = defProgressPane.getUpdateListener(1000);
                WindowManager.setMainContent(defProgressPane,false);
                try {
                    installable.download(listener);
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
                if(!installable.isNeedExtraction()){
                    Log.debug("No need to extract");
                    return;
                }
                if (installable.getDownloadedFile() == null){
                    throw new InstallException("Extract failed: null downloaded file", InstallException.Code.EXTRACTION_FAILED, false);
                }
                ProgressPane.DefProgressPane defProgressPane = new ProgressPane.DefProgressPane();
                defProgressPane.setContentText(LRes.EXTRACTING_ROM_FILE+"\n"+installable.getDownloadedFile().toString());
                UpdateListener listener = defProgressPane.getUpdateListener(333);
                WindowManager.setMainContent(defProgressPane,false);
                try {
                    installable.extract(listener);
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
                    return;
                }
                installable = Procedures.requireInstallable(runner);
                Procedures.pushRInstallOnStack(runner,installable.getInstallProcedure());
            }
        }, Procedures.runStackedProcedures());
    }

    public static RInstall installationSuccess() {
        return RNode.sequence(RebootDevice.rebootNoWaitIfConnected(), new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, InterruptedException {
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
                Installable stashed = (Installable) runner.getContext(KEY_STASHED_INSTALLABLE);
                Installable installable = Procedures.requireInstallable(runner);
                Device device = Procedures.requireDevice(runner);
                if (stashed == null){
                    runner.setContext(KEY_STASHED_INSTALLABLE, installable);
                }
                Log.debug("Satisfying all requirements");
                RInstall toSatisfy = InstallationRequirement.satisfyNextRequirement(Procedures.requireDevice(runner),installable);
                RInstall copy = null;
                while (toSatisfy != null){
                    Log.debug("Statisfying requrement: "+toSatisfy.toString());
                    toSatisfy.run(runner);
                    copy = toSatisfy;
                    toSatisfy = InstallationRequirement.satisfyNextRequirement(Procedures.requireDevice(runner),installable);
                    if (Objects.equals(copy, toSatisfy)){
                        throw new InstallException("Trying to satisfy a requirement that should had been just satisfied: "+copy.toString(), InstallException.Code.INTERNAL_ERROR, false);
                    }
                    if (YesNoMaybe.YES.equals(device.getAnswers().isNeedDeviceDebug())){
                        RebootDevice.rebootNoWaitIfConnected().run(runner);
                        ActionsDynamic.WAIT_USB_DEBUG_ENABLE(device).run();
                    }
                    try {
                        ManageDevice.waitDevice(30).setFlag(RNode.FLAG_THROWRAWEXCEPTION, true).run(runner);
                    } catch (InstallException e){
                        Log.warn("Starting next requirement satisfaction without device active");
                    }
                }
                stashed = (Installable) runner.getContext(KEY_STASHED_INSTALLABLE);
                if (stashed != null){
                    Log.debug("Reloading stashed installable: "+stashed);
                    Procedures.setInstallable(runner, stashed);
                }
            }
        };
    }


    public static RInstall updateDeviceStatus(Boolean isUnlocked, Boolean hasTwrp, Boolean hasUsbDebug){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
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
            }
        };
    }

}
