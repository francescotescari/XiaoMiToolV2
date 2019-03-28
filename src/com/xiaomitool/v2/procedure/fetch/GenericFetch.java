package com.xiaomitool.v2.procedure.fetch;

import com.xiaomitool.v2.adb.AdbException;
import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.adb.device.DeviceProperties;
import com.xiaomitool.v2.inet.CustomHttpException;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.*;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.procedure.retrive.XiaomiEuRoms;
import com.xiaomitool.v2.rom.Installable;
import com.xiaomitool.v2.rom.MiuiRom;
import com.xiaomitool.v2.rom.RomException;
import com.xiaomitool.v2.rom.ZipRom;
import com.xiaomitool.v2.rom.chooser.InstallableChooser;
import com.xiaomitool.v2.utility.utils.SettingsUtils;
import com.xiaomitool.v2.utility.utils.StrUtils;
import com.xiaomitool.v2.xiaomi.miuithings.Branch;
import com.xiaomitool.v2.xiaomi.miuithings.DeviceRequestParams;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashSet;

public class GenericFetch {

    public static final String SELECTED_FILE = "gen_sel_file";
    public static final String FILE_MD5 = "gen_file_md5";




    public static RInstall fetchAllOfficial(Device device){
        SettingsUtils.Region region = SettingsUtils.getRegion();
        String codename = (String) device.getDeviceProperties().get(DeviceProperties.CODENAME);
        LinkedHashSet<MiuiRom.Specie> speciesToSearch = MiuiRom.Specie.listToSearchSpecies(region,codename);
        return RNode.sequence(FastbootFetch.findAllLatestFastboot(speciesToSearch),StockRecoveryFetch.allLatestOta(speciesToSearch));
    }

    public static RInstall computeMD5File() {
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {

                File file = (File) runner.requireContext(SELECTED_FILE);
                Log.info("Computing MD5 hash of file: "+file);
                if (!file.exists()){
                    throw new InstallException("File not found: "+file.toString(), InstallException.Code.FILE_NOT_FOUND, false);
                }
                String md5 = "";
                runner.text(LRes.CALCULATING_MD5);
                try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
                    md5 = DigestUtils.md5Hex(in);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (StrUtils.isNullOrEmpty(md5)){
                    throw new InstallException("Failed to calculate file md5: "+file.toString(), InstallException.Code.HASH_FAILED, true);
                }
                runner.setContext(FILE_MD5, md5);
            }
        };
    }

    public static RInstall fetchXiaomieuRom(Branch branch){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Log.info("Fetching latest xiaomi.eu rom: branch: "+branch);
                Device device = Procedures.requireDevice(runner);
                try {
                    DeviceRequestParams requestParams = DeviceRequestParams.readFromDevice(device, false);
                    MiuiRom.Specie specie = requestParams.getSpecie();
                    specie.setBranch(branch);
                    requestParams.setSpecie(specie);
                    runner.text(LRes.SEARCHING_XIAOMIEU_ROM.toString(LRes.branchToString(branch)));
                    ZipRom rom = XiaomiEuRoms.latest(requestParams);
                    Procedures.setInstallable(runner, rom);
                } catch (AdbException e) {
                    throw new InstallException(e);
                } catch (RomException e) {
                    throw new InstallException(e);
                } catch (CustomHttpException e) {
                    throw new InstallException(e);
                }


            }
        };

    }

    public static RInstall fetchAllUnofficial(){
        return RNode.skipOnException(RNode.sequence(GenericFetch.fetchXiaomieuRom(Branch.STABLE), new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Installable installable = Procedures.requireInstallable(runner);
                InstallableChooser chooser = Procedures.requireInstallableChooser(runner);
                chooser.add(InstallableChooser.ID_XIAOMIEU_STABLE, installable);
                Procedures.setInstallable(runner,null);
            }
        }),RNode.sequence(GenericFetch.fetchXiaomieuRom(Branch.DEVELOPER), new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Installable installable = Procedures.requireInstallable(runner);
                InstallableChooser chooser = Procedures.requireInstallableChooser(runner);
                chooser.add(InstallableChooser.ID_XIAOMIEU_DEV, installable);
                Procedures.setInstallable(runner,null);
            }
        }));
    }


    public static RInstall fetchAllMods(){
        return RNode.skipOnException(RNode.sequence(TwrpFetch.fetchTwrp(), new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Log.debug("Checking if twrp auto found");
                Installable installable = Procedures.requireInstallable(runner);
                InstallableChooser chooser = Procedures.requireInstallableChooser(runner);
                chooser.add(InstallableChooser.ID_INSTALL_TWRP,installable);
                Procedures.setInstallable(runner,null);
                Log.debug("Auto twrp added");
            }
        }), RNode.sequence(ModFetch.fetchMagiskStable(), new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Installable installable = Procedures.requireInstallable(runner);
                InstallableChooser chooser = Procedures.requireInstallableChooser(runner);
                chooser.add(InstallableChooser.ID_INSTALL_MAGISK,installable);
                Procedures.setInstallable(runner,null);
            }
        }), Procedures.doNothing());
    }

}
