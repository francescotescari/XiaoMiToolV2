package com.xiaomitool.v2.logging;

import com.xiaomitool.v2.engine.ToolManager;
import org.apache.commons.io.output.NullOutputStream;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Log {
    public static final boolean ADVANCED_LOG = false;
    private static final String PREFIX_DEBUG = "DEBUG";
    private static final String PREFIX_INFO = "INFO";
    private static final String PREFIX_WARN = "WARN";
    private static String PREFIX_ERROR = "ERROR";
    private static Debugger logOutput = Debugger.fromOutputStream(NullOutputStream.NULL_OUTPUT_STREAM);
    private static boolean LIVE_LOG = true;

    public static void init() {
        logOutput = Debugger.defaultDebugger();
        if (!ToolManager.DEBUG_MODE) {
            PrintStream nullPrint = new PrintStream(new NullOutputStream());
            System.setOut(nullPrint);
            System.setErr(nullPrint);
        }
    }

    public static void log_private(Object arg) {
        log("PRIV", arg, true, true);
    }

    public static void debug(Object arg) {
        if (ADVANCED_LOG || ToolManager.DEBUG_MODE) {
            log(PREFIX_DEBUG, arg, false);
        }
    }

    public static void debugLine() {
    }

    public static void info(Object arg) {
        log(PREFIX_INFO, arg, true);
    }

    public static void warn(Object arg) {
        log(PREFIX_WARN, arg, true);
    }

    public static void error(Object arg) {
        log(PREFIX_ERROR, arg, true);
    }

    public static void printStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String sStackTrace = sw.toString();
    }

    public static void process(Object arg, boolean isFeedback) {
        log("PROC", arg, isFeedback);
    }

    public static void log(String prefix, Object arg, boolean isFeedback) {
        log(prefix, arg, isFeedback, false);
    }

    private static void log(String prefix, Object arg, boolean isFeedback, boolean is_private) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        String classLog = "";
        if (ADVANCED_LOG && stackTraceElements.length > 3) {
            String callerClass = stackTraceElements[3].getClassName();
            int index = callerClass.lastIndexOf('.');
            callerClass = index > -1 ? callerClass.substring(index + 1) : callerClass;
            classLog = "[" + callerClass + "::" + stackTraceElements[3].getMethodName() + "]";
        }
        String out = String.format("%s[%-6s][%06x]%s %s", LogUtils.getTimeStamp(), prefix, Thread.currentThread().hashCode(), classLog, arg == null ? "null" : arg.toString());
        if (Log.ADVANCED_LOG || LIVE_LOG) {
            System.out.println(out);
        }
        if (logOutput != null) {
            try {
                logOutput.writeln(out, isFeedback, is_private);
            } catch (Exception e) {
                System.out.println("Failed to write to feedback output: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void closeLogFile() {
        if (logOutput != null) {
            try {
                logOutput.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static String i(Number number) {
        String string = number.toString();
        if (number instanceof Long) {
            string = Long.toHexString((long) number);
        } else if (number instanceof Integer) {
            string = Integer.toHexString((int) number);
        }
        return string;
    }

    public static Debugger getDebugger() {
        return logOutput;
    }

    public static void exc(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        error(sw.toString());
    }
}
