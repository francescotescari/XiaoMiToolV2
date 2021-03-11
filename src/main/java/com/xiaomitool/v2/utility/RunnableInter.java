package com.xiaomitool.v2.utility;

import com.xiaomitool.v2.logging.Log;

public abstract class RunnableInter implements Runnable {
    @Override
    public void run() {
        try {
            runi();
        } catch (Exception e) {
            Log.error("Runnable inter got onException: " + e.getMessage());
        }
    }

    public abstract void runi() throws Exception;
}
