package com.xiaomitool.v2.procedure.uistuff;

import com.xiaomitool.v2.gui.drawable.DrawableManager;
import com.xiaomitool.v2.gui.visual.ChooserPane;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.procedure.*;
import com.xiaomitool.v2.procedure.fetch.StockRecoveryFetch;
import com.xiaomitool.v2.procedure.fetch.TwrpFetch;
import com.xiaomitool.v2.procedure.install.FastbootInstall;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.procedure.install.TwrpInstall;
import com.xiaomitool.v2.rom.Installable;
import com.xiaomitool.v2.rom.MiuiTgzRom;
import com.xiaomitool.v2.rom.MiuiZipRom;
import com.xiaomitool.v2.rom.ZipRom;
import com.xiaomitool.v2.utility.Choiceable;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;

import java.io.File;


public abstract class ChoosableProcedure implements Choiceable, ProcedureBundled {

    public static ChoosableProcedure OFFICIAL_ROM_INSTALL = new ChoosableProcedure() {
        @Override
        public RInstall getProcedure() {
            return new RInstall() {
                @Override
                public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                    File file = Procedures.selectFileFromPc(LRes.FILE_SELECT_OFFICIAL_TIT.toString(), LRes.FILE_SELECT_OFFICIAL_TEXT.toString(), new FileChooser.ExtensionFilter("MIUI file","*.tgz","*.zip","*.tar.gz"));
                    Installable installable = null;
                    String path = file.getAbsolutePath();
                    RInstall toDoNext = null;
                    if (path == null){
                        throw new InstallException("File selected is null", InstallException.Code.FILE_NOT_FOUND, true);
                    } else if (path.endsWith(".zip")){
                        installable = new MiuiZipRom(file, true);
                        toDoNext = StockRecoveryFetch.createValidatedZipInstall();
                    } else if (path.endsWith(".tgz") || path.endsWith(".tar.gz")){
                        installable = new MiuiTgzRom(file, true);
                    } else {
                        throw new InstallException("Unknown file extension: "+FilenameUtils.getExtension(path), InstallException.Code.FILE_NOT_FOUND, true);
                    }
                    if (installable != null){
                        Procedures.setInstallable(runner,installable);
                    }
                    if (toDoNext != null){
                        Procedures.pushRInstallOnStack(runner,toDoNext);
                    }
                }
            }.next();
        }

        @Override
        public ChooserPane.Choice getChoiceInternal() {
            return new ChooserPane.Choice(LRes.ROM_LOCAL_OFFICIAL.toString(), LRes.ROM_LOCAL_OFFICIAL_SUB.toString(), new Image(DrawableManager.getPng("localpc.png").toString()));
        }
    };

    public static ChoosableProcedure UNOFFICIAL_ROM_INSTALL = new ChoosableProcedure() {
        @Override
        public RInstall getProcedure() {
            return new RInstall() {
                @Override
                public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                    File file = Procedures.selectFileFromPc(LRes.FILE_SELECT_TIT.toString(), LRes.FILE_SELECT_TEXT.toString(), new FileChooser.ExtensionFilter("Rom file","*.tgz","*.zip","*.tar.gz"));
                    Installable installable = null;
                    String path = file.getAbsolutePath();

                    if (path == null){
                        throw new InstallException("File selected is null", InstallException.Code.FILE_NOT_FOUND, true);
                    } else if (path.endsWith(".zip")){
                        installable = new ZipRom(file) {
                            @Override
                            public ChooserPane.Choice getChoice() {
                                return getChoiceInternal();
                            }
                        };
                    } else if (path.endsWith(".tgz") || path.endsWith(".tar.gz")){
                        installable = new MiuiTgzRom(file, false);
                    } else {
                        throw new InstallException("Unknown file extension: "+FilenameUtils.getExtension(path), InstallException.Code.FILE_NOT_FOUND, true);
                    }
                    Procedures.setInstallable(runner,installable);
                }
            };
        }

        @Override
        public ChooserPane.Choice getChoiceInternal() {
            return new ChooserPane.Choice(LRes.ROM_LOCAL.toString(), LRes.ROM_LOCAL_TEXT.toString(), new Image(DrawableManager.getPng("localpc.png").toString()));
        }
    };

    public static final ChoosableProcedure GENERIC_MOD_ZIP = new ChoosableProcedure() {
        @Override
        public RInstall getProcedure() {
            return new RInstall() {
                @Override
                public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                    File file = Procedures.selectFileFromPc(LRes.FILE_SELECT_TIT.toString(), LRes.FILE_SELECT_TEXT.toString(), new FileChooser.ExtensionFilter("Mod file","*.zip"));
                    Installable installable = null;
                    String path = file.getAbsolutePath();

                    if (path == null){
                        throw new InstallException("File selected is null", InstallException.Code.FILE_NOT_FOUND, true);
                    } else if (path.endsWith(".zip")){
                        installable = new ZipRom(file) {
                            @Override
                            public ChooserPane.Choice getChoice() {
                                return getChoiceInternal();
                            }
                        };
                    } else {
                        throw new InstallException("Unknown file extension: "+FilenameUtils.getExtension(path), InstallException.Code.FILE_NOT_FOUND, true);
                    }
                    Procedures.setInstallable(runner,installable);
                }
            };
        }

        @Override
        public ChooserPane.Choice getChoiceInternal() {
            return new ChooserPane.Choice(LRes.MOD_LOCAL.toString(), LRes.MOD_LOCAL_TEXT.toString(),  new Image(DrawableManager.getPng("localpc.png").toString()));
        }
    };

    public static final ChoosableProcedure UNLOCK_DEVICE = new ChoosableProcedure() {
        @Override
        public RInstall getProcedure() {
            return FastbootInstall.unlockBootloader();
        }

        @Override
        public ChooserPane.Choice getChoiceInternal() {
            return new ChooserPane.Choice(LRes.UNLOCK_BOOTLOADER.toString(), LRes.UNLOCK_DEVICE_BOOTLOADER.toString(), new Image(DrawableManager.getPng("unlock.png").toString()));
        }
    };

    public static final ChoosableProcedure BACK_TO_CATEGORIES = new ChoosableProcedure() {
        @Override
        protected ChooserPane.Choice getChoiceInternal() {
            return new ChooserPane.Choice(LRes.BACK_TO_CATEGORIES.toString(), LRes.BACK_TO_CATEGORIES_TEXT.toString(), new Image(DrawableManager.getPng("back.png").toString()));
        }

        @Override
        public RInstall getProcedure() {
            return RNode.sequence(ChooseProcedure.chooseRomCategory(), ChooseProcedure.chooseRom());
        }
    };


    protected abstract ChooserPane.Choice getChoiceInternal();

    @Override
    public ChooserPane.Choice getChoice() {
        return getChoiceInternal();
    }



}
