package com.xiaomitool.v2.adb.device;

import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.utility.RunnableWithArg;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Properties {
    private int failedParsingAttempts = 0;
    private final ConcurrentHashMap<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();
    private RunnableWithArg onFailedAttemptThree = null;
    private boolean parsed = false, failed = false;

    public boolean isFailed() {
        return !parsed && failed;
    }

    public boolean parse(){
        return parse(false);
    }
    public boolean parse(boolean force){

        boolean res = parse(force, true);
        Log.debug("Parsing finished");
        return res;
    }
    public boolean parse(boolean force, boolean internal){
        synchronized (propertiesMap) {
            Log.debug("Starting parsing");
            if (parsed && !force) {
                return true;
            }
            parsed = true;
            if (parseInternal()) {
                Log.debug(this.toString());
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
}
