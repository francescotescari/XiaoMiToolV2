package com.xiaomitool.v2.procedure.fetch;

import com.xiaomitool.v2.adb.AdbException;
import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.inet.CustomHttpException;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.*;
import com.xiaomitool.v2.procedure.install.GenericInstall;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.rom.Installable;
import com.xiaomitool.v2.rom.MiuiRom;
import com.xiaomitool.v2.rom.MiuiZipRom;
import com.xiaomitool.v2.rom.chooser.InstallableChooser;
import com.xiaomitool.v2.utility.utils.SettingsUtils;
import com.xiaomitool.v2.utility.utils.StrUtils;
import com.xiaomitool.v2.xiaomi.XiaomiProcedureException;
import com.xiaomitool.v2.xiaomi.miuithings.DeviceRequestParams;
import com.xiaomitool.v2.xiaomi.miuithings.MiuiVersion;
import com.xiaomitool.v2.xiaomi.miuithings.RequestParams;
import com.xiaomitool.v2.xiaomi.miuithings.UnlockStatus;
import com.xiaomitool.v2.xiaomi.romota.MiuiRomOta;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static com.xiaomitool.v2.rom.chooser.InstallableChooser.idBySpecie;

public class StockRecoveryFetch {
    private static final String ROM_LOG_SB = "rom_log_sb";
    private static final String SRF_MD5 = "SFR_md5_key";

