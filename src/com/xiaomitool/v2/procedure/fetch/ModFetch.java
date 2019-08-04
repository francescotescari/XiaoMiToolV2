package com.xiaomitool.v2.procedure.fetch;

import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.gui.drawable.DrawableManager;
import com.xiaomitool.v2.gui.visual.ChooserPane;
import com.xiaomitool.v2.inet.CustomHttpException;
import com.xiaomitool.v2.inet.CustomHttpRequest;
import com.xiaomitool.v2.inet.EasyHttp;
import com.xiaomitool.v2.inet.EasyResponse;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.*;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.rom.ApkFileInstallable;
import com.xiaomitool.v2.rom.MultiInstallable;
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
                        throw new InstallException("Failed to get latest stable magisk data from static url", InstallException.Code.INFO_RETRIVE_FAILED, new Exception("Returned status code: "+response.getCode()));
                    }
                    Log.info("Magisk latest response: "+response.getBody());
                    JSONObject jsonObject = new JSONObject(response.getBody());
                    JSONObject magiskObj = jsonObject.getJSONObject("magisk");
                    JSONObject appObj = jsonObject.getJSONObject("app");
                    String mUrl = magiskObj.getString("link");
                    String mVersion = magiskObj.getString("version");
                    String aUrl = appObj.getString("link");
                    String aVersion = appObj.getString("version");
                    ZipRom mZip = new ZipRom(mUrl) {
                        @Override
                        public String getTitle() {
                            return LRes.MAGISK_ROOT.toString();
                        }

                        @Override
                        public String getText() {
                            return LRes.MAGISK_AUTO_DOWNLOAD.toString(mVersion);
                        }

                        @Override
                        public Image getIcon() {
                            return DrawableManager.getResourceImage("magiskround.png");
                        }

                    };
                    mZip.setDownloadFilename("magisk_"+mVersion+".zip");
                    ApkFileInstallable app = new ApkFileInstallable("MagiskManager "+aVersion, aUrl, Device.Status.RECOVERY){
                        @Override
                        public RInstall getInstallProcedure(){
                            return RNode.sequence(new RInstall() {
                                @Override
                                public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                                    runner.text(LRes.MAGISK_INSTALL_MANAGER.toString());
                                }
                            }, super.getInstallProcedure());
                        }
                    };
                    app.setPackageName("com.topjohnwu.magisk");
                    app.setDownloadFilename("magiskMg_"+aVersion+".apk");
                    MultiInstallable multiInstallable = new MultiInstallable(mZip, app) {
                        @Override
                        public String getTitle() {
                            return mZip.getTitle();
                        }

                        @Override
                        public String getText() {
                            return mZip.getText();
                        }

                        @Override
                        public Image getIcon() {
                            return mZip.getIcon();
                        }



                    };
                    Log.debug(mZip.getDownloadUrl());
                    Procedures.setInstallable(runner,multiInstallable);
                } catch (CustomHttpException e) {
                    throw new InstallException(e);
                } catch (JSONException e){
                    throw new InstallException("Failed to parse latest stable magisk data", InstallException.Code.INFO_RETRIVE_FAILED, e);
                }
            }
        };
    }
}
