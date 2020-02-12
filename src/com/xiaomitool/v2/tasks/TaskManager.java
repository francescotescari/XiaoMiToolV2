package com.xiaomitool.v2.tasks;

import com.xiaomitool.v2.logging.Log;

import java.util.HashSet;

public class TaskManager {
    private HashSet<Task> taskSet = new HashSet<>();
    private static TaskManager instance = new TaskManager();
    public static TaskManager getInstance() {
        return instance;
    }

    public void  start(Task task){
        start(task,false);
    }
    public void start(Task task, boolean sameThread){
        /*Log.debug("Starting task, sameThread: "+sameThread);*/
        try {
            if (sameThread) {
                task.startSameThread();
            } else {
                task.start();
            }
        } catch (Exception t){
            Log.warn("Failed to start task: "+t.getMessage());
            Log.printStackTrace(t);
            UpdateListener listener = task.getListener();
            if (listener != null){
                listener.onError(t);
            }
        }
        /*Log.debug("Start task finished");*/
    }
    public void restart(Task task) throws InterruptedException {task.restart();}
    public void startSameThread(Task task){
        start(task, true);
    }
}
