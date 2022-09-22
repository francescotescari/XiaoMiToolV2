package com.xiaomitool.v2.logging;

import com.xiaomitool.v2.resources.ResourcesConst;
import com.xiaomitool.v2.resources.ResourcesManager;
import com.xiaomitool.v2.utility.FeedbackOutputStream;
import org.apache.commons.io.output.NullOutputStream;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

public class Debugger extends BufferedOutputStream {
    private static final byte LOG_LINE_SEPARATOR = '\n';
    private static final byte[] LINE_SEPARATOR = System.lineSeparator().getBytes(ResourcesConst.interalCharset());
    private static final String LOG_FN = "xmt2log_";
    private static final Pattern LOG_PATTERN = Pattern.compile(LOG_FN + "(\\d+)\\.txt$");
    private static final int MAX_LOG_FILES = 5;
    private static final int MAX_FEEDBACK_PAYLOAD_SIZE = 30000000;
    private static Debugger defaultDebugger;
    private final FeedbackOutputStream feedbackOutputStream = new FeedbackOutputStream();
    private DeflaterOutputStream gzipOutputStream;

    private Debugger(OutputStream outputStream) {
        super(outputStream);
    }

    public static Debugger fromOutputStream(OutputStream outputStream) {
        return new Debugger(outputStream);
    }

    public static Debugger defaultDebugger() {
        return defaultDebugger(true);
    }

    public static Debugger defaultDebugger(boolean deleteOldLogs) {
        if (deleteOldLogs) {
            try {
                deleteOldLogs();
            } catch (IOException e) {
                Log.error("Failed to identify old logs: " + e.getMessage());
            }
        }
        if (defaultDebugger == null) {
            try {
                Path defaultDebugPath = ResourcesManager.getLogPath().resolve(LOG_FN + System.currentTimeMillis() + ".txt");
                createDefaultPath(defaultDebugPath);
                defaultDebugger = fromOutputStream(new FileOutputStream(defaultDebugPath.toFile()));
            } catch (Exception e) {
                Log.error("Failed to create debug output stream: " + e.getMessage());
                return fromOutputStream(new NullOutputStream());
            }
        }
        return defaultDebugger;
    }

    private static void createDefaultPath(Path defaultDebugPath) throws IOException {
        if (!Files.exists(defaultDebugPath.getParent())) {
            Files.createDirectories(defaultDebugPath.getParent());
        }
    }

    private static void deleteOldLogs() throws IOException {
        TreeSet<DatePath> allLogs = getExistingLogsFiles();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (allLogs.size() > MAX_LOG_FILES) {
                    DatePath datePath = allLogs.pollFirst();
                    if (datePath != null) {
                        try {
                            Files.delete(datePath.path);
                        } catch (IOException e) {
                            Log.error("Failed to delete log file: " + e.getMessage());
                        }
                    }
                }
            }
        }).start();
    }

    private static TreeSet<DatePath> getExistingLogsFiles() throws IOException {
        Supplier<TreeSet<DatePath>> supplier = () -> new TreeSet<>(Comparator.comparingLong(o -> o.date));
        try (Stream<Path> files = Files.walk(ResourcesManager.getLogPath())) {
            return files.filter(Files::isRegularFile).map(new Function<Path, DatePath>() {
                @Override
                public DatePath apply(Path path) {
                    try {
                        String p = path.toString();
                        Matcher m = LOG_PATTERN.matcher(p);
                        if (!m.find()) {
                            return null;
                        }
                        DatePath datePath = new DatePath();
                        datePath.path = path;
                        datePath.date = Long.parseLong(m.group(1));
                        return datePath;
                    } catch (Throwable t) {
                        return null;
                    }
                }
            }).collect(Collectors.toCollection(supplier));
        }
    }

    private DeflaterOutputStream getCompressingOutputStream() throws IOException {
        if (gzipOutputStream == null) {
            gzipOutputStream = new DeflaterOutputStream(feedbackOutputStream, new Deflater(Deflater.BEST_COMPRESSION, false));
        }
        return gzipOutputStream;
    }

    public void setCompressingOutputStream(DeflaterOutputStream outputStream) {
        this.gzipOutputStream = outputStream;
    }

    public void writeln(Object line, boolean isFeedback, boolean is_private) throws IOException {
        byte[] data = String.valueOf(line).getBytes(ResourcesConst.interalCharset());
        if (!is_private) {
            this.write(data);
            this.write(LINE_SEPARATOR);
        }
        if (isFeedback) {
            synchronized (feedbackOutputStream) {
                OutputStream outputStream = getCompressingOutputStream();
                outputStream.write(data);
                outputStream.write(LOG_LINE_SEPARATOR);
                outputStream.flush();
            }
        }
    }

    public InputStream getFeedbackData(String userMessage) throws IOException {
        synchronized (feedbackOutputStream) {
            DeflaterOutputStream oStream = getCompressingOutputStream();
            oStream.finish();
            feedbackOutputStream.setFlagsOnClose(FeedbackOutputStream.FLAG_COMPRESSED);
            oStream.close();
            setCompressingOutputStream(new DeflaterOutputStream(feedbackOutputStream, new Deflater(Deflater.BEST_COMPRESSION, false)));
            feedbackOutputStream.setUserMessage(userMessage);
            return feedbackOutputStream.getReadInputStream();
        }
    }

    private static class DatePath {
        public Path path;
        public long date;
    }
}