    public static RInstall findInstallWay(MiuiRom.Specie specie) {
        return RNode.fallback(basicOta(specie), RNode.sequence(otaLatestRecovery(specie), new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Installable installable = Procedures.requireInstallable(runner);
                if (StrUtils.isNullOrEmpty(installable.getMd5())) {
                    throw new InstallException("Latest ota package has no md5", InstallException.Code.INFO_RETRIVE_FAILED);
                }
                runner.setContext(GenericFetch.FILE_MD5, installable.getMd5());
            }
        }, otaPkgRom(specie)), Procedures.doNothing());
    }

    public static RInstall basicOta(MiuiRom.Specie specie) {
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Log.info("Basic ota request starting");
                Device device = Procedures.requireDevice(runner);
                DeviceRequestParams params = null;
                try {
                    params = DeviceRequestParams.readFromDevice(device, true);
                } catch (AdbException e) {
                    throw new InstallException(e);
                }
                params.setSpecie(specie);
                MiuiZipRom installable;
                HashMap<MiuiRom.Kind, MiuiZipRom> rom;
                runner.text(LRes.SEARCHING_LATEST_RECOVERY_ROM.toString(params.getSpecie().toHuman()));
                try {
                    rom = MiuiRomOta.otaV3_request(params);
                } catch (XiaomiProcedureException e) {
                    throw new InstallException(e);
                } catch (CustomHttpException e) {
                    throw new InstallException(e);
                }
                Log.info("Basic ota request found " + rom.size() + " valid roms");
                Log.info(rom);
                installable = rom.get(MiuiRom.Kind.PACKAGE);
                if (installable == null) {
                    installable = rom.get(MiuiRom.Kind.LATEST);
                }
                if (installable == null) {
                    installable = rom.get(MiuiRom.Kind.CURRENT);
                }
                if (installable == null) {
                    installable = rom.get(MiuiRom.Kind.INCREMENTAL);
                }
                Log.info("Choosen preferred rom: " + installable);
                if (installable == null || (!installable.hasInstallToken() && !UnlockStatus.UNLOCKED.equals(Procedures.requireDevice(runner).getAnswers().getUnlockStatus()))) {
                    throw new InstallException("Ota response doesn't contain an installable rom data", InstallException.Code.MISSING_PROPERTY);
                }
                InstallableChooser chooser = Procedures.requireInstallableChooser(runner);
                MiuiRom.Specie romSpecie = specie.toBranch(installable.getBranch());
                String id = idBySpecie(romSpecie == null ? specie : romSpecie);
                chooser.add(id, installable);
                Procedures.setInstallable(runner, installable);
            }
        };
    }

    public static RInstall allLatestOta(Set<MiuiRom.Specie> speciesToSearch) {
        RInstall[] procedures = new RInstall[speciesToSearch.size()];
        int i = 0;
        for (MiuiRom.Specie specie : speciesToSearch) {
            procedures[i++] = fetchOnlyIfDeviceLockedOrNoFastboot(specie, findInstallWay(specie));
        }
        return RNode.skipOnException(procedures);
    }

    public static RInstall validatePkgRom(ProcedureRunner runner) throws InstallException {
        Device device = Procedures.requireDevice(runner);
        List<String> logStringList = new ArrayList<>();
        runner.setContext(ROM_LOG_SB, logStringList);
        Set<MiuiRom.Specie> speciesToSearch = MiuiRom.Specie.listToSearchSpecies(SettingsUtils.getRegion(), device.getDeviceProperties().getCodename(false));
        RInstall[] procedures = new RInstall[speciesToSearch.size()];
        int i = 0;
        for (MiuiRom.Specie specie : speciesToSearch) {
            procedures[i++] = otaPkgRom(specie, logStringList);
        }
        return RNode.sequence(new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Log.info("Validating pacakge rom: " + runner.getContext(GenericFetch.FILE_MD5));
                runner.text(LRes.VALIDATING_PKG_ROM);
            }
        }, RNode.fallback(RNode.fallback(procedures), new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                List logServer = (List) runner.getContext(ROM_LOG_SB);
                String output;
                if (logServer == null) {
                    output = "Missing server log";
                } else {
                    StringBuilder builder = new StringBuilder();
                    int i = 0;
                    for (Object o : logServer) {
                        builder.append(++i).append(") ").append(o.toString()).append('\n');
                    }
                    output = builder.toString();
                }
                logServer.clear();
                GenericInstall.showUserAndRestart(LRes.ROM_INSTALL_NOT_ALLOWED_EXP.toString(output), true).run(runner);
            }
        }));
    }

    public static RInstall otaPkgRom(MiuiRom.Specie specie) {
        return otaPkgRom(specie, null);
    }

    public static RInstall otaPkgRom(MiuiRom.Specie specie, List<String> showLog) {
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                InstallableChooser chooser = Procedures.requireInstallableChooser(runner);
                String md5 = (String) runner.requireContext(GenericFetch.FILE_MD5);
                Device device = Procedures.requireDevice(runner);
                Log.info("Starting ota request with pkg: " + md5);
                DeviceRequestParams params;
                try {
                    params = DeviceRequestParams.readFromDevice(device, true);
                    params.setSpecie(specie);
                } catch (AdbException e) {
                    throw new InstallException(e);
                }
                params.setPkg(md5);
                HashMap<MiuiRom.Kind, MiuiZipRom> rom;
                runner.text(LRes.REQUEST_OTA_INSTALLATION_TOKEN.toString(specie.toHuman()));
                try {
                    rom = MiuiRomOta.otaV3_request(params, showLog != null);
                } catch (XiaomiProcedureException e) {
                    if (showLog != null && XiaomiProcedureException.ExceptionCode.NOT_ALLOWED.equals(e.getCode())) {
                        String msg = e.getAdditional();
                        runner.text(LRes.ROM_INSTALL_NOT_ALLOWED.toString(msg));
                        showLog.add(msg);
                    }
                    throw new InstallException(e);
                } catch (CustomHttpException e) {
                    throw new InstallException(e);
                }
                Installable installable = rom.get(MiuiRom.Kind.PACKAGE);
                Log.info("Found pkg rom: " + installable);
                if (installable == null) {
                    throw new InstallException("Failed to validate rom pkg. Xiaomi server doesn't allow installation of this rom with locked bootloader", InstallException.Code.CANNOT_INSTALL);
                }
                String id = idBySpecie(specie);
                chooser.add(id, installable);
                Procedures.setInstallable(runner, installable);
            }
        };
    }

    private static RInstall fetchOnlyIfNoFastboot(MiuiRom.Specie specie, RInstall toRun) {
        return RNode.fallback(new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                InstallableChooser chooser = Procedures.requireInstallableChooser(runner);
                String id = idBySpecie(specie);
                Installable installable = chooser.getByType(id, Installable.Type.FASTBOOT);
                if (installable == null) {
                    throw new InstallException("You should not see this error, please report with a feedback", InstallException.Code.INTERNAL_ERROR, "FetchOnlyIfNoFastboot internal dummy");
                }
            }
        }, toRun);
    }

    private static RInstall fetchOnlyIfDeviceLockedOrNoFastboot(MiuiRom.Specie specie, RInstall toRun) {
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                if (UnlockStatus.UNLOCKED.equals(device.getAnswers().getUnlockStatus())) {
                    Log.info("The device is unlocked, fetching only if no fastboot rom available");
                    fetchOnlyIfNoFastboot(specie, toRun).run(runner);
                } else {
                    toRun.run(runner);
                }
            }
        };
    }

    public static RInstall otaLatestRecovery(MiuiRom.Specie specie) {
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                MiuiRom.Specie sp = specie;
                Log.info("Looking for latest recovery rom of specie :" + specie);
                InstallableChooser chooser = Procedures.requireInstallableChooser(runner);
                Device device = Procedures.requireDevice(runner);
                DeviceRequestParams params;
                try {
                    params = DeviceRequestParams.readFromDevice(device, true);
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
                params.setZone(specie.getZone());
                String id = idBySpecie(sp);
                Installable installable = chooser.getByType(id, Installable.Type.RECOVERY);
                MiuiVersion version = null;
                if (installable != null) {
                    if (installable.hasInstallToken()) {
                        Procedures.setInstallable(runner, installable);
                        return;
                    }
                    version = installable.getMiuiVersion();
                }
                if (version == null) {
                    installable = chooser.getByType(id, Installable.Type.FASTBOOT);
                    if (installable != null) {
                        version = installable.getMiuiVersion();
                    }
                }
                if (version == null) {
                    Log.warn("No version available to request ota, using apis to get latest rom");
                    findLatestRecovery(sp).run(runner);
                    installable = Procedures.requireInstallable(runner);
                    version = installable.getMiuiVersion();
                }
                if (version == null) {
                    throw new InstallException("Failed to create romOta request: null version", InstallException.Code.INFO_RETRIVE_FAILED);
                }
                params.setVersion(version);
                String md5 = (String) runner.consumeContext(SRF_MD5);
                if (md5 != null) {
                    params.setPkg(md5);
                }
                HashMap<MiuiRom.Kind, MiuiZipRom> rom;
                Log.info("Performing latest ota search request");
                runner.text(LRes.SEARCHING_LATEST_OTA_ROM.toString(params.getSpecie().toHuman()));
                try {
                    rom = MiuiRomOta.otaV3_request(params);
                } catch (XiaomiProcedureException e) {
                    throw new InstallException(e);
                } catch (CustomHttpException e) {
                    throw new InstallException(e);
                }
                Log.info("Found latest possible roms number: " + rom.size());
                installable = rom.get(MiuiRom.Kind.PACKAGE);
                if (installable == null) {
                    installable = rom.get(MiuiRom.Kind.LATEST);
                }
                if (installable == null) {
                    installable = rom.get(MiuiRom.Kind.CURRENT);
                }
                if (installable == null) {
                    installable = rom.get(MiuiRom.Kind.INCREMENTAL);
                }
                Log.info("Preffered latest rom choosen: " + installable);
                if (installable == null) {
                    throw new InstallException("Ota response doesn't contain an installable rom data", InstallException.Code.MISSING_PROPERTY);
                }
                Procedures.setInstallable(runner, installable);
            }
        };
    }

    public static RInstall findLatestRecovery(MiuiRom.Specie specie) {
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Log.info("Looking for latest recovery rom version using apis");
                MiuiRom.Specie sp = specie;
                InstallableChooser chooser = Procedures.requireInstallableChooser(runner);
                Device device = Procedures.requireDevice(runner);
                DeviceRequestParams params;
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
                String id = idBySpecie(specie);
                Installable rom = chooser.getByType(id, Installable.Type.RECOVERY);
                try {
                    if (rom == null) {
                        try {
                            rom = MiuiRomOta.latestRecovery_request(params);
                        } catch (XiaomiProcedureException e) {
                            throw new InstallException(e);
                        }
                        chooser.add(id, rom);
                    }
                } catch (CustomHttpException e) {
                    throw new InstallException(e);
                }
                Procedures.setInstallable(runner, rom);
            }
        };
    }

    public static RInstall createValidatedZipInstall(ProcedureRunner runner) throws InstallException {
        return RNode.sequence(new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Installable installable = Procedures.requireInstallable(runner);
                File file = Procedures.getInstallableFile(installable);
                Log.info("Trying to validate file for stock recovery installation: " + file);
                runner.setContext(GenericFetch.SELECTED_FILE, file);
            }
        }, GenericFetch.computeMD5File(), validatePkgRom(runner));
    }

    public static RInstall findBestRecoverRom() {
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                RequestParams params;
                try {
                    params = DeviceRequestParams.readFromDevice(device, true);
                } catch (AdbException e) {
                    throw new InstallException(e);
                }
                basicOta(params.getSpecie()).run(runner);
            }
        };
    }
}
