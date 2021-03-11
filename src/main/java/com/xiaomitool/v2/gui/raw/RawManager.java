package com.xiaomitool.v2.gui.raw;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class RawManager {
    public static final String DISCLAIMER = "Disclaimer.txt";

    public static byte[] getRawBytes(String resource) throws IOException {
        InputStream stream = RawManager.class.getResourceAsStream(resource);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IOUtils.copy(stream, outputStream);
        return outputStream.toByteArray();
    }

    public static String getRawString(String resource) throws IOException {
        return new String(getRawBytes(resource));
    }

    public static URL getRawURI(String resource) {
        return RawManager.class.getResource(resource);
    }

    public static String getDisclaimer() {
        try {
            return RawManager.getRawString(RawManager.DISCLAIMER);
        } catch (IOException e) {
            return "Failed to get disclaimer: " + e.getMessage();
        }
    }

    public static InputStream getInputStream(String resource) {
        return RawManager.class.getResourceAsStream(resource);
    }
}
