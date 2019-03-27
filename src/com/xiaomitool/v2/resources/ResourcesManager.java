package com.xiaomitool.v2.resources;



import com.xiaomitool.v2.logging.Log;
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

    public static Path getCurrentPath(){
        return Paths.get(".");
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
        Path p =  getResourcesPath().resolve(TMP_DIR);
        if (!Files.exists(p)){
            try {
                Files.createDirectories(p);
            } catch (IOException e) {
                return getResourcesPath();
            }
        }
        return p;
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

    public static Image b64toImage(String base64){
        return b64toImage(base64.getBytes());
    }
    public static Image b64toImage(byte[] base64encoded){
        return new Image(new Base64InputStream(new ByteArrayInputStream(base64encoded),false));
    }

    public static Path getLogPath() {
        return getTmpPath().resolve("logs");
    }
}
