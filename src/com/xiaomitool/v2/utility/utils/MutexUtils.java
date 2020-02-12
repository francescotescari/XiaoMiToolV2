package com.xiaomitool.v2.utility.utils;

import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.resources.ResourcesManager;


import java.io.IOException;
import java.net.ServerSocket;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class MutexUtils {

    private static final int SOCKET_PORT = 53435;
    private static final String MUTEX_FILENAME = ".instance.lock";
    private static final Path MUTEX_PATH = ResourcesManager.getSystemTmpPath().resolve(MUTEX_FILENAME);
    private static boolean fileLocked = false, socketLocked = false;
    private static FileLock lock;
    private static ServerSocket serverSocket;

    public static boolean lock(){
        boolean lockFile = lockFile();
        boolean lockSocket = lockSocket();
        return lockFile || lockSocket;
    }

    private static boolean lockSocket(){
        if (socketLocked){
            return false;
        }
        boolean result = true;
        try {
            serverSocket = new ServerSocket(SOCKET_PORT);
            socketLocked = true;
        } catch (Throwable e) {
            Log.error("Failed to create mutex socket");
            serverSocket = null;
           result = false;
        }
        return result;
    }



    private static boolean lockFile(){
        try {
            if (fileLocked) {
                return false;
            }
            if (!Files.exists(MUTEX_PATH)) {
                try {
                    Files.createFile(MUTEX_PATH);
                } catch (IOException e) {
                    Log.error("Failed to create mutex file: "+e.getMessage());
                    return true;
                }
                try {
                    Files.setAttribute(MUTEX_PATH, "dos:hidden", true);
                } catch (IOException e) {
                    Log.warn("Failed to hide mutex file: "+e.getMessage());
                }
            }
            FileChannel fileChannel;
            try {
                fileChannel = FileChannel.open(MUTEX_PATH, StandardOpenOption.WRITE);
            } catch (IOException e) {
                Log.error("Failed to open mutex file: "+e.getMessage());
                return false;
            }

            try {
                lock = fileChannel.tryLock();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (lock == null) {
                Log.warn("Mutex not available");
                return false;
            }
            fileLocked = true;
            return true;
        } catch (Throwable t){
            Log.error("Failed to lock mutex: unmanaged exception: "+t.getMessage());
            return false;
        }
    }

    private static boolean unlockFile(){
        if (!fileLocked || lock == null){
            return false;
        }
        try {
            lock.release();
        } catch (IOException e) {
            Log.error("Failed to release mutex file lock");
            return false;
        }
        fileLocked = false;
        lock = null;
        return true;
    }

    private static boolean unlockSocket(){
        if (!socketLocked || serverSocket == null){
            return false;
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            /*Log.debug("Failed to close server socket");*/
            return false;
        }
        return true;
    }

    public static boolean unlock(){
        boolean unlockFile = unlockFile();
        boolean unlockSocket = unlockSocket();
        return unlockFile || unlockSocket;

    }

    public static boolean waitUnlock(int seconds) throws InterruptedException {
        boolean res = false;
        for (int i = 0; i<seconds; ++i){
            if (!lock()){
                Thread.sleep(1000);
            } else {
                res = true;
                break;
            }
        }
        return res || unlock();
    }
}
