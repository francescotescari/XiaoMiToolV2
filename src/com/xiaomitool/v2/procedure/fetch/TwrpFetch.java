package com.xiaomitool.v2.procedure.fetch;

import com.xiaomitool.v2.adb.device.DeviceProperties;
import com.xiaomitool.v2.inet.CustomHttpException;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.*;
import com.xiaomitool.v2.procedure.install.GenericInstall;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.procedure.retrive.Twrp;
import com.xiaomitool.v2.rom.RomException;
import com.xiaomitool.v2.rom.TwrpFile;
import javafx.stage.FileChooser;

import java.io.File;

public class TwrpFetch {

     static RInstall findTwrpMeInstallable(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                String codename = Procedures.requireDevice(runner).getDeviceProperties().getCodename(true);
                if ("sirius".equals(codename.toLowerCase())){
                    codename = "aNotExistingDevice";
                }
                runner.text(LRes.TWRP_SEARCHING_FROM.toString(codename, "Twrp.me"));
                TwrpFile file;

                try {
                     file = Twrp.latestTwrpMe(codename);
                } catch (CustomHttpException e) {
                    throw new InstallException(e);
                } catch (RomException e) {
                    throw new InstallException(e);
                }
                runner.setContext(Procedures.INSTALLABLE, file);
            }

        };
    }

     static RInstall findAfhInstallable(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Log.info("Searching TWRP on android file host");
                String codename = Procedures.requireDevice(runner).getDeviceProperties().getCodename(true);
                TwrpFile file;
                runner.text(LRes.TWRP_SEARCHING_FROM.toString(codename, "AndroidFileHost"));
                try {
                    file = Twrp.latestTwrpAfh(codename);
                } catch (CustomHttpException e) {
                    throw new InstallException(e);
                } catch (RomException e) {
                    throw new InstallException(e);
                }
                runner.setContext(Procedures.INSTALLABLE, file);
            }

        };
    }

    static RInstall selectTwrpFromPC() {
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Log.info("Selecting twrp from PC");
                String codename = Procedures.requireDevice(runner).getDeviceProperties().getCodename(true);
                File file = Procedures.selectFileFromPc(LRes.TWRP_SELECT_FILE.toString(), LRes.TWRP_SELECT_FILE_EXP.toString("sirius".equals(codename.toLowerCase()) ? (codename+" xiaomi") : codename), new FileChooser.ExtensionFilter("Generic TWRP file","*.img","*.zip"),new FileChooser.ExtensionFilter("Image file", "*.img"), new FileChooser.ExtensionFilter("Compressed image file", "*.zip"));
                if (file == null){
                    GenericInstall.restartMain(GenericInstall.selectRomAndGo()).run(runner);
                    return;
                }
                TwrpFile twrpFile = new TwrpFile(file,codename);
                runner.setContext(Procedures.INSTALLABLE, twrpFile);
            }
        };
    }

    public  static RInstall fetchTwrpFallbackToPc(){
         return RNode.fallback(findTwrpMeInstallable(),findAfhInstallable(),selectTwrpFromPC());
    }
    public static RInstall fetchTwrp(){
         return RNode.fallback(findTwrpMeInstallable(), findAfhInstallable(), Procedures.doNothing());
    }
}
