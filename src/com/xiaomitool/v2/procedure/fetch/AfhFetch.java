package com.xiaomitool.v2.procedure.fetch;

import com.xiaomitool.v2.adb.device.DeviceProperties;
import com.xiaomitool.v2.inet.CustomHttpException;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.ProcedureRunner;
import com.xiaomitool.v2.procedure.Procedures;
import com.xiaomitool.v2.procedure.RInstall;
import com.xiaomitool.v2.procedure.RNode;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.procedure.retrive.AfhRequest;
import com.xiaomitool.v2.rom.RomException;
import com.xiaomitool.v2.rom.TwrpFile;

import java.util.List;

public class AfhFetch {

    private static final String SELECTED_FID = "afh_selected_fid";
    private static final String SELECTED_DID = "afh_selected_did";
    private static final String DIR_CONTENT = "afh_dir_content";


    public static RInstall getFidDownloadUrl(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, InterruptedException {
                String fid = (String) runner.requireContext(SELECTED_FID);
                Log.info("Fetching download link for afh fid: "+fid);
                runner.text(LRes.FETCHING_DOWNLOAD_URL);
                String downloadUrl;
                try {
                     downloadUrl = AfhRequest.getDownloadLink(fid);
                } catch (CustomHttpException e) {
                    throw new InstallException(e);
                } catch (RomException e) {
                    throw new InstallException(e);
                }
                runner.setContext(Procedures.DOWNLOAD_URL, downloadUrl);
            }
        };
    }

    public static RInstall getAfhDirContent(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner procedureRunner) throws InstallException, InterruptedException {
                String did = (String) procedureRunner.requireContext(SELECTED_DID);
                Log.info("Fetching directory content for afh did: "+did);
                List<AfhRequest.AfhEntry> entries;
                try {
                    entries = AfhRequest.listDirFiles(did);
                } catch (CustomHttpException e) {
                    throw new InstallException(e);
                } catch (RomException e) {
                    throw new InstallException(e);
                }
                procedureRunner.setContext(DIR_CONTENT, entries);
            }
        };
    }

    public static RInstall findLatestTwrp(){
        return RNode.sequence(new RInstall() {
            @Override
            public void run(ProcedureRunner procedureRunner) throws InstallException, InterruptedException {
                procedureRunner.setContext(SELECTED_DID, "50678");
            }
        }, getAfhDirContent(), new RInstall() {
            @Override
            public void run(ProcedureRunner procedureRunner) throws InstallException, InterruptedException {
                String deviceName = Procedures.requireDeviceProperty(procedureRunner, DeviceProperties.CODENAME);
                deviceName = deviceName.toLowerCase();
                @SuppressWarnings("unchecked")
                List<AfhRequest.AfhEntry> entries = (List<AfhRequest.AfhEntry>) procedureRunner.requireContext(DIR_CONTENT);
                boolean isTwrp = false;
                AfhRequest.AfhEntry choosenOne = null;
                for (AfhRequest.AfhEntry entry : entries) {
                    String name = entry.getFilename().toLowerCase();
                    if (name.contains(deviceName)) {
                        boolean isThisTwrp = name.contains("twrp");
                        if (isThisTwrp && !isTwrp) {
                            choosenOne = entry;
                            isTwrp = true;
                            continue;
                        } else if (choosenOne == null) {
                            choosenOne = entry;
                            continue;
                        }
                        if (isThisTwrp == isTwrp && entry.getUploadDate() > choosenOne.getUploadDate()) {
                            choosenOne = entry;
                        }
                    }
                }
                if (choosenOne == null) {
                    throw new InstallException(new RomException("Failed to find a valid twrp from afh database"));
                }
                procedureRunner.setContext(SELECTED_FID, choosenOne.getFid());
            }
        }, getFidDownloadUrl(), new RInstall() {
            @Override
            public void run(ProcedureRunner procedureRunner) throws InstallException, InterruptedException {
                String downloadUrl = (String) procedureRunner.requireContext(Procedures.DOWNLOAD_URL);
                TwrpFile twrpFile = new TwrpFile(downloadUrl, Procedures.requireDeviceProperty(procedureRunner, DeviceProperties.CODENAME));
                procedureRunner.setContext(Procedures.INSTALLABLE, twrpFile);
            }
        });
    }
}
