package com.xiaomitool.v2.engine;

import com.xiaomitool.v2.gui.GuiUtils;
import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.gui.controller.LoginController;
import com.xiaomitool.v2.language.Lang;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.utility.utils.MutexUtils;
import com.xiaomitool.v2.utility.utils.SettingsUtils;
import com.xiaomitool.v2.xiaomi.XiaomiKeystore;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ToolManager {
    public static String TOOL_VERSION = "8.9.24";
    public static String URL_DONATION = "https://xiaomitool.page.link/donate";
    public static String TOOL_VERSION_EX = "unbrick only";
    public static String XMT_HOST = "https://www.xiaomitool.com/V2/";
    public static String URL_UPDATE = XMT_HOST+"update.php";
    public static String URL_LATEST = XMT_HOST+"latest";
    private static boolean exiting = false;


    private static List<Stage> activeStages = new ArrayList<>();
    public static void init(Stage primaryStage) throws Exception {
        boolean isSingleInstance = MutexUtils.lock();
        if (!isSingleInstance){
            ToolManager.exit(1);
            return;
        }
        SettingsUtils.load();
        Lang.loadSystemLanguage();
        GuiUtils.init();
        checkLoadSession();

        WindowManager.launchMain(primaryStage);
    }

    public static void showStage(Stage stage){
        if (stage == null){
            return;
        }
        activeStages.add(stage);
        stage.show();
    }
    public static void closeStage(Stage stage){
        if (stage == null){
            return;
        }
        activeStages.remove(stage);
        stage.close();
    }
    public static void exit(int code){
        if (exiting){
            return;
        }
        exiting = true;
        saveOptions();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                for (Stage stage : activeStages){
                    stage.close();
                }
                MutexUtils.unlock();
                Log.closeLogFile();
                ToolManager.closeStage(WindowManager.mainWindow());
                System.exit(code);
            }
        });

    }
    private static void saveOptions(){
        checkSaveSession();
        SettingsUtils.save();
    }
    private static void checkSaveSession(){
        String pcId = XiaomiKeystore.getInstance().getPcId();
        if (pcId != null){
            SettingsUtils.saveOpt(SettingsUtils.PC_ID, pcId);
        }
        String saveSession = SettingsUtils.getOpt(SettingsUtils.PREF_SAVE_SESSION);
        if (!"true".equals(saveSession)){
            SettingsUtils.saveOpt(SettingsUtils.SESSION_TOKEN,"null");
            return;
        }
        String json = XiaomiKeystore.getInstance().getJson();
        if (json == null){
            SettingsUtils.saveOpt(SettingsUtils.SESSION_TOKEN,"null");
            return;
        }
        SettingsUtils.saveOptEncrpyted(SettingsUtils.SESSION_TOKEN,json);
    }
    private static void checkLoadSession(){
        String pcId = SettingsUtils.getOpt(SettingsUtils.PC_ID);
        if (pcId != null){
            XiaomiKeystore.getInstance().setDeviceId(pcId);
        }
        String saveSession = SettingsUtils.getOpt(SettingsUtils.PREF_SAVE_SESSION);
        if (!"true".equals(saveSession)){
            return;
        }

        String json = SettingsUtils.getOptDecrypted(SettingsUtils.SESSION_TOKEN);
        if (json == null){
            Log.warn("Failed to load old session token");
            return;
        }
        Log.debug("Old session token: "+json);
        try {
            JSONObject object = new JSONObject(json);
            XiaomiKeystore.getInstance().setCredentials(object);
            Log.debug(XiaomiKeystore.getInstance().getUserId());

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    LoginController.setLoginNumber(XiaomiKeystore.getInstance().getUserId());
                }
            });
        } catch (JSONException e){
            Log.warn("Failed to parse old session token: "+e.getMessage());
        }
    }
}
