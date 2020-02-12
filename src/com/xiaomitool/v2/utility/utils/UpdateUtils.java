package com.xiaomitool.v2.utility.utils;

import com.xiaomitool.v2.engine.actions.ActionsStatic;
import com.xiaomitool.v2.inet.EasyHttp;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.process.ProcessRunner;
import com.xiaomitool.v2.resources.ResourcesConst;
import com.xiaomitool.v2.resources.ResourcesManager;
import org.json.JSONObject;
import sun.net.ResourceManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashSet;
import java.util.Set;

public class UpdateUtils {
    public enum UpdateStatus {
        UPDATED(0),
        QUICK_UPDATE(1),
        FULL_UPDATE(2),
        BLOCK(987654321);
        private final int code;
        private String quickUrl, latestVersion, latestFullVersion, quickSize;
        private UpdateStatus(int code){
            this.code = code;
        }

        public String getLatestFullVersion() {
            return latestFullVersion;
        }

        public String getLatestVersion() {
            return latestVersion;
        }

        public String getQuickUrl() {
            return quickUrl;
        }

        public String getQuickSize() {
            return quickSize;
        }

        public int getCode() {
            return code;
        }
    }
    private static final String BLOCK_VERSION = "0.0.0";

    public static UpdateStatus checkForUpdatesV2(String urlUpdateV2, String toolVersion, String requireHashedPCId) throws Exception {
        JSONObject jsonObject = new JSONObject(EasyHttp.get(urlUpdateV2+"?i="+requireHashedPCId+"&v="+toolVersion+"&p="+ ResourcesConst.getOSName()).getBody());
        String latestVersion = jsonObject.getString("lv");
        String latestFullVersion = jsonObject.getString("lf");
        String latestUrl = jsonObject.getString("lu");
        String latestSize = jsonObject.getString("ls");
        UpdateStatus updateStatus;
        if (StrUtils.compareVersion(latestVersion, BLOCK_VERSION) == 0){
            updateStatus = UpdateStatus.BLOCK;
        } else if (StrUtils.compareVersion(latestVersion, toolVersion) <= 0){
            updateStatus = UpdateStatus.UPDATED;
        } else if (StrUtils.compareVersion(latestFullVersion, toolVersion) > 0){
            updateStatus = UpdateStatus.FULL_UPDATE;
        } else {
            updateStatus = UpdateStatus.QUICK_UPDATE;
        }
        updateStatus.latestFullVersion = latestFullVersion;
        updateStatus.latestVersion = latestVersion;
        updateStatus.quickUrl = latestUrl;
        updateStatus.quickSize = latestSize;
        return updateStatus;
    }

    private static final String ALIVE_YEAH = "what_a_nice_time_to_be_alive";

    public static boolean checkIfAlive(Path java, Path filename) throws Exception{
            ProcessRunner runner = new ProcessRunner(java);
            runner.addArgument("-jar");
            runner.addArgument(filename.toString());
            runner.addArgument("--isalive");
            runner.runWait(10);
            String out = runner.getOutputString();
            return out != null && out.contains(ALIVE_YEAH);
    }
    public static boolean startUpdateProcess(Path java, Path filename, boolean update) throws Exception{
        ProcessRunner runner = new ProcessRunner(java);
        runner.addArgument("-jar");
        runner.addArgument(filename.toString());
        if (update) {
            runner.addArgument("--update_start");
        }
        return runner.start() != null;
    }

    public static boolean checkUpdateKillMe(String[] args) throws Exception{
        for (String arg : args){
            if ("--isalive".equals(arg)){
                printAlive();
                return true;
            } else if ("--update_start".equals(arg)){
                return doUpdate();
            }
        }
        removePendingUpdates();
        return false;
    }

    private static void removePendingUpdates(){
        /*Log.debug("Starting remove pending update thread");*/
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i<3; ++i);
                try {
                    Path currentJarDir = ResourcesManager.getCurrentJarDirPath();
                    Path pendingUpdate = currentJarDir.resolve(ActionsStatic.XMT_UPDATE_FILENAME);
                    /*Log.debug("Checking if update file "+pendingUpdate+" exists");*/
                    if (Files.exists(pendingUpdate)){
                        /*Log.debug("It exists");*/
                        Files.delete(pendingUpdate);
                    }
                    return;
                } catch (Throwable t){
                    /*Log.debug("Delete error: "+t.getMessage());*/
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {

                    }
                }
            }
        }).start();

    }

    private static boolean doUpdate() throws Exception {
        Path currentDir = ResourcesManager.getCurrentJarDirPath();
        Path sourceXMT = ResourcesManager.getCurrentJarPath();
        Path dstXMT = currentDir.resolve("XiaoMiTool.jar");
        if (!MutexUtils.waitUnlock(10)){
            /*Log.debug("Mutex unlock wait failed");*/
        }
        Files.copy(sourceXMT,dstXMT, StandardCopyOption.REPLACE_EXISTING);
        if(!Files.exists(dstXMT)){
            return false;
        }
        Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);

        perms.add(PosixFilePermission.OTHERS_READ);
        perms.add(PosixFilePermission.OTHERS_WRITE);
        perms.add(PosixFilePermission.OTHERS_EXECUTE);

        perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.GROUP_WRITE);
        perms.add(PosixFilePermission.GROUP_EXECUTE);
        try {
            Files.setPosixFilePermissions(dstXMT, perms);
        } catch (Exception ignored){}
        Path java = ResourcesManager.getJavaLaunchExe();
        if (java == null){
            return false;
        }
        return startUpdateProcess(java,dstXMT,false);
     }


    private static void printAlive(){
        System.out.println(ALIVE_YEAH);
        System.out.flush();
    }
}
