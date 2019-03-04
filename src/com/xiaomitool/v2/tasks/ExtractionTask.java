package com.xiaomitool.v2.tasks;

import com.xiaomitool.v2.crypto.Hash;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.utility.FeedbackInputStream;
import com.xiaomitool.v2.utility.RunnableWithArg;
import com.xiaomitool.v2.utility.utils.FileUtils;
import com.xiaomitool.v2.utility.utils.SettingsUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ExtractionTask extends Task {
    private static final int READ_BLOCK_SIZE = 16*1024;
    private static final int FILE_READ_BLOCK_SIZE = 128*1024;
    private File fileToExtract;
    private File dirExtractTo;
    private long fileSize = -1;
    private long totalRead = 0, lastTotal = 0;
    private ExtractionType type;
    private boolean keepPath;


    public ExtractionTask(UpdateListener listener, File filename, File destinationDir, ExtractionType type, boolean keepPath){
        super(listener);
        this.fileToExtract = filename;
        this.dirExtractTo = destinationDir;
        this.type = type;
        this.keepPath = keepPath;
    }
    public ExtractionTask(UpdateListener listener, String filename, String destinationDir, ExtractionType type, boolean keepPath ){
        this(listener, new File(filename), new File(destinationDir), type, keepPath);
    }


    @Override
    protected boolean canPause() {
        return true;
    }

    @Override
    protected boolean canStop() {
        return true;
    }

    @Override
    protected boolean pauseInternal() {
        return false;
    }

    @Override
    protected boolean stopInternal() {
        return false;
    }

    //public ExtractionTask(File )


    public enum ExtractionType {
        GZIP,
        ZIP,
        TAR,
        TGZ,
        NONE
    }
    @Override
    public void startInternal(){
        int index = 0;
        boolean validOutputDir = dirExtractTo != null;
        do {
            if (!validOutputDir) {

                dirExtractTo = SettingsUtils.getExtractFile(fileToExtract, index);
                Log.debug("Finding new extraction directory: "+dirExtractTo.toString());
            }
            validOutputDir = true;
            if (dirExtractTo.exists()) {
                validOutputDir = false;
                ++index;
                String fn = getExtractionHash();
                File hashFile = new File(dirExtractTo, fn);
                if (hashFile.exists()) {
                    finished(dirExtractTo);
                    return;
                }
            }
        } while (!validOutputDir);
        try {
            Files.createDirectories(dirExtractTo.toPath());
        } catch (IOException e) {
            error(e);
            return;
        }
        fileSize = fileToExtract.length();
        setTotalSize(fileSize);
        File out = null;
        try {
            switch (type) {
                case TAR:
                    out = extractTar(keepPath);
                    break;
                case TGZ:
                    out = extractTgz(keepPath);
                    break;
                case ZIP:
                    out = extractZip(keepPath);
                    break;
                case GZIP:
                    out = extractGzip();
                    break;
                case NONE:
                    finished(fileToExtract);
                    return;
            }
        } catch (IOException e){
            error(e);
            return;
        }
        String fn = getExtractionHash();
        File hashFile = new File(dirExtractTo, fn);
        if (!hashFile.exists()){
            try {
                FileUtils.writeAll(hashFile,this.toString());
            } catch (IOException e) {
                Log.warn("Failed to write file "+hashFile+": "+e.getMessage());
            }
        }

        finished(out);
    }

    private File extractGzip() throws IOException {

        InputStream stream = new GZIPInputStream(new FeedbackInputStream(new BufferedInputStream(new FileInputStream(fileToExtract),FILE_READ_BLOCK_SIZE), new RunnableWithArg() {
            @Override
            public void run(Object arg) {
                onUpdateInternal((int) arg);
            }
        }));
        File output = new File(dirExtractTo, FilenameUtils.removeExtension(fileToExtract.getName()));
        OutputStream ostream = new FileOutputStream(output);
        ioRead(stream,ostream);
        stream.close();
        ostream.close();
        return output;
    }
    private File extractZip(boolean keepPath) throws IOException {

        ZipInputStream zis = new ZipInputStream(new FeedbackInputStream(new BufferedInputStream(new FileInputStream(fileToExtract),FILE_READ_BLOCK_SIZE), new RunnableWithArg() {
            @Override
            public void run(Object arg) {
                onUpdateInternal((int) arg);
            }
        }));
        ZipEntry zipEntry;
        while ((zipEntry = zis.getNextEntry()) != null) {
            String fileName = zipEntry.getName();
            File newFile = new File(dirExtractTo, keepPath ? fileName : FilenameUtils.getName(fileName));
            if (zipEntry.isDirectory()){
                if (!newFile.exists()) {
                    Files.createDirectories(newFile.toPath());
                }
                continue;
            } else {
                if (!newFile.getParentFile().exists()){
                    Files.createDirectories(newFile.getParentFile().toPath());
                }
            }
            FileOutputStream ostream = new FileOutputStream(newFile);
            ioRead(zis, ostream);
            ostream.close();
        }
        zis.close();
        return dirExtractTo;
    }
    private File extractTar(boolean keepPath) throws IOException {
        TarArchiveInputStream tarIn = new TarArchiveInputStream(new FeedbackInputStream(new BufferedInputStream(new FileInputStream(fileToExtract), FILE_READ_BLOCK_SIZE), arg -> onUpdateInternal((int) arg)));
        return extractTarInternal(tarIn, keepPath);
    }
    private File extractTgz(boolean keepPath) throws IOException {

        TarArchiveInputStream tarIn = new TarArchiveInputStream(new GZIPInputStream(new FeedbackInputStream(new BufferedInputStream(new FileInputStream(fileToExtract), FILE_READ_BLOCK_SIZE), arg -> onUpdateInternal((int) arg))));
        return extractTarInternal(tarIn, keepPath);
    }

    private File extractTarInternal(TarArchiveInputStream tarIn, boolean keepPath) throws IOException {
        ArchiveEntry entry;
        while ((entry = tarIn.getNextEntry()) != null) {
            File tmpFile = new File(dirExtractTo, keepPath ? entry.getName() : FilenameUtils.getName(entry.getName()));
            if (entry.isDirectory()){
                tmpFile.mkdirs();
                continue;
            }
            OutputStream out = new FileOutputStream(tmpFile);
            ioRead(tarIn, out);
            out.close();
        }
        tarIn.close();
        return dirExtractTo;
    }

    private void ioRead(InputStream stream, OutputStream ostream) throws IOException {
        byte[] buffer = new byte[READ_BLOCK_SIZE];
        int  read = stream.read(buffer);
        while (read > 0 && !STATUS.ABORTED.equals(status)){
            if (STATUS.PAUSED.equals(status)){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            ostream.write(buffer, 0, read);
            read = stream.read(buffer);
        }
    }

    private void onUpdateInternal(int read){
        totalRead+=read;
        if (totalRead-lastTotal > 1024*1024){
            update(totalRead);
            lastTotal = totalRead;
        }

    }

    private String getExtractionHash(){
        String toHash = fileToExtract.toString()+type.toString()+fileToExtract.length();
        return "."+Hash.md5Hex(toHash);
    }
    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        String n = System.lineSeparator();
        try {
            builder.append("Extraction entry").append(n).append("Input file: ").append(fileToExtract.toString()).append(n).append("Output dir: ").append(dirExtractTo.toString()).append(n).append("Extraction type: ").append(type.toString()).append(n).append("File size: ").append(fileToExtract.length());
        } catch (Throwable t){
            return "Extraction entry"+n+"Failed to get all info: "+t.getMessage();
        }
        return builder.toString();
    }


}
