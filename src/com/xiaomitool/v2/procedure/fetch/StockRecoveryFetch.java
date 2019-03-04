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
import com.xiaomitool.v2.rom.MiuiZipRom;
import com.xiaomitool.v2.rom.chooser.InstallableChooser;
import com.xiaomitool.v2.utility.utils.StrUtils;
import com.xiaomitool.v2.xiaomi.XiaomiProcedureException;
import com.xiaomitool.v2.xiaomi.miuithings.DeviceRequestParams;
import com.xiaomitool.v2.xiaomi.miuithings.MiuiVersion;
import com.xiaomitool.v2.xiaomi.miuithings.UnlockStatus;
import com.xiaomitool.v2.xiaomi.romota.MiuiRomOta;

import java.io.File;
import java.util.HashMap;


import static com.xiaomitool.v2.rom.chooser.InstallableChooser.idBySpecie;

public class StockRecoveryFetch {

    public static RInstall findInstallWay(MiuiRom.Specie specie){
        return RNode.fallback(basicOta(specie), RNode.sequence(otaLatestRecovery(specie), new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Installable installable = Procedures.requireInstallable(runner);
                if (StrUtils.isNullOrEmpty(installable.getMd5())){
                    throw new InstallException("Latest ota package has no md5", InstallException.Code.INFO_RETRIVE_FAILED, false);
                }
                runner.setContext(GenericFetch.FILE_MD5, installable.getMd5());
            }
        }, otaPkgRom(specie)), Procedures.doNothing());
    }


    public static RInstall basicOta(MiuiRom.Specie specie){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                DeviceRequestParams params = null;
                try {
                    params = DeviceRequestParams.readFromDevice(device,true);
                    Log.debug("SERIAL: " + params.getSerialNumber());
                } catch (AdbException e) {
                    throw new InstallException(e);
                }
                params.setSpecie(specie);
                MiuiZipRom installable;
                HashMap<MiuiRom.Kind, MiuiZipRom> rom ;
                runner.text(LRes.SEARCHING_LATEST_RECOVERY_ROM.toString(params.getSpecie().toString()));
                try {
                    rom = MiuiRomOta.otaV3_request(params);
                } catch (XiaomiProcedureException e) {
                    throw new InstallException(e);
                } catch (CustomHttpException e) {
                    throw new InstallException(e);
                }
                installable = rom.get(MiuiRom.Kind.PACKAGE);
                if (installable == null){
                    installable = rom.get(MiuiRom.Kind.LATEST);
                }
                if (installable == null){
                    installable = rom.get(MiuiRom.Kind.CURRENT);
                }
                if (installable == null){
                    installable = rom.get(MiuiRom.Kind.INCREMENTAL);
                }
                if (installable == null || (!installable.hasInstallToken() && !UnlockStatus.UNLOCKED.equals(Procedures.requireDevice(runner).getAnswers().getUnlockStatus()))){
                    throw new InstallException("Ota response doesn't contain an installable rom data", InstallException.Code.MISSING_PROPERTY, true);
                }
                InstallableChooser chooser = Procedures.requireInstallableChooser(runner);
                MiuiRom.Specie romSpecie = MiuiRom.Specie.fromStringBranch(specie.getSuffix(), installable.getBranch());
                String id = idBySpecie(romSpecie == null ? specie : romSpecie);
                chooser.add(id, installable);
                Procedures.setInstallable(runner,installable);

            }
        };
    }

    public static RInstall allLatestOta(){
        return RNode.setSkipOnException(RNode.sequence(
                fetchOnlyIfDeviceLockedOrNoFastboot(MiuiRom.Specie.CHINA_STABLE, findInstallWay(MiuiRom.Specie.CHINA_STABLE)),
                fetchOnlyIfDeviceLockedOrNoFastboot(MiuiRom.Specie.CHINA_DEVELOPER,findInstallWay(MiuiRom.Specie.CHINA_DEVELOPER)),
                fetchOnlyIfDeviceLockedOrNoFastboot(MiuiRom.Specie.GLOBAL_STABLE,findInstallWay(MiuiRom.Specie.GLOBAL_STABLE)),
                fetchOnlyIfDeviceLockedOrNoFastboot(MiuiRom.Specie.GLOBAL_DEVELOPER,findInstallWay(MiuiRom.Specie.GLOBAL_DEVELOPER))
        ));
    }

    public static RInstall validatePkgRom(){
        return RNode.sequence(new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                runner.text(LRes.VALIDATING_PKG_ROM);
            }
        },RNode.fallback(otaPkgRom(MiuiRom.Specie.GLOBAL_STABLE),otaPkgRom(MiuiRom.Specie.GLOBAL_DEVELOPER), otaPkgRom(MiuiRom.Specie.CHINA_STABLE), otaPkgRom(MiuiRom.Specie.CHINA_DEVELOPER)));
    }

    public static RInstall otaPkgRom(MiuiRom.Specie specie){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                InstallableChooser chooser = Procedures.requireInstallableChooser(runner);
                String md5 = (String) runner.requireContext(GenericFetch.FILE_MD5);
                Device device = Procedures.requireDevice(runner);
                DeviceRequestParams params;
                try {
                    params = DeviceRequestParams.readFromDevice(device, true);
                    params.setSpecie(specie);
                } catch (AdbException e) {
                    throw new InstallException(e);
                }
                params.setPkg(md5);
                HashMap<MiuiRom.Kind, MiuiZipRom> rom ;
                try {
                    rom = MiuiRomOta.otaV3_request(params);
                } catch (XiaomiProcedureException e) {
                    throw new InstallException(e);
                } catch (CustomHttpException e) {
                    throw new InstallException(e);
                }
                Installable installable = rom.get(MiuiRom.Kind.PACKAGE);
                if (installable == null){
                    throw new InstallException("Failed to validate rom pkg. Xiaomi server doesn't allow installation", InstallException.Code.MISSING_PROPERTY, true);
                }
                String id = idBySpecie(specie);
                chooser.add(id, installable);
                Procedures.setInstallable(runner,installable);

            }
        };

    }

    private static final String SRF_MD5 = "SFR_md5_key";

    private static RInstall fetchOnlyIfNoFastboot(MiuiRom.Specie specie, RInstall toRun){
        return RNode.fallback(new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                InstallableChooser chooser = Procedures.requireInstallableChooser(runner);
                String id = idBySpecie(specie);
                Installable installable = chooser.getByType(id, Installable.Type.FASTBOOT);
                if (installable == null){
                    throw new InstallException("", InstallException.Code.INTERNAL_ERROR, false);
                }
            }
        }, toRun);
    }

    private static RInstall fetchOnlyIfDeviceLockedOrNoFastboot(MiuiRom.Specie specie, RInstall toRun){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Device device = Procedures.requireDevice(runner);
                if (UnlockStatus.UNLOCKED.equals(device.getAnswers().getUnlockStatus())){
                    fetchOnlyIfNoFastboot(specie, toRun).run(runner);
                } else {
                    toRun.run(runner);
                }
            }
        };
    }

    public static RInstall otaLatestRecovery(MiuiRom.Specie specie){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                //new Exception().printStackTrace();
                MiuiRom.Specie sp = specie;

                InstallableChooser chooser = Procedures.requireInstallableChooser(runner);
                Device device = Procedures.requireDevice(runner);
                DeviceRequestParams params;
                try {
                    params = DeviceRequestParams.readFromDevice(device, true);
                } catch (AdbException e) {
                    throw new InstallException(e);
                }

                if (sp == null){
                    sp = params.getSpecie();
                    if (sp == null){
                        throw new InstallException("Failed to obtain device branch and region: null specie", InstallException.Code.MISSING_PROPERTY, false);
                    }
                } else {
                    params.setSpecie(sp);
                }
                params.setZone(specie.getZone());
                String id = idBySpecie(sp);
                Installable installable = chooser.getByType(id, Installable.Type.RECOVERY);
                MiuiVersion version = null;
                if (installable != null){
                    if (installable.hasInstallToken()){
                        Procedures.setInstallable(runner,installable);
                        return;
                    }
                    version = installable.getMiuiVersion();
                }
                if (version == null){
                     installable = chooser.getByType(id, Installable.Type.FASTBOOT);
                     if(installable != null){
                         version = installable.getMiuiVersion();
                     }
                }
                if (version == null){
                    findLatestRecovery(sp).run(runner);
                    installable = Procedures.requireInstallable(runner);
                    version = installable.getMiuiVersion();
                }
                if (version == null){
                    throw new InstallException("Failed to create romOta request: null version", InstallException.Code.INFO_RETRIVE_FAILED, true);
                }
                params.setVersion(version);
                String md5 = (String) runner.consumeContext(SRF_MD5);
                if (md5 != null){
                    params.setPkg(md5);
                }
                HashMap<MiuiRom.Kind, MiuiZipRom> rom ;
                runner.text(LRes.SEARCHING_LATEST_RECOVERY_ROM.toString(params.getSpecie().toString()));
                        try {
                            rom = MiuiRomOta.otaV3_request(params);
                        } catch (XiaomiProcedureException e) {
                            throw new InstallException(e);
                        } catch (CustomHttpException e) {
                            throw new InstallException(e);
                        }
                installable = rom.get(MiuiRom.Kind.PACKAGE);
                if (installable == null){
                    installable = rom.get(MiuiRom.Kind.LATEST);
                }
                if (installable == null){
                    installable = rom.get(MiuiRom.Kind.CURRENT);
                }
                if (installable == null){
                    installable = rom.get(MiuiRom.Kind.INCREMENTAL);
                }
                if (installable == null){
                    throw new InstallException("Ota response doesn't contain an installable rom data", InstallException.Code.MISSING_PROPERTY, true);
                }
                if (!installable.hasInstallToken() && !UnlockStatus.UNLOCKED.equals(Procedures.requireDevice(runner).getAnswers().getUnlockStatus()) && installable.getMd5() != null && !installable.getMd5().isEmpty()){
                    runner.setContext(SRF_MD5, installable.getMd5());
                    this.run(runner);
                    return;
                }


                chooser.add(id, installable);
                Procedures.setInstallable(runner,installable);


            }
        };
    }


    public static RInstall findLatestRecovery(MiuiRom.Specie specie){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                MiuiRom.Specie sp = specie;
                InstallableChooser chooser = Procedures.requireInstallableChooser(runner);
                Device device = Procedures.requireDevice(runner);
                DeviceRequestParams params;
                try {
                    params = DeviceRequestParams.readFromDevice(device);
                } catch (AdbException e) {
                    throw new InstallException(e);
                }
                if (sp == null){
                    sp = params.getSpecie();
                    if (sp == null){
                        throw new InstallException("Failed to obtain device branch and region: null specie", InstallException.Code.MISSING_PROPERTY, false);
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
                Procedures.setInstallable(runner,rom);
            }
        };
    }

    public static RInstall createValidatedZipInstall() {
        return RNode.sequence(new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Installable installable = Procedures.requireInstallable(runner);
                File file = Procedures.getInstallableFile(installable);
                runner.setContext(GenericFetch.SELECTED_FILE, file);
            }
        }, GenericFetch.computeMD5File(), validatePkgRom());
    }


}
