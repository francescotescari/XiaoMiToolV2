package com.xiaomitool.v2.procedure.fetch;

import com.xiaomitool.v2.gui.drawable.DrawableManager;
import com.xiaomitool.v2.gui.visual.ChooserPane;
import com.xiaomitool.v2.inet.CustomHttpException;
import com.xiaomitool.v2.inet.CustomHttpRequest;
import com.xiaomitool.v2.inet.EasyHttp;
import com.xiaomitool.v2.inet.EasyResponse;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.ProcedureRunner;
import com.xiaomitool.v2.procedure.Procedures;
import com.xiaomitool.v2.procedure.RInstall;
import com.xiaomitool.v2.procedure.RMessage;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.rom.ZipRom;
import javafx.scene.image.Image;
import org.json.JSONException;
import org.json.JSONObject;

public class ModFetch {
    private static final String MAGISK_UPDATE_URL = "https://raw.githubusercontent.com/topjohnwu/magisk_files/master/stable.json";
    public static RInstall fetchMagiskStable(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                try {
                    Log.info("Fetching latest magisk stable");
                    runner.text(LRes.SEARCHING_LATEST_MAGISK);
                    EasyResponse response = EasyHttp.get(MAGISK_UPDATE_URL);
                    if (!response.isAllRight()){
                        throw new InstallException("Failed to get latest stable magisk data from static url", InstallException.Code.INFO_RETRIVE_FAILED, true);
                    }
                    Log.info("Magisk latest response: "+response.getBody());
                    JSONObject jsonObject = new JSONObject(response.getBody());
                    JSONObject magiskObj = jsonObject.getJSONObject("magisk");
                    String url = magiskObj.getString("link");
                    String version = magiskObj.getString("version");
                    ZipRom installable = new ZipRom(url) {
                        @Override
                        public String getTitle() {
                            return LRes.MAGISK_ROOT.toString();
                        }

                        @Override
                        public String getText() {
                            return LRes.MAGISK_AUTO_DOWNLOAD.toString(version);
                        }

                        @Override
                        public Image getIcon() {
                            return DrawableManager.getResourceImage("magiskround.png");
                        }

                    };
                    installable.setDownloadFilename("magisk_"+version+".zip");
                    Log.debug(installable.getDownloadUrl());
                    Procedures.setInstallable(runner,installable);
                } catch (CustomHttpException e) {
                    throw new InstallException(e);
                } catch (JSONException e){
                    throw new InstallException("Failed to parse latest stable magisk data", InstallException.Code.INFO_RETRIVE_FAILED, false);
                }
            }
        };
    }
}
