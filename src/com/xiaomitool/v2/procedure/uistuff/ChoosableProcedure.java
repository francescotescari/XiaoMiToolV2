package com.xiaomitool.v2.procedure.uistuff;

import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.adb.device.DeviceProperties;
import com.xiaomitool.v2.gui.drawable.DrawableManager;
import com.xiaomitool.v2.gui.visual.ChooserPane;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.procedure.*;
import com.xiaomitool.v2.procedure.device.ManageDevice;
import com.xiaomitool.v2.procedure.fetch.StockRecoveryFetch;
import com.xiaomitool.v2.procedure.fetch.TwrpFetch;
import com.xiaomitool.v2.procedure.install.FastbootInstall;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.procedure.install.TwrpInstall;
import com.xiaomitool.v2.rom.*;
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
                    }
                    String lowPath = path.toLowerCase();
                    Device device = Procedures.requireDevice(runner);
                    if (lowPath.endsWith(".zip")){
                        installable = new MiuiZipRom(file, true);
                        toDoNext = StockRecoveryFetch.createValidatedZipInstall(device);
                    } else if (lowPath.endsWith(".tgz") || lowPath.endsWith(".tar.gz")){
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
                    }
                    String lowPath = path.toLowerCase();
                    if (lowPath.endsWith(".zip")){
                        installable = new ZipRom(file) {
                            @Override
                            public ChooserPane.Choice getChoice() {
                                return getChoiceInternal();
                            }
                        };
                    } else if (lowPath.endsWith(".tgz") || lowPath.endsWith(".tar.gz")){
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


    public static final ChoosableProcedure RECOVERY_IMAGE = new ChoosableProcedure() {
        @Override
        protected ChooserPane.Choice getChoiceInternal() {
            return new ChooserPane.Choice(LRes.CUSTOM_RECOVERY.toString(), LRes.CUSTOM_RECOVERY_TEXT.toString(), new Image(DrawableManager.getPng("localpc.png").toString()));
        }

        @Override
        public RInstall getProcedure() {
            return new RInstall() {
                @Override
                public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                    File file = Procedures.selectFileFromPc(LRes.FILE_SELECT_TIT.toString(), LRes.FILE_SELECT_TEXT.toString(), new FileChooser.ExtensionFilter("Recovery image file or archive","*.img"));
                    Installable installable = null;
                    String path = file.getAbsolutePath();
                    String codename = Procedures.requireDevice(runner).getDeviceProperties().getCodename(true);
                    if (path == null){
                        throw new InstallException("File selected is null", InstallException.Code.FILE_NOT_FOUND, true);
                    }
                    String lowPath = path.toLowerCase();
                    if (lowPath.endsWith(".img")){
                        installable = new TwrpFile(file, codename) {
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
            return RNode.sequence(ManageDevice.waitRequireAccessible(30, Device.Status.DEVICE),FastbootInstall.unlockBootloader());
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
            return RNode.sequence(new RInstall() {
                @Override
                public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                    Procedures.setInstallable(runner,null);
                }
            },ChooseProcedure.chooseRomCategory(), ChooseProcedure.chooseRom());
        }
    };


    protected abstract ChooserPane.Choice getChoiceInternal();

    @Override
    public ChooserPane.Choice getChoice() {
        return getChoiceInternal();
    }



}
