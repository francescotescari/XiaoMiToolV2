package com.xiaomitool.v2.utility.utils;

import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.resources.ResourcesManager;
import javafx.stage.FileChooser;
import org.apache.commons.io.IOUtils;

import java.io.*;

public class FileUtils {
    private static final int FILE_READ_BUFFER_SIZE = 1024*512;
    public static long getFreeSpace(File f){
        return f.getUsableSpace();
    }
    public static String readAll(File file) throws IOException {
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file), FILE_READ_BUFFER_SIZE);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, outputStream, FILE_READ_BUFFER_SIZE);
        outputStream.close();
        inputStream.close();
        return outputStream.toString();
    }
    public static void writeAll(File file, String output) throws IOException {
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file),FILE_READ_BUFFER_SIZE);
        outputStream.write(output.getBytes());
        outputStream.close();

    }

    public static File selectFile(String title, FileChooser.ExtensionFilter... extensions) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(ResourcesManager.getTmpPath().toFile());
        fileChooser.setTitle(title);
        if (extensions != null) {


            fileChooser.getExtensionFilters().addAll(extensions);
        }
        return fileChooser.showOpenDialog(WindowManager.mainWindow());
    }
}
