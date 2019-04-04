package com.xiaomitool.v2.adb.device;

import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.utility.RunnableWithArg;
import com.xiaomitool.v2.utility.WaitSemaphore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public abstract class Properties {
    private int failedParsingAttempts = 0;
    private final ConcurrentHashMap<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();
    private final WaitSemaphore parsingSemaphore;
    private RunnableWithArg onFailedAttemptThree = null;
    private boolean parsed = false, failed = false;

    public Properties(){
        parsingSemaphore = new WaitSemaphore(1);
        Log.debug("Properties created: "+this.getClass().getSimpleName()+"::"+this.hashCode()+" -> sem = "+parsingSemaphore.toString());
    }

    public boolean isFailed() {
        return !parsed && failed;
    }

    public boolean parse(){
        return parse(false);
    }
    public boolean parse(boolean force){
        boolean res =false;
        synchronized (parsingSemaphore) {
            Log.debug(this.hashCode());
            try {
                //Log.debug("Parsing semaphore permits A");
                Log.debug(parsingSemaphore.getPermitNumber());
                parsingSemaphore.decrease();
                parsingSemaphore.setPermits(0);
                //Log.debug("Parsing semaphore permits B");
                Log.debug(parsingSemaphore.getPermitNumber());
                res = parse(force, true);
                //Log.debug("Parsing finished");
            } catch (Exception e) {
                Log.error("Failed waiting for parsing");
            } finally {
                parsingSemaphore.setPermits(1);
            }
        }
        return res;


    }
    public boolean parse(boolean force, boolean internal){

        synchronized (propertiesMap) {
            Log.debug("Starting parsing: force: "+force+", class: "+this.getClass().getSimpleName());
            if (parsed && !force) {
                Log.debug("Already parsed, returning");
                return true;

            }

            parsed = true;
            Log.debug("Parsing should be disabled");

            if (parseInternal()) {
                Log.debug(this.toString());
                Log.info("Properties parsed");
                //Log.info(this.propertiesMap);
                failedParsingAttempts = 0;
                return true;
            }
            failed = true;
            ++failedParsingAttempts;
            if (failedParsingAttempts > 3) {
                Log.warn("Failed to parse properties for three times");
                if (onFailedAttemptThree != null) {
                    onFailedAttemptThree.run(this);

                }
                //failedParsingAttempts = 0;
            }
            parsed = false;
            return false;
        }
    }

    public void setOnFailedAttemptThree(RunnableWithArg onFailedAttemptThree) {
        this.onFailedAttemptThree = onFailedAttemptThree;
    }

    public boolean isParsed() {
        return parsed;
    }

    protected abstract boolean parseInternal();
    public synchronized Object put(String key, Object value){
        synchronized (propertiesMap) {
            return propertiesMap.put(key, value);
        }
    }
    public synchronized Object get(String key){
        synchronized (propertiesMap) {
            return propertiesMap.get(key);
        }
    }
    public synchronized Object get(String key, Object defaultObject){
        synchronized (propertiesMap) {
            return propertiesMap.getOrDefault(key, defaultObject);
        }
    }
    public synchronized void putAll(Map<String, ?> map){
        synchronized (propertiesMap) {
            propertiesMap.putAll(map);
        }
    }
    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : propertiesMap.entrySet()){
            builder.append(entry.getKey()).append(" -> ").append(entry.getValue()).append(System.lineSeparator());
        }
        return builder.toString();
    }

    public synchronized void reset() {
        synchronized (propertiesMap) {
            this.parsed = false;
        }
    }
}
