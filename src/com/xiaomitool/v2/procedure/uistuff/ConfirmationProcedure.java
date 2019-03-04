package com.xiaomitool.v2.procedure.uistuff;

import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.gui.visual.ButtonPane;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.procedure.*;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.rom.Installable;
import com.xiaomitool.v2.rom.chooser.InstallationRequirement;

import java.util.List;

public class ConfirmationProcedure {

    public static RInstall confirmInstallableProcedure(){
        return RNode.fallback(new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Procedures.saveProcedure("confirmInstallableProcedure",this).run(runner);
                Installable installable = Procedures.requireInstallable(runner);
                Device device = Procedures.requireDevice(runner);
                List<InstallationRequirement> requirements = InstallationRequirement.getAllInstallableRequirements(installable,device);
                if (requirements.isEmpty()){
                    return;
                }
                StringBuilder text = new StringBuilder(LRes.CONFIRM_REQUIREMENTS_TEXT.toString(LRes.CONTINUE.toString(), LRes.CANCEL.toString()));
                for (InstallationRequirement requirement : requirements){
                    text.append("- ").append(requirement.getHumanName(device)).append("\n");
                }
                ButtonPane buttonPane = new ButtonPane(LRes.CONTINUE, LRes.CANCEL);
                buttonPane.setContentText(text.toString());
                WindowManager.setMainContent(buttonPane, false);
                int click = buttonPane.waitClick();
                WindowManager.removeTopContent();
                if (click != 0){
                    throw new InstallException("You should not see this text", InstallException.Code.INTERNAL_ERROR, false);
                }

            }
        },RNode.sequence(ChooseProcedure.chooseRom(), Procedures.runSavedProcedure("confirmInstallableProcedure")));
    }
}
