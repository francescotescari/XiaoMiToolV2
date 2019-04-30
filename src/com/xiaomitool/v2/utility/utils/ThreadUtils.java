package com.xiaomitool.v2.utility.utils;

import com.xiaomitool.v2.logging.Log;

public class ThreadUtils {
    public static void sleepSilently(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Log.printStackTrace(e);
        }
    }

    public static void runSafely(Runnable runnable){
        try {
            runnable.run();
        } catch (Throwable t){
            Log.error("Run safely failed: "+t.getMessage());
            Log.printStackTrace(t);
        }
    }
}
