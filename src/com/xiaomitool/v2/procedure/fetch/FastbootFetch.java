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
import com.xiaomitool.v2.xiaomi.XiaomiProcedureException;
import com.xiaomitool.v2.xiaomi.miuithings.DeviceRequestParams;
import com.xiaomitool.v2.xiaomi.miuithings.RequestParams;
import com.xiaomitool.v2.xiaomi.romota.MiuiRomOta;

import static com.xiaomitool.v2.rom.chooser.InstallableChooser.idBySpecie;

public class FastbootFetch {


    public static RInstall findLatestFastboot(MiuiRom.Specie specie){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Log.info("Searching latest fastboot rom: specie: "+specie);
                MiuiRom.Specie sp = specie;
                InstallableChooser chooser = Procedures.requireInstallableChooser(runner);
                Device device = Procedures.requireDevice(runner);
                RequestParams params;
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
                String id = idBySpecie(sp);
                Installable rom = chooser.getByType(id, Installable.Type.FASTBOOT);
                try {
                    if (rom == null) {
                        try {
                            runner.text(LRes.SEARCHING_LATEST_FASTBOOT.toString(params.getSpecie().toString()));
                            rom = MiuiRomOta.latestFastboot_request(params);
                        } catch (XiaomiProcedureException e) {
                            try {
                                Log.info("Using fastboot fetch method 2");
                                rom = MiuiRomOta.latestFastboot2_request(params);
                            } catch (XiaomiProcedureException e2) {
                                throw new InstallException(e2);
                            }
                        }
                        Log.info("Fastboot rom for specie "+specie+" found: "+rom);
                        chooser.add(id, rom);
                    }
                } catch (CustomHttpException e) {
                    throw new InstallException(e);
                }
                Procedures.setInstallable(runner,rom);

            }
        };

    }

    public static RInstall findAllLatestFastboot(){
        return RNode.setSkipOnException(RNode.sequence(findLatestFastboot(MiuiRom.Specie.CHINA_STABLE), findLatestFastboot(MiuiRom.Specie.CHINA_DEVELOPER), findLatestFastboot(MiuiRom.Specie.GLOBAL_STABLE), findLatestFastboot(MiuiRom.Specie.GLOBAL_DEVELOPER)));
    }
}
