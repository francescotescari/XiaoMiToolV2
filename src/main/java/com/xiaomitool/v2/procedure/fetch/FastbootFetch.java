package com.xiaomitool.v2.procedure.fetch;

import com.xiaomitool.v2.adb.AdbException;
import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.inet.CustomHttpException;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.*;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.rom.Installable;
import com.xiaomitool.v2.rom.MiuiRom;
import com.xiaomitool.v2.rom.MiuiTgzRom;
import com.xiaomitool.v2.rom.chooser.InstallableChooser;
import com.xiaomitool.v2.utility.utils.SettingsUtils;
import com.xiaomitool.v2.xiaomi.XiaomiProcedureException;
import com.xiaomitool.v2.xiaomi.miuithings.Branch;
import com.xiaomitool.v2.xiaomi.miuithings.DeviceRequestParams;
import com.xiaomitool.v2.xiaomi.miuithings.RequestParams;
import com.xiaomitool.v2.xiaomi.romota.MiuiRomOta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.xiaomitool.v2.rom.chooser.InstallableChooser.idBySpecie;

public class FastbootFetch {
    public static RInstall findLatestFastboot(MiuiRom.Specie specie) {
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Log.info("Searching latest fastboot rom: specie: " + specie);
                MiuiRom.Specie sp = specie;
                InstallableChooser chooser = Procedures.requireInstallableChooser(runner);
                Device device = Procedures.requireDevice(runner);
                RequestParams params;
                try {
                    params = DeviceRequestParams.readFromDevice(device);
                } catch (AdbException e) {
                    throw new InstallException(e);
                }
                if (sp == null) {
                    sp = params.getSpecie();
                    if (sp == null) {
                        throw new InstallException("Failed to obtain device branch and region: null specie", InstallException.Code.MISSING_PROPERTY);
                    }
                } else {
                    params.setSpecie(sp);
                }
                String id = idBySpecie(sp);
                Installable rom = chooser.getByType(id, Installable.Type.FASTBOOT);
                try {
                    if (rom == null) {
                        try {
                            runner.text(LRes.SEARCHING_LATEST_FASTBOOT.toString(params.getSpecie().toHuman()));
                            rom = MiuiRomOta.latestFastboot_request(params);
                        } catch (XiaomiProcedureException e) {
                            try {
                                Log.info("Using fastboot fetch method 2");
                                rom = MiuiRomOta.latestFastboot2_request(params);
                            } catch (XiaomiProcedureException e2) {
                                throw new InstallException(e2);
                            }
                        }
                        Log.info("Fastboot rom for specie " + specie + " found: " + rom);
                        chooser.add(id, rom);
                    }
                } catch (CustomHttpException e) {
                    throw new InstallException(e);
                }
                Procedures.setInstallable(runner, rom);
            }
        };
    }

    public static RInstall findAllLatestFastboot(Set<MiuiRom.Specie> speciesToSearch) {
        RInstall[] procedures = new RInstall[speciesToSearch.size()];
        int i = 0;
        for (MiuiRom.Specie specie : speciesToSearch) {
            procedures[i++] = findLatestFastboot(specie);
        }
        return RNode.skipOnException(procedures);
    }

    public static RInstall findBestRecoveryFastboot() {
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                SettingsUtils.Region region = SettingsUtils.getRegion();
                List<SettingsUtils.Region> tryRegions = new ArrayList<>();
                if (SettingsUtils.Region.CN.equals(region)) {
                    tryRegions.add(SettingsUtils.Region.CN);
                    tryRegions.add(SettingsUtils.Region.GLOBAL);
                } else if (SettingsUtils.Region.EU.equals(region)) {
                    tryRegions.add(SettingsUtils.Region.EU);
                    tryRegions.add(SettingsUtils.Region.GLOBAL);
                    tryRegions.add(SettingsUtils.Region.CN);
                } else {
                    if (!SettingsUtils.Region.GLOBAL.equals(region) && !SettingsUtils.Region.OTHER.equals(region)) {
                        tryRegions.add(region);
                    }
                    tryRegions.add(SettingsUtils.Region.GLOBAL);
                    tryRegions.add(SettingsUtils.Region.EU);
                    tryRegions.add(SettingsUtils.Region.CN);
                }
                RequestParams params;
                try {
                    params = DeviceRequestParams.readFromDevice(device, false);
                } catch (AdbException e) {
                    throw new InstallException(e);
                }
                runner.text(LRes.SEARCHING_BEST_ROM_TO_RECOVER);
                Branch[] branches = new Branch[]{Branch.DEVELOPER, Branch.STABLE};
                for (SettingsUtils.Region reg : tryRegions) {
                    for (Branch bi : branches) {
                        MiuiRom.Specie sp = MiuiRom.Specie.fromZoneBranch(reg, bi);
                        params.setSpecie(sp);
                        MiuiTgzRom rom;
                        try {
                            try {
                                rom = MiuiRomOta.latestFastboot_request(params);
                            } catch (XiaomiProcedureException e) {
                                try {
                                    rom = MiuiRomOta.latestFastboot2_request(params);
                                } catch (XiaomiProcedureException e2) {
                                    rom = null;
                                }
                            }
                        } catch (CustomHttpException e) {
                            throw new InstallException(e);
                        }
                        if (rom != null) {
                            InstallableChooser c = Procedures.requireInstallableChooser(runner);
                            c.add(idBySpecie(sp), rom);
                            Procedures.setInstallable(runner, rom);
                            return;
                        }
                    }
                }
                throw new InstallException("Failed to get a fastboot image for this device: " + device.getDeviceProperties().getCodename(true), InstallException.Code.INFO_RETRIVE_FAILED);
            }
        };
    }
}
