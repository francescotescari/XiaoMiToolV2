package com.xiaomitool.v2.adb.device;

import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.utility.RunnableWithArg;
import com.xiaomitool.v2.utility.WaitSemaphore;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Properties {
    private final ConcurrentHashMap<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();
    private final WaitSemaphore parsingSemaphore;
    private int failedParsingAttempts = 0;
    private RunnableWithArg onFailedAttemptThree = null;
    private boolean parsed = false, failed = false;
    private Instant lastFailIntant = null;

    public Properties() {
        parsingSemaphore = new WaitSemaphore(1);
    }

    public boolean isFailed() {
        return !parsed && failed;
    }

    public boolean parse() {
        return parse(false);
    }

    public boolean parse(boolean force) {
        boolean res = false;
        synchronized (parsingSemaphore) {
            try {
                parsingSemaphore.decrease();
                parsingSemaphore.setPermits(0);
                res = parse(force, true);
            } catch (Exception e) {
                Log.error("Failed waiting for parsing");
            } finally {
                parsingSemaphore.setPermits(1);
            }
        }
        return res;
    }

    public boolean parse(boolean force, boolean internal) {
        if (parsed && !force) {
            return true;
        }
        parsed = true;
        if (failed && lastFailIntant != null && failedParsingAttempts > 3) {
            if (Duration.between(lastFailIntant, Instant.now()).getSeconds() < 10) {
                Log.warn("Parsing failed more than three times less than 10 seconds ago, waiting");
                return false;
            }
        }
        if (parseInternal()) {
            Log.info("Properties parsed");
            failedParsingAttempts = 0;
            return true;
        }
        lastFailIntant = Instant.now();
        failed = true;
        ++failedParsingAttempts;
        Log.warn("Failed to parse properties: " + this.toString() + ", attempt: " + failedParsingAttempts);
        if (failedParsingAttempts > 3) {
            Log.warn("Failed to parse properties for three times");
            if (onFailedAttemptThree != null) {
                onFailedAttemptThree.run(this);
            }
        }
        parsed = false;
        return false;
    }

    public void setOnFailedAttemptThree(RunnableWithArg onFailedAttemptThree) {
        this.onFailedAttemptThree = onFailedAttemptThree;
    }

    public boolean isParsed() {
        return parsed;
    }

    protected abstract boolean parseInternal();

    public synchronized Object put(String key, Object value) {
        synchronized (propertiesMap) {
            return propertiesMap.put(key, value);
        }
    }

    public synchronized Object get(String key) {
        synchronized (propertiesMap) {
            return propertiesMap.get(key);
        }
    }

    public synchronized Object get(String key, Object defaultObject) {
        synchronized (propertiesMap) {
            return propertiesMap.getOrDefault(key, defaultObject);
        }
    }

    public synchronized void putAll(Map<String, ?> map) {
        synchronized (propertiesMap) {
            propertiesMap.putAll(map);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : propertiesMap.entrySet()) {
            builder.append(entry.getKey()).append(" -> ").append(entry.getValue()).append(System.lineSeparator());
        }
        return builder.toString();
    }

    public synchronized void reset() {
        synchronized (propertiesMap) {
            this.parsed = false;
        }
    }

    public void set(String key, Object value) {
        this.propertiesMap.put(key, value);
    }
}
