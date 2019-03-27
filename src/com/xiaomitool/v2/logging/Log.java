package com.xiaomitool.v2.logging;

import com.xiaomitool.v2.utility.utils.SettingsUtils;

import java.io.*;


public class Log {

    public static final boolean ADVANCED_LOG  = true;
    private static final String PREFIX_DEBUG = "DEBUG";
    private static final String PREFIX_INFO = "INFO";
    private static final String PREFIX_WARN = "WARN";
    private static String PREFIX_ERROR = "ERROR";
    private static Debugger logOutput = Debugger.defaultDebugger();
    private static boolean LIVE_LOG = true;





    public static void debug(Object arg){
        if (ADVANCED_LOG) {
            log(PREFIX_DEBUG, arg, false);
        }
    }
    public static void debugLine(){
        Log.debug("-----------------------------");
    }
    public static void info(Object arg){

        log(PREFIX_INFO, arg, true);
    }
    public static void warn(Object arg){
        log(PREFIX_WARN, arg, true);
    }
    public static void error(Object arg){
        log(PREFIX_ERROR, arg, true);
    }
    public static void printStackTrace(Exception e){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String sStackTrace = sw.toString();
        Log.debug(sStackTrace);
    }

    public static void process(Object arg){
        log("PROC",arg, false);
    }

    public static void log(String prefix, Object arg, boolean isFeedback){
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        String classLog = "";
        if (ADVANCED_LOG && stackTraceElements.length > 3) {
            String callerClass = stackTraceElements[3].getClassName();
            int index = callerClass.lastIndexOf('.');
            callerClass = index > -1 ? callerClass.substring(index+1) : callerClass;
            classLog = "["+callerClass+"::"+stackTraceElements[3].getMethodName()+"]";
        }
        String out = String.format("%s[%-6s]%s %s",LogUtils.getTimeStamp(),prefix,classLog,arg == null ? "null" : arg.toString());

        if (Log.ADVANCED_LOG || LIVE_LOG) {
            System.out.println(out);
        }

            if (logOutput != null){
                try {
                    logOutput.writeln(out, isFeedback);
                } catch (Exception e) {

                    System.out.println("Failed to write to feedback output: "+e.getMessage());
                    e.printStackTrace();
                }
            }

    }
    public static void  closeLogFile()  {
        if (logOutput != null){
            try {
                logOutput.close();
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
