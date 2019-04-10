package com.xiaomitool.v2.procedure.uistuff;

import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.gui.drawable.DrawableManager;
import com.xiaomitool.v2.gui.visual.ChooserPane;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.procedure.*;
import com.xiaomitool.v2.procedure.device.ManageDevice;
import com.xiaomitool.v2.procedure.fetch.StockRecoveryFetch;
import com.xiaomitool.v2.procedure.install.FastbootInstall;
import com.xiaomitool.v2.procedure.install.GenericInstall;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.rom.*;
import com.xiaomitool.v2.rom.interfaces.InstallObject;
import com.xiaomitool.v2.utility.Choiceable;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;


public abstract class ChoosableProcedure implements Choiceable, InstallObject {

    public static ChoosableProcedure OFFICIAL_ROM_INSTALL = new ChoosableProcedure() {
        @Override
        public String getTitle() {
            return LRes.ROM_LOCAL_OFFICIAL.toString();
        }

        @Override
        public String getText() {
            return LRes.ROM_LOCAL_OFFICIAL_SUB.toString();
        }

        @Override
        public Image getIcon() {
            return DrawableManager.getResourceImage(DrawableManager.LOCAL_PC);
        }

        @Override
        public LinkedHashSet<Device.Status> getRequiredStates() {
            return SET_SIDELOAD;
        }

        @Override
        public Installable.Type getInstallType() {
            return Installable.Type.RECOVERY;
        }


        @Override
        public RInstall getInstallProcedure() {
            return new RInstall() {
                @Override
                public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                    File file = Procedures.selectFileFromPc(LRes.FILE_SELECT_OFFICIAL_TIT.toString(), LRes.FILE_SELECT_OFFICIAL_TEXT.toString(), new FileChooser.ExtensionFilter("MIUI file","*.tgz","*.zip","*.tar.gz"));
                    if (file == null){
                        GenericInstall.restartMain(GenericInstall.selectRomAndGo()).run(runner);
                        return;
                    }
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
                        toDoNext = StockRecoveryFetch.createValidatedZipInstall(runner);
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


    };

    public static ChoosableProcedure UNOFFICIAL_ROM_INSTALL = new ChoosableProcedure() {
        @Override
        public String getTitle() {
            return LRes.ROM_LOCAL.toString();
        }

        @Override
        public String getText() {
            return LRes.ROM_LOCAL_TEXT.toString();
        }

        @Override
        public Image getIcon() {
            return DrawableManager.getResourceImage(DrawableManager.LOCAL_PC);
        }



        @Override
        public LinkedHashSet<Device.Status> getRequiredStates() {
            return SET_RECOVERY;
        }

        @Override
        public Installable.Type getInstallType() {
            return Installable.Type.RECOVERY;
        }

        @Override
        public RInstall getInstallProcedure() {
            return new RInstall() {
                @Override
                public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                    File file = Procedures.selectFileFromPc(LRes.FILE_SELECT_TIT.toString(), LRes.FILE_SELECT_TEXT.toString(), new FileChooser.ExtensionFilter("Rom file","*.tgz","*.zip","*.tar.gz"));
                    if (file == null){
                        GenericInstall.restartMain(GenericInstall.selectRomAndGo()).run(runner);
                        return;
                    }
                    Installable installable = null;
                    String path = file.getAbsolutePath();

                    if (path == null){
                        throw new InstallException("File selected is null", InstallException.Code.FILE_NOT_FOUND, true);
                    }
                    String lowPath = path.toLowerCase();
                    if (lowPath.endsWith(".zip")){
                        installable = new LocalZipRomFile(file);
                    } else if (lowPath.endsWith(".tgz") || lowPath.endsWith(".tar.gz")){
                        installable = new MiuiTgzRom(file, false);
                    } else {
                        throw new InstallException("Unknown file extension: "+FilenameUtils.getExtension(path), InstallException.Code.FILE_NOT_FOUND, true);
                    }
                    Procedures.setInstallable(runner,installable);
                }
            };
        }


    };


    public static final ChoosableProcedure RECOVERY_IMAGE = new ChoosableProcedure() {
        @Override
        public String getTitle() {
            return LRes.CUSTOM_RECOVERY.toString();
        }

        @Override
        public String getText() {
            return LRes.CUSTOM_RECOVERY_TEXT.toString();
        }

        @Override
        public Image getIcon() {
            return DrawableManager.getResourceImage(DrawableManager.LOCAL_PC);
        }

        @Override
        public LinkedHashSet<Device.Status> getRequiredStates() {
            return SET_FASTBOOT;
        }

        @Override
        public Installable.Type getInstallType() {
            return Installable.Type.IMAGE;
        }




        @Override
        public RInstall getInstallProcedure() {
            return new RInstall() {
                @Override
                public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                    File file = Procedures.selectFileFromPc(LRes.FILE_SELECT_TIT.toString(), LRes.FILE_SELECT_TEXT.toString(), new FileChooser.ExtensionFilter("Recovery image file","*.img"));
                    if (file == null){
                        GenericInstall.restartMain(GenericInstall.selectRomAndGo()).run(runner);
                        return;
                    }
                    Installable installable = null;
                    String path = file.getAbsolutePath();
                    String codename = Procedures.requireDevice(runner).getDeviceProperties().getCodename(true);
                    if (path == null){
                        throw new InstallException("File selected is null", InstallException.Code.FILE_NOT_FOUND, true);
                    }
                    String lowPath = path.toLowerCase();
                    if (lowPath.endsWith(".img")){
                        installable = new TwrpFile(file, codename);
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
        public String getTitle() {
            return LRes.MOD_LOCAL.toString();
        }

        @Override
        public String getText() {
            return  LRes.MOD_LOCAL_TEXT.toString();
        }

        @Override
        public Image getIcon() {
            return DrawableManager.getResourceImage(DrawableManager.LOCAL_PC);
        }


        @Override
        public LinkedHashSet<Device.Status> getRequiredStates() {
            return SET_RECOVERY;
        }

        @Override
        public Installable.Type getInstallType() {
            return Installable.Type.RECOVERY;
        }



        @Override
        public RInstall getInstallProcedure() {
            return new RInstall() {
                @Override
                public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                    File file = Procedures.selectFileFromPc(LRes.FILE_SELECT_TIT.toString(), LRes.FILE_SELECT_TEXT.toString(), new FileChooser.ExtensionFilter("Mod file","*.zip"));
                    if (file == null){
                        GenericInstall.restartMain(GenericInstall.selectRomAndGo()).run(runner);
                        return;
                    }
                    Installable installable = null;
                    String path = file.getAbsolutePath();

                    if (path == null){
                        throw new InstallException("File selected is null", InstallException.Code.FILE_NOT_FOUND, true);
                    } else if (path.endsWith(".zip")){
                        installable = new LocalZipRomFile(file);
                    } else {
                        throw new InstallException("Unknown file extension: "+FilenameUtils.getExtension(path), InstallException.Code.FILE_NOT_FOUND, true);
                    }
                    Procedures.setInstallable(runner,installable);
                }
            };
        }


    };

    public static final ChoosableProcedure UNLOCK_DEVICE = new ChoosableProcedure() {
        @Override
        public String getTitle() {
            return LRes.UNLOCK_BOOTLOADER.toString();
        }

        @Override
        public String getText() {
            return LRes.UNLOCK_DEVICE_BOOTLOADER.toString();
        }

        @Override
        public Image getIcon() {
            return DrawableManager.getResourceImage("unlock.png");
        }

        @Override
        public LinkedHashSet<Device.Status> getRequiredStates() {
            return SET_FASTBOOT;
        }

        @Override
        public Installable.Type getInstallType() {
            return Installable.Type.PROCEDURE;
        }

        @Override
        public RInstall getInstallProcedure() {
            return RNode.sequence(ManageDevice.waitRequireAccessible(30, Device.Status.DEVICE),FastbootInstall.unlockBootloader());
        }
    };

    public static final ChoosableProcedure BACK_TO_CATEGORIES = new ChoosableProcedure() {
        @Override
        public String getTitle() {
            return LRes.BACK_TO_CATEGORIES.toString();
        }

        @Override
        public String getText() {
            return LRes.BACK_TO_CATEGORIES_TEXT.toString();
        }

        @Override
        public Image getIcon() {
            return DrawableManager.getResourceImage("back.png");
        }

        @Override
        public LinkedHashSet<Device.Status> getRequiredStates() {
            return new LinkedHashSet<>();
        }

        @Override
        public Installable.Type getInstallType() {
            return Installable.Type.OTHER;
        }

        @Override
        public RInstall getInstallProcedure() {
            return RNode.sequence(new RInstall() {
                @Override
                public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                    Procedures.setInstallable(runner,null);
                }
            },ChooseProcedure.chooseRomCategory(), ChooseProcedure.chooseRom());
        }
    };



    @Override
    public final ChooserPane.Choice getChoice() {
        return new ChooserPane.Choice(this.getTitle(), this.getText(), this.getIcon());
    }


    @Override
    public String toLogString() {
        return this.toString();
    }

    @Override
    public boolean isProcedure() {
        return true;
    }
}
