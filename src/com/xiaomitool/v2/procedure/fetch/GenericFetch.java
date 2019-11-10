package com.xiaomitool.v2.procedure.fetch;

import com.xiaomitool.v2.adb.AdbException;
import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.adb.device.DeviceProperties;
import com.xiaomitool.v2.engine.actions.ActionsDynamic;
import com.xiaomitool.v2.gui.WindowManager;
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
import com.xiaomitool.v2.utility.utils.ApkUtils;
import com.xiaomitool.v2.utility.utils.SettingsUtils;
import com.xiaomitool.v2.utility.utils.StrUtils;
import com.xiaomitool.v2.xiaomi.XiaomiProcedureException;
import com.xiaomitool.v2.xiaomi.XiaomiUtilities;
import com.xiaomitool.v2.xiaomi.miuithings.Branch;
import com.xiaomitool.v2.xiaomi.miuithings.DeviceRequestParams;
import com.xiaomitool.v2.xiaomi.romota.MiuiRomOta;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class GenericFetch {

    public static final String SELECTED_FILE = "gen_sel_file";
    public static final String FILE_MD5 = "gen_file_md5";
    public static final String PACKAGE_NAME = "apk_package_name";




    public static RInstall fetchAllOfficial(Device device){
        SettingsUtils.Region region = SettingsUtils.getRegion();
        String codename = device.getDeviceProperties().getCodename(false);
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
                    throw new InstallException("File not found: "+file.toString(), InstallException.Code.FILE_NOT_FOUND);
                }
                String md5 = "";
                runner.text(LRes.CALCULATING_MD5);
                try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
                    md5 = DigestUtils.md5Hex(in);
                } catch (IOException e) {
                    throw new InstallException("Failed to calculate file md5: "+file, InstallException.Code.HASH_FAILED, e);
                }
                if (StrUtils.isNullOrEmpty(md5)){
                    throw new InstallException("Failed to calculate file md5: "+file.toString(), InstallException.Code.HASH_FAILED, "Null or empty result hash");
                }
                runner.setContext(FILE_MD5, md5);
            }
        };
    }

    public static RInstall getPackageName(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                if (runner.getContext(PACKAGE_NAME) != null){
                    return;
                }
                File apk = (File) runner.requireContext(SELECTED_FILE);
                String packageName = ApkUtils.getPackageName(apk.toPath());
                runner.setContext(PACKAGE_NAME, packageName);
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


    public static RInstall fetchDeviceCodename(String keyDest){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                ActionsDynamic.MAIN_SCREEN_LOADING().run();
                JSONObject data = null;
                HashMap<String, String> res = new HashMap<>();
                try {
                    data = MiuiRomOta.deviceNames_request();
                } catch (XiaomiProcedureException | CustomHttpException e) {
                    Log.error("Failed to get updated devices list: "+e.getMessage());
                }
                if (data != null) {
                    for (String key : data.keySet()) {
                        key = XiaomiUtilities.stripCodename(key);
                        if (res.containsKey(key)) {
                            continue;
                        }
                        try {
                            JSONObject obj = data.getJSONObject(key);
                            String name = obj.getString("display_name_en");
                            if (name.isEmpty()) {
                                name = obj.getString("display_name");
                            }
                            res.put(key, name);
                        } catch (JSONException e) {
                            continue;
                        }
                    }
                }
                for (Map.Entry<String, String> e : XiaomiUtilities.getDeviceCodenames().entrySet()){
                    res.put(e.getKey(), e.getValue());
                }
                WindowManager.removeTopContent(false);
                if (res.isEmpty()){
                    throw new InstallException("Empty codename list from api", InstallException.Code.INFO_RETRIVE_FAILED);
                }
                runner.setContext(keyDest, res);
            }
        };
    }

}
