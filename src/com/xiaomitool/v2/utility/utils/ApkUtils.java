package com.xiaomitool.v2.utility.utils;

import com.xiaomitool.v2.apk.ApkManifestDecoder;
import com.xiaomitool.v2.apk.ApkManifestParser;
import com.xiaomitool.v2.logging.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;

public class ApkUtils {
    public static String getPackageName(Path apkFile) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (ApkManifestDecoder decoder = new ApkManifestDecoder(apkFile)) {
                decoder.open();
                decoder.decode(outputStream);
            }
            outputStream.close();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            ApkManifestParser manifestParser = new ApkManifestParser();
            manifestParser.open(inputStream);
            return manifestParser.getPackageName();
        } catch (Throwable t){
            Log.warn("Failed to get apk package name for "+apkFile+": "+t.getMessage());
            return null;
        }
    }
}
