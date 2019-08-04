package com.xiaomitool.v2.utility;

import com.xiaomitool.v2.logging.Log;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class WaitSemaphore {
    protected Semaphore semaphore;

    private String name;
    public WaitSemaphore() {
        this(0);
    }
    public WaitSemaphore(int permits){
        this(permits, null);
    }

    public WaitSemaphore(int permits, String name){
        semaphore = new Semaphore(permits,true);
        this.name = name;
        log_action("Created");
    }
    public void increase(){
        log_action("Increase");
        semaphore.release();
    }
    public void decrease() throws InterruptedException {
        log_action("Decrease");
        new Exception().printStackTrace();
        semaphore.acquire();
    }
    public void waitOnce() throws InterruptedException {
        log_action("WaitOnce");
        semaphore.acquire();
        semaphore.release();
    }
    private void log_action(String action){
        Log.debug("SEM "+(this.name == null ? this : this.name)+", "+this.semaphore.availablePermits()+", "+action);
    }

    public boolean waitOnce(int timeoutSeconds) throws InterruptedException {
        log_action("WaitOnceT");
        if (semaphore.tryAcquire(timeoutSeconds, TimeUnit.SECONDS)){
            semaphore.release();
            return true;
        }
        return false;
    }
    public void setPermits(int permits){
        log_action("setPermits"+permits);
        semaphore.drainPermits();
        semaphore.release(permits);
    }

    public int getPermitNumber() {
        return semaphore.availablePermits();
    }
}
