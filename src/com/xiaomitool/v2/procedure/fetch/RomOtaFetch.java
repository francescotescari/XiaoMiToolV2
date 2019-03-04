package com.xiaomitool.v2.procedure.fetch;

import com.xiaomitool.v2.inet.CustomHttpException;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.ProcedureRunner;
import com.xiaomitool.v2.procedure.Procedures;
import com.xiaomitool.v2.procedure.RInstall;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.rom.MiuiRom;
import com.xiaomitool.v2.rom.MiuiZipRom;
import com.xiaomitool.v2.xiaomi.XiaomiProcedureException;
import com.xiaomitool.v2.xiaomi.miuithings.MiuiVersion;
import com.xiaomitool.v2.xiaomi.romota.MiuiRomOta;
import com.xiaomitool.v2.xiaomi.miuithings.RequestParams;

import java.util.HashMap;

public class RomOtaFetch {

    public static RInstall fetchMiuiOta(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner procedureRunner) throws InstallException, InterruptedException {
                RequestParams requestParams = (RequestParams) procedureRunner.requireContext(Procedures.REQUEST_PARAMS);
                HashMap<MiuiRom.Kind, MiuiZipRom> roms;
                try {
                    roms = MiuiRomOta.otaV3_request(requestParams);
                } catch (XiaomiProcedureException e) {
                    throw new InstallException("Failed to obatain ota roms: "+e.getMessage(), InstallException.Code.XIAOMI_EXCEPTION, true);
                } catch (CustomHttpException e) {
                    throw new InstallException("Internet exception: "+e.getMessage(), InstallException.Code.CONNECTION_ERROR, true);
                }
                if (roms == null){
                    throw new InstallException("Failed to fetch roms via ota: null map", InstallException.Code.ROM_OTA_ERROR, true);
                }
                procedureRunner.setContext(Procedures.REQUEST_RESULT, roms);
            }
        };
    }




    public static RInstall chooseChineseSameBranchDifferentVersion(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner procedureRunner) throws InstallException, InterruptedException {
                RequestParams unmodifiableRequestParams = (RequestParams) procedureRunner.requireContext(Procedures.REQUEST_PARAMS);
                RequestParams requestParams;
                try {
                    requestParams = unmodifiableRequestParams.clone();
                } catch (CloneNotSupportedException e) {
                    throw new InstallException("Cannot clone request params: "+e.getMessage(), InstallException.Code.INTERNAL_ERROR, false);
                }
                requestParams.setSpecie(requestParams.getSpecie().toChina());
                MiuiRom latestChinese = null;
                MiuiVersion version = null;
                String validate = null;
                MiuiZipRom rom = null;
                int trial = 0;
                while (validate == null) {
                    version = null;
                    while (version == null) {
                        ++trial;
                        if (trial == 3) {
                            requestParams.setSpecie(requestParams.getSpecie().toOppositeBranch());
                        }
                        if (trial >= 5) {
                            throw new InstallException("Failed to get latest Chinese rom: both stable and developer version not found valid", InstallException.Code.ROM_OTA_ERROR, true);
                        }
                        procedureRunner.text(LRes.SEARCHING_LATEST_ROM.toString(requestParams.getSpecie().toString()));
                        try {
                            switch (trial % 2) {
                                case 1:
                                    latestChinese = MiuiRomOta.latestRecovery_request(requestParams);
                                    break;
                                case 0:
                                    latestChinese = MiuiRomOta.latestFastboot_request(requestParams);
                                    break;
                            }
                        } catch (XiaomiProcedureException e) {
                            Log.error("Failed to get latest Chinese rom: " + e.getMessage());
                        } catch (CustomHttpException e) {
                            throw new InstallException(e);
                        }
                        if (latestChinese != null) {
                            version = latestChinese.getMiuiVersion();
                        }
                    }
                    requestParams.setVersion(version);
                    HashMap<MiuiRom.Kind, MiuiZipRom> otaRom = null;
                    procedureRunner.text(LRes.QUERYING_OTA_ROM.toString(requestParams.getSpecie().toString()));
                    try {
                        otaRom = MiuiRomOta.otaV3_request(requestParams);
                    } catch (XiaomiProcedureException e) {
                            Log.error("Failed to get valid ota response for this rom: "+e.getMessage());
                            continue;
                    } catch (CustomHttpException e) {
                        throw new InstallException(e);
                    }
                    if (otaRom == null){
                        continue;
                    }
                    rom = otaRom.get(MiuiRom.Kind.CURRENT);
                    if (rom == null || !rom.hasInstallToken()){
                        rom = otaRom.get(MiuiRom.Kind.LATEST);
                    }
                    if (rom == null || !rom.hasInstallToken()){
                        rom = otaRom.get(MiuiRom.Kind.PACKAGE);
                    }
                    if (rom == null || !rom.hasInstallToken()){
                            continue;
                    }
                    validate = rom.getInstallToken();
                }
                try {
                    procedureRunner.text(LRes.ROM_SELECTED_ROM.toString(rom.getMiuiVersion().toString()+" - "+requestParams.getSpecie().toString()));
                } catch (Throwable t){
                    Log.warn("Failed to display installable rom: "+t.getMessage());
                }
                procedureRunner.setContext(Procedures.INSTALLABLE, rom);

                }

        };
    }



}
