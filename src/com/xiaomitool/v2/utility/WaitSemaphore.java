package com.xiaomitool.v2.utility;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class WaitSemaphore {
    Semaphore semaphore;
    public WaitSemaphore() {
        this(0);
    }
    public WaitSemaphore(int permits){
        semaphore = new Semaphore(permits,true);
    }
    public void increase(){
        semaphore.release();
    }
    public void decrease() throws InterruptedException {
        semaphore.acquire();
    }
    public void waitOnce() throws InterruptedException {
        semaphore.acquire();
        semaphore.release();
    }
    public boolean waitOnce(int timeoutSeconds) throws InterruptedException {
        if (semaphore.tryAcquire(timeoutSeconds, TimeUnit.SECONDS)){
            semaphore.release();
            return true;
        }
        return false;
    }
    public void setPermits(int permits){
        semaphore.drainPermits();
        semaphore.release(permits);
    }

}
