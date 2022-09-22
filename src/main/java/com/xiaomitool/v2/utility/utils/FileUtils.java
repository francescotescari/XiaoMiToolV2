package com.xiaomitool.v2.utility.utils;

import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.resources.ResourcesConst;
import com.xiaomitool.v2.resources.ResourcesManager;
import javafx.stage.FileChooser;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class FileUtils {
    private static final int FILE_READ_BUFFER_SIZE = 1024 * 512;

    public static long getFreeSpace(File f) {
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
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file), FILE_READ_BUFFER_SIZE);
        outputStream.write(output.getBytes(ResourcesConst.interalCharset()));
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

    public static Path searchFile(Path directory, String filename, boolean caseSensitive, int maxDepth) throws IOException {
        if (filename == null) {
            return null;
        }
        try (Stream<Path> files = Files.walk(directory, maxDepth)) {
            return files
                    .filter(path -> {
                        String fn = path.getFileName().toString().toLowerCase();
                        return caseSensitive ? filename.equals(fn) : filename.equalsIgnoreCase(fn);
                    }).findFirst().orElse(null);
        }
    }

    public static Path toCanonical(Path p) {
        try {
            return p.normalize().toFile().getCanonicalFile().toPath();
        } catch (Throwable e) {
            return null;
        }
    }

    public static FileSystem openZipFileSystem(Path zipFile, boolean create) throws IOException {
        final Path path = zipFile;
        final URI uri = URI.create("jar:file:" + path.toUri().getPath());
        final Map<String, String> env = new HashMap<>();
        if (create) {
            env.put("create", "true");
        }
        return FileSystems.newFileSystem(uri, env);
    }
}
