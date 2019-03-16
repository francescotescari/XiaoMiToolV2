package com.xiaomitool.v2.logging;

import com.xiaomitool.v2.resources.ResourcesManager;
import com.xiaomitool.v2.utility.utils.SettingsUtils;

import java.io.*;
import java.sql.Timestamp;


public class Log {

    public static final boolean ADVANCED_LOG  = true;
    private static final String PREFIX_DEBUG = "DEBUG";
    private static final String PREFIX_INFO = "INFO";
    private static final String PREFIX_WARN = "WARN";
    private static final String PREFIX_ERROR = "ERROR";
    private static  OutputStream LOG_OUTPUT = null;
    private static boolean TRY_OPEN_LOG = true;
    private static OutputStream outputStream = System.out;
    private static File outputFile = null;

    private static OutputStream getLogOutput(){
        if (LOG_OUTPUT == null){
            if (!TRY_OPEN_LOG){
                return null;
            }
            try {
                LOG_OUTPUT = new FileOutputStream(ResourcesManager.getSystemTmpPath().resolve("log"+System.currentTimeMillis()+".txt").toFile());
            } catch (Throwable t) {
                TRY_OPEN_LOG = false;
                LOG_OUTPUT = null;
            }
        }
        return LOG_OUTPUT;
    }

    public static void debug(Object arg){

        log(PREFIX_DEBUG,arg);
    }
    public static void debugLine(){
        Log.debug("-----------------------------");
    }
    public static void info(Object arg){

        log(PREFIX_INFO, arg);
    }
    public static void warn(Object arg){
        log(PREFIX_WARN, arg);
    }
    public static void error(Object arg){
        log(PREFIX_ERROR, arg);
    }
    public static void printStackTrace(Exception e){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String sStackTrace = sw.toString();
        Log.debug(sStackTrace);
    }

    public static void log(String prefix, Object arg){
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        String classLog = "";
        if (ADVANCED_LOG && stackTraceElements.length > 3) {
            String callerClass = stackTraceElements[3].getClassName();
            int index = callerClass.lastIndexOf('.');
            callerClass = index > -1 ? callerClass.substring(index+1) : callerClass;
            classLog = "["+callerClass+"::"+stackTraceElements[3].getMethodName()+"]";
        }
        String out = String.format("%s[%-6s]%s %s%s",LogUtils.getTimeStamp(),prefix,classLog,arg == null ? "null" : arg.toString(), SettingsUtils.lineSeparator);
        try {
            OutputStream logOutput = getLogOutput();
            if (logOutput != null){
                try {
                    logOutput.write(out.getBytes());
                } catch (Throwable ignored){

                }
            }
            outputStream.write(out.getBytes());
        } catch (IOException e) {
            System.out.print(out);
        }
    }
    public static void  closeLogFile()  {
        OutputStream out = getLogOutput();
        if (out != null){
            try {
                out.close();
            } catch (IOException ignored) {

            }
        }
    }
    public static String i(Number number){
        String string = number.toString();
        if (number instanceof Long){
            string = Long.toHexString((long) number);
        } else if (number instanceof Integer){
            string = Integer.toHexString((int) number);
        }
        return string;
    }

    public static void setOutputStream(OutputStream out){
        Log.outputStream = out;
    }
    public static boolean setOutputFile(File outputFile){
        try {
            Log.outputStream = new FileOutputStream(outputFile);
            Log.outputFile = outputFile;
        } catch (FileNotFoundException e) {
            return false;
        }
        return true;
    }

    public static void debugArray(Object[] arg){
        Log.debug(LogUtils.toStringArray(arg));
    }


}
