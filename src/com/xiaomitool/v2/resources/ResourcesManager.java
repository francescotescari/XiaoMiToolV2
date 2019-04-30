package com.xiaomitool.v2.resources;



import com.xiaomitool.v2.engine.ToolManager;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.utility.utils.FileUtils;
import com.xiaomitool.v2.utility.utils.StrUtils;
import javafx.scene.image.Image;
import org.apache.commons.codec.binary.Base64InputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResourcesManager {
    private static Path tmpPath;
    private static final String TMP_DIR = "tmp";
    private static final String RESOURCE_DIR = "res";
    private static final String TOOLS_DIR = "tools";
    private static final String DRIVER_DIR = "driver";
    private static final String LANG_DIR = "lang";
    private static final String ADB_EXE = "adb";
    private static final String FASTBOOT_EXE = "fastboot";
    private static final String XIAOMITOOL_JAR = "XiaoMiTool.jar";
    private static final Path DEFAULT_DOWNLAOD_PATH = getResourcesPath().resolve("downloads");

    private static final Path[] FASTBOOT_FILES_WIN = new Path[]{getToolPath("AdbWinApi.dll", false),getToolPath("AdbWinUsbApi.dll",false),getToolPath("fastboot.exe",true),getToolPath("libwinpthread-1.dll",false)};
    private static final Path[] FASTBOOT_FILES_MAC_LIN = new Path[]{getToolPath("fastboot")};
    public static Path[] getFastbootFilesPath(){
        if (ResourcesConst.isWindows()){
            return FASTBOOT_FILES_WIN;
        } else {
            // MAC AND LINUX
            return FASTBOOT_FILES_MAC_LIN;
        }
    }

    public static void copyResourcesToDir(Path[] resources, Path destinationDir) throws IOException {
        for (Path res : resources){
            Files.copy(res, destinationDir.resolve(res.getFileName()) , StandardCopyOption.REPLACE_EXISTING);
        }
    }


    public static Path getSystemTmpPath(){
        if (tmpPath == null){
            String tmp = System.getProperty("java.io.tmpdir");
            if (StrUtils.isNullOrEmpty(tmp)){
                return getTmpPath();
            }
            Path p = Paths.get(tmp);
            if (!Files.exists(p)){
                return getTmpPath();
            }
            tmpPath = p.resolve("xiaomitool2");
            if (!Files.exists(tmpPath)){
                try {
                    Files.createDirectories(tmpPath);
                } catch (IOException e) {
                    Log.error("Failed to create tmp directory");
                    tmpPath = null;
                    return getTmpPath();
                }
            }
        }
        return tmpPath;
    }

    private static Boolean initStatus = null;
    public static boolean init(){
        if (initStatus == null) {
            Path p = getXiaomitoolPath();
            Log.debug("XmtPath: "+p);
            boolean res = setCurrentPath(p) || ToolManager.DEBUG_MODE;
            boolean b = findWritableTmpDir();
            Log.debug("Found writable tmp dir: "+b);
            Log.info("Temporary dir used: "+getTmpPath());
            initStatus = b && res;
        }
        return initStatus;
    }

    private static Path getXiaomitoolPath(){
        Path cJar = getCurrentJarDirPath();
        if (verifyWorkingDirectoryGood(cJar)){
            return cJar;
        }
        Log.warn("Path not valid working dir:" + cJar);
        cJar = Paths.get(".");
        if (verifyWorkingDirectoryGood(cJar)){
            return cJar;
        }
        Log.warn("Path not valid working dir:" + cJar);
        try {
            Path adbPath = FileUtils.searchFile(getCurrentPath(), ResourcesConst.getOSExe(ADB_EXE), false, 4);
            cJar = adbPath.getParent().getParent().getParent();
            if (verifyWorkingDirectoryGood(cJar)){
                return cJar;
            }
            Log.warn("Path not valid working dir:" + cJar);
        } catch (Throwable t){
            Log.warn("Path failed check working dir:" + t.getMessage());
        }
        return null;
    }

    private static boolean setCurrentPath(Path path){
            currentPath = path;
            if (path != null){

            }
            return path != null;
        }

    private static boolean verifyWorkingDirectoryGood(Path path){
        if (path == null){
            return false;
        }
        Path p1 = path.resolve(XIAOMITOOL_JAR), p2 = path.resolve(RESOURCE_DIR).resolve(TOOLS_DIR).resolve(ResourcesConst.getOSExe(ADB_EXE));
        if (!Files.exists(p1)){
            Log.warn("Path not exists: "+p1);
            return false;
        }
        if (!Files.exists(p2)){
            Log.warn("Path not exists: "+p2);
            return false;
        }
        return true;
    }

    private static Path currentPath;

    public static Path getCurrentPath()
    {
        if (currentPath == null) {
            currentPath = Paths.get(".");
        }
        return currentPath;
    }
    public static Path getResourcesPath(){
        return getCurrentPath().resolve(RESOURCE_DIR);
    }

    public static Path getToolsPath(){
        return getResourcesPath().resolve(TOOLS_DIR);
    }

    public static Path getToolPath(String path){
        return getToolPath(path, true);
    }

    public static Path getToolPath(String path, boolean isExe){
        if (path == null){
            return null;
        }
        if(isExe){
            String extension = ResourcesConst.getOSExeExtension();
            if  (!path.toLowerCase().endsWith(extension)){
                path+=extension;
            }
        }
        return getToolsPath().resolve(path);
    }
    public static Path getAdbPath(){
        return getToolPath(ADB_EXE,true);
    }
    public static Path getFastbootPath(){
        return getToolPath(FASTBOOT_EXE,true);
    }
    public static Path getTmpPath(){
        synchronized (TMP_DIR_SYNC){
            if (tmpPath == null){
                Path p =  getResourcesPath().resolve(TMP_DIR);
                if (!Files.exists(p)){
                    try {
                        Files.createDirectories(p);
                    } catch (IOException e) {
                        return getResourcesPath();
                    }
                }
                Log.debug("Setting tmp path to easy val");
                tmpPath = p;
            }
            return tmpPath;
        }
    }
    public static Path getLangPath(){
        return getResourcesPath().resolve(LANG_DIR);
    }
    public static Path getLangFilePath(String lang){
        return getLangPath().resolve(lang+".xml");
    }
    public static Path getDriverPath(){
        return getResourcesPath().resolve(DRIVER_DIR);
    }
    public static List<Path> getAllInfPaths() throws IOException {
        Stream<Path> result = Files.find(getDriverPath(), 2, new BiPredicate<Path, BasicFileAttributes>() {
            @Override
            public boolean test(Path path, BasicFileAttributes basicFileAttributes) {
                return path != null && basicFileAttributes.isRegularFile() &&  path.toString().toLowerCase().endsWith(".inf");
            }
        });
        return result.collect(Collectors.toList());
    }


    public static Image b64toImage(byte[] base64encoded){
        return new Image(new Base64InputStream(new ByteArrayInputStream(base64encoded),false));
    }

    public static Path getLogPath() {
        return getTmpPath().resolve("logs");
    }

    private static Path currentJarDirPath;
    public static Path getCurrentJarDirPath(){
        if (currentJarDirPath == null) {
            try {
                Path jarPath = getCurrentJarPath();
                if (Files.exists(jarPath.getParent())){
                    currentJarDirPath = jarPath.getParent();
                }
                Log.info("Current jar dir path: "+ currentJarDirPath);
            } catch (Exception e) {
                Log.error("Failed to find current jar dir path: "+e.getMessage());
            }
            if (currentJarDirPath == null){
                Path xiaomiPath = getXiaomitoolPath();
                if (xiaomiPath != null && Files.exists(xiaomiPath.resolve(XIAOMITOOL_JAR))){
                    currentJarDirPath = xiaomiPath;
                }
            }
        }
        return currentJarDirPath;
    }
    private static Path currentJarPath;
    public static Path getCurrentJarPath(){
        if (currentJarPath == null) {
            try {
                Path jarPath = Paths.get(ResourcesManager.class.getProtectionDomain().getCodeSource().getLocation()
                        .toURI());
                if (Files.exists(jarPath)){
                    currentJarPath = jarPath;
                }
                Log.info("Current jar path: "+ currentJarPath);
            } catch (Exception e) {
                Log.error("Failed to find current jar path: "+e.getMessage());
            }
            if (currentJarPath == null){
                Path xiaomiPath = getXiaomitoolPath();
                if (xiaomiPath != null){
                    currentJarPath = xiaomiPath.resolve(XIAOMITOOL_JAR);
                    if (!Files.exists(currentJarPath)){
                        currentJarPath = null;
                    }
                }
            }
        }
        return currentJarPath;
    }

    private static Boolean quickUpdateSupported = null;
    public static boolean isQuickUpdatedSupported() {
        if (quickUpdateSupported == null) {
            Path p = getCurrentJarDirPath();
            quickUpdateSupported = ( p != null && getJavaLaunchExe() != null && checkIfWritable(p) );
            Log.info("Quick update supported: "+quickUpdateSupported);
        }
        return quickUpdateSupported;
    }

    private static Path javaLaunchExe = null;
    public static Path getJavaLaunchExe(){
        return getJavaLaunchExe(ResourcesConst.isWindows() ? "javaw.exe" : "java");
    }

    public static Path getJavaLaunchExe(String javaExe){

        if (javaLaunchExe == null){
            try {
                Path currentJarPath = getCurrentJarDirPath();
                if (currentJarPath != null) {
                    Path binPath = currentJarPath.resolve("bin").resolve(javaExe);
                    if (Files.exists(binPath)) {
                        javaLaunchExe = binPath;
                    } else {
                        Log.warn("Java launch path not existing: "+binPath);
                        if (ToolManager.DEBUG_MODE) {
                            binPath = currentJarPath.getParent().getParent().getParent().resolve("bin").resolve(javaExe);
                            if (Files.exists(binPath)) {
                                javaLaunchExe = binPath;
                            } else {
                                Log.warn("Java launch path not existing: " + binPath);
                            }
                        }
                    }
                }
            } catch (Exception e){
                Log.debug(e);
            }
            Log.info("Java launch path: "+javaLaunchExe);
        }
        if (javaLaunchExe == null){
            try {
                javaLaunchExe = FileUtils.searchFile(getCurrentPath(), javaExe, false, 4);
                if (javaLaunchExe == null){
                    throw new Exception("not found");
                }
            } catch (Throwable t){
                Log.warn("Java launch path not in current path: "+t.getMessage());
            }
        }
        if (javaLaunchExe == null){
            try {
                Path parent = getCurrentPath().normalize().getParent();
                if (parent == null){
                    throw new Exception("null parent");
                }
                javaLaunchExe = FileUtils.searchFile(parent, javaExe, false, 4);
                if (javaLaunchExe == null){
                    throw new Exception("not found");
                }
            } catch (Throwable t){
                Log.warn("Java launch path not in parent path: "+t.getMessage());
            }
        }
        return javaLaunchExe;
    }

    private static final Integer TMP_DIR_SYNC = 1;
    private static synchronized boolean findWritableTmpDir(){
        synchronized (TMP_DIR_SYNC) {
            try {
                Path tmp = getTmpPath();
                if (checkIfWritable(tmp)) {
                    tmpPath = tmp;
                    return true;
                }
                Log.warn("Standard tmp path is not writable, trying java.io.tmpdir");
                tmp = FileUtils.toCanonical(Paths.get(System.getProperty("java.io.tmpdir")).resolve("xiaomitool_v2"));
                if (tmp != null && !Files.exists(tmp)) {
                    tmp.toFile().mkdirs();
                }
                if (checkIfWritable(tmp)) {
                    Log.debug("Setting tmp path to "+tmp);
                    tmpPath = tmp;
                    return true;
                }
                Log.warn("Failed to get tmp dir: no valid dir found");
                return false;
            } catch (Throwable t) {
                Log.warn("Failed to get tmp dir: " + t.getMessage());
                return false;
            }
        }
    }

    private static boolean checkIfWritable(Path path){
        if (path == null){
            return false;
        }
        try {
            Path test = path.resolve(StrUtils.randomWord(16));
            FileUtils.writeAll(test.toFile(), "test");
            Files.delete(test);
            return true;
        } catch (Throwable t) {
            Log.warn("Path not writable: "+t.getMessage());
            Log.printStackTrace(t);
            return false;
        }

    }


}
