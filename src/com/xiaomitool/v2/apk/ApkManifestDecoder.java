package com.xiaomitool.v2.apk;

import brut.androlib.AndrolibException;
import brut.androlib.res.data.ResPackage;
import brut.androlib.res.data.ResTable;
import brut.androlib.res.decoder.AXmlResourceParser;
import brut.androlib.res.decoder.ResAttrDecoder;
import brut.androlib.res.decoder.XmlPullStreamDecoder;
import brut.androlib.res.util.ExtMXSerializer;
import com.xiaomitool.v2.utility.utils.FileUtils;

import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ApkManifestDecoder implements Closeable {
    private Path zipFile;
    private FileSystem mountedFileSystem;

    public ApkManifestDecoder(String file){
        this(Paths.get(file));
    }
    public ApkManifestDecoder(Path file){
        this.zipFile = file;
    }

    public void open() throws IOException {
        if (mountedFileSystem != null){
            return;
        }
        mountedFileSystem = FileUtils.openZipFileSystem(zipFile, false);
        isOpen = true;
    }

    public void close() throws IOException {
        if (this.mountedFileSystem == null){
            return;
        }
        this.mountedFileSystem.close();
        this.mountedFileSystem = null;
        isOpen = false;
    }

    private boolean isOpen = false;

    public boolean isOpen() {
        return isOpen;
    }

    private XmlPullStreamDecoder decoder;
    private XmlPullStreamDecoder getDecoder(){
        if (this.decoder == null){
            AXmlResourceParser axmlParser = new AXmlResourceParser();
            axmlParser.setAttrDecoder(new ResAttrDecoder());
            axmlParser.getAttrDecoder().setCurrentPackage(new ResPackage(new ResTable(), 0,null));
            decoder = new XmlPullStreamDecoder(axmlParser,getResXmlSerializer());
        }
        return decoder;
    }

    public static ExtMXSerializer getResXmlSerializer() {
        ExtMXSerializer serial = new ExtMXSerializer();
        serial.setProperty(PROPERTY_SERIALIZER_INDENTATION, " ");
        serial.setProperty(PROPERTY_SERIALIZER_LINE_SEPARATOR, "\n");
        serial.setProperty(PROPERTY_DEFAULT_ENCODING, "utf-8");
        serial.setDisabledAttrEscape(true);
        return serial;
    }
    private static final String DEFAULT_MANIFEST_PATH = "AndroidManifest.xml";

    public void decode(OutputStream destination) throws IOException {
        if (!isOpen()){
            throw new IOException("Closed decoder");
        }
        Path file = mountedFileSystem.getPath("/").resolve(DEFAULT_MANIFEST_PATH);
        try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(file))) {
            XmlPullStreamDecoder decoder = getDecoder();
            try {
                decoder.decode(inputStream, destination);
            } catch (AndrolibException e) {
                throw new IOException("AndroidLibException: "+e.getMessage(), e);
            }
        }
    }

    private static final String PROPERTY_SERIALIZER_INDENTATION = "http://xmlpull.org/v1/doc/properties.html#serializer-indentation";
    private static final String PROPERTY_SERIALIZER_LINE_SEPARATOR = "http://xmlpull.org/v1/doc/properties.html#serializer-line-separator";
    private static final String PROPERTY_DEFAULT_ENCODING = "DEFAULT_ENCODING";

}
