package com.xiaomitool.v2.tasks;

import com.xiaomitool.v2.logging.Log;

import java.util.HashSet;

public class TaskManager {
    private static TaskManager instance = new TaskManager();
    private HashSet<Task> taskSet = new HashSet<>();

    public static TaskManager getInstance() {
        return instance;
    }

    public void start(Task task) {
        start(task, false);
    }

    public void start(Task task, boolean sameThread) {
        try {
            if (sameThread) {
                task.startSameThread();
            } else {
                task.start();
            }
        } catch (Exception t) {
            Log.warn("Failed to start task: " + t.getMessage());
            Log.printStackTrace(t);
            UpdateListener listener = task.getListener();
            if (listener != null) {
                listener.onError(t);
            }
        }
    }

    public void restart(Task task) throws InterruptedException {
        task.restart();
    }

    public void startSameThread(Task task) {
        start(task, true);
    }
}
