package com.xiaomitool.v2.tasks;

import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.utility.WaitSemaphore;

import java.time.Duration;
import java.time.LocalDateTime;

public abstract class Task {
    protected STATUS status = STATUS.READY;
    protected UpdateListener listener;
    protected Thread runningThread;
    private Object result;
    private Exception error;
    private long totalSize = -1;
    private LocalDateTime timeLatestUpdate, timeStart;
    private WaitSemaphore isNotRunning = new WaitSemaphore();
    private long latestUpdate = -1;
    public Task(){
        this.listener = new UpdateListener.Debug();
    }
    public Task(UpdateListener listener){
        this.listener = listener;
    }

    public UpdateListener getListener() {
        return listener;
    }

    public enum STATUS {
        READY,
        RUNNING,
        PAUSED,
        ERROR,
        FINISHED,
        ABORTED
    }

     void restart() throws InterruptedException {
         Log.info("Restarting task: "+this);
        if (runningThread != null){
            runningThread.join(2000);
            start(false);
        } else {
            start(true);
        }

    }

    void start(){
        start(false);
    }
     private void start(boolean sameThread){
        status = STATUS.RUNNING;
        timeStart = LocalDateTime.now();
        timeLatestUpdate = LocalDateTime.now();
        Log.info("Starting task: same thread: "+sameThread+" : "+this);
         Runnable runnable = () -> {
             try {
                 startInternal();
             } catch (Exception e){
                 error(e);
             }
         };
        if (sameThread){
            runnable.run();
        }else {
            runningThread = new Thread(runnable);
            runningThread.start();
        }
    }
    protected void update(long done){
        this.latestUpdate = done;
        Duration durationLatest = Duration.between(timeLatestUpdate, LocalDateTime.now());
        timeLatestUpdate = (LocalDateTime) durationLatest.addTo(timeLatestUpdate);
        Duration durationTotal = Duration.between(timeStart, timeLatestUpdate);
        listener.onUpdate(done, totalSize, durationLatest, durationTotal);
    }
    public long getLatestUpdate(){
        return this.latestUpdate;
    }
    protected void finished(Object subject){
        Log.info("Task finished: result: "+subject+" : "+this);
        update(totalSize);
        result = subject;
        this.status = STATUS.FINISHED;
        listener.onFinished(subject);
        isNotRunning.increase();
        if (runningThread != null) {
            runningThread.interrupt();
        }
    }
    protected void error(Exception e){
        Log.error("Task error: exception: "+e.getMessage()+" : "+this);
        this.status = STATUS.ERROR;
        this.error = e;
        listener.onError(e);
        isNotRunning.increase();
    }

    protected abstract void startInternal() throws Exception;
    protected void setTotalSize(long size){
        this.totalSize = size;
        listener.onStart(totalSize);
    }
    public long getTotalSize(){
        return this.totalSize;
    }

    public boolean pause(){
        if(!canPause()){
            return false;
        }
        if( pauseInternal()){
            Log.info("Task paused: "+this);
            status = STATUS.PAUSED;
            return true;
        }
        return false;
    }
    public boolean stop(){
        if(!canStop()){
            return false;
        }
        if(stopInternal()){
            Log.info("Task aborted: "+this);
            status = STATUS.ABORTED;
            return true;
        }
        return false;
    }
    protected void abort(){
        this.status = STATUS.ABORTED;
        isNotRunning.increase();
    }
    public STATUS waitFinished() throws InterruptedException {
        isNotRunning.waitOnce();
        return status;
    }

    public Object getResult() {
        return result;
    }

    public Exception getError() {
        return error;
    }
    public String getStatusString(){
        return this.status.toString();
    }

    public boolean isFinished(){
        return STATUS.FINISHED.equals(status);
    }

    protected abstract boolean canPause();
    protected abstract boolean canStop();
    protected abstract boolean pauseInternal();
    protected abstract boolean stopInternal();

    public void setListener(UpdateListener listener) {
        this.listener = listener;
    }

    void startSameThread(){
        start(true);
    }

    @Override
    public String toString(){
        return this.getClass().getSimpleName()+" -> status: "+this.status;
    }



}
