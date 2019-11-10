package com.xiaomitool.v2.procedure.uistuff;

import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.gui.visual.ButtonPane;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.*;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.rom.Installable;
import com.xiaomitool.v2.rom.chooser.InstallationRequirement;
import com.xiaomitool.v2.utility.utils.InetUtils;
import com.xiaomitool.v2.xiaomi.miuithings.UnlockStatus;

import java.util.List;

public class ConfirmationProcedure {

    public static final String KEY_BOOL_CONFIRM_STEPS = "bool_confirm_steps";


    public static RInstall confirmInstallableProcedure(){
        return RNode.sequence(new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Procedures.saveProcedure("confirmInstallableProcedure",confirmInstallableProcedure()).run(runner);
                Installable installable = Procedures.requireInstallable(runner);
                Device device = Procedures.requireDevice(runner);
                List<InstallationRequirement> requirements = InstallationRequirement.getAllInstallableRequirements(installable,device);
                runner.setContext(KEY_BOOL_CONFIRM_STEPS, Boolean.TRUE);
                if (requirements.isEmpty()){
                    Log.info("No additional requirements are needed for this installation");
                    Log.debug("No requirements for this installation");
                    return;
                }
                StringBuilder text = new StringBuilder(LRes.CONFIRM_REQUIREMENTS_TEXT.toString(LRes.CONTINUE.toString(), LRes.CANCEL.toString()));
                for (InstallationRequirement requirement : requirements){
                    Log.info("Showing requirement: "+requirement.toString());
                    text.append("- ").append(requirement.getHumanName(device)).append("\n");
                }
                ButtonPane buttonPane = new ButtonPane(LRes.CONTINUE, LRes.CANCEL);
                buttonPane.setContentText(text.toString());
                WindowManager.setMainContent(buttonPane, false);
                int click = buttonPane.waitClick();
                WindowManager.removeTopContent();
                if (click != 0){

                    runner.setContext(KEY_BOOL_CONFIRM_STEPS, Boolean.FALSE);
                } else {
                    Log.info("Installation procedure confirmed");
                }

            }
        },RNode.conditional(KEY_BOOL_CONFIRM_STEPS, null, RNode.sequence(ChooseProcedure.chooseRom(), Procedures.runSavedProcedure("confirmInstallableProcedure"))));
    }

    public static RInstall suggestInternetIfMissing(String message, String keyWasSkipped){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                if(InetUtils.isInternetAvailable()){
                    runner.setContext(keyWasSkipped, Boolean.FALSE);
                    return;
                }
                Log.warn("Internet connection is not available, suggest to enable that");
                ButtonPane buttonPane = new ButtonPane(LRes.SKIP, LRes.TRY_AGAIN);
                buttonPane.setContentText(message);
                WindowManager.setMainContent(buttonPane,false);
                int click = buttonPane.waitClick();
                WindowManager.removeTopContent();
                if (click == 0){
                    runner.setContext(keyWasSkipped, Boolean.TRUE);
                    return;
                }
                this.run(runner);
            }
        };
    }

    private static final String KEY_BOOL_CONFIRM_INSTALL = "bool_confirm_install";
    public static RInstall confirmInstallationStart(){
        return RNode.sequence(new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Procedures.saveProcedure("confirmInstallationStart", confirmInstallationStart()).run(runner);
                runner.setContext(KEY_BOOL_CONFIRM_INSTALL, Boolean.TRUE);
                ButtonPane buttonPane = new ButtonPane(LRes.CONTINUE, LRes.CANCEL);
                buttonPane.setContentText(LRes.CONFIRM_INSTALLATION_START.toString(LRes.CONTINUE));
                WindowManager.setMainContent(buttonPane,false);
                int click = buttonPane.waitClick();
                WindowManager.removeTopContent();
                if (click != 0){
                    runner.setContext(KEY_BOOL_CONFIRM_INSTALL, Boolean.FALSE);
                } else {
                    Log.info("Installation confrimation accepted");
                }

            }
        }, RNode.conditional(KEY_BOOL_CONFIRM_INSTALL, null, RNode.sequence(ChooseProcedure.chooseRom(), confirmInstallableProcedure(), Procedures.runSavedProcedure("confirmInstallationStart"))));
    }


    public static RInstall confirmPhoneCharged(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                ButtonPane buttonPane = new ButtonPane(LRes.OK_UNDERSTAND);
                buttonPane.setContentText(LRes.RECOVER_PHONE_CHARGED);
                WindowManager.setMainContent(buttonPane, false);
                buttonPane.waitClick();
                WindowManager.removeTopContent();
            }
        };
    }

    public static String WANT_TO_UNLOCK = "want_to_unlock";
    public static String IS_DEVICE_UNLOCKED = "device_is_unlocked";

    public static RInstall suggestUnlockBootloader(String message){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                UnlockStatus status = device.getAnswers().getUnlockStatus();
                runner.setContext(IS_DEVICE_UNLOCKED, true);
                if (UnlockStatus.UNLOCKED.equals(status)){
                    return;
                }
                runner.setContext(IS_DEVICE_UNLOCKED, false);
                ButtonPane buttonPane = new ButtonPane(LRes.REQ_BOOTLOADER_UNLOCKED, LRes.CONTINUE);
                buttonPane.setContentText(message);
                WindowManager.setMainContent(buttonPane, false);
                int click = buttonPane.waitClick();
                runner.setContext(WANT_TO_UNLOCK, click == 0);
                WindowManager.removeTopContent(true);
            }
        };
    }

    public static RInstall isThisMiuiVersionCantBeInstalled(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {

            }
        };
    }

}
