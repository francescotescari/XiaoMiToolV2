package com.xiaomitool.v2.tasks;



import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.utility.utils.StrUtils;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;

public  class AdvancedUpdateListener extends UpdateListener {
    public static String[] UNITS = new String[]{" ", "k","M","G","T"};
    public AdvancedUpdateListener(int updateEveryXMillis){
        this(updateEveryXMillis,null);
    }
    public AdvancedUpdateListener(int updateEveryXMillis, OnAdvancedUpdate onAdvancedUpdate){
        updateTime = updateEveryXMillis;
        super.addOnUpdate((downloaded, totalSize, latestDuration, totalDuration) -> {
            long downloadedNow = downloaded-latestDownloaded;
            latestDownloaded = downloaded;
            cacheDuration = cacheDuration.plus(latestDuration);
            cacheBytes = cacheBytes+downloadedNow;
            if (cacheDuration.toMillis() > updateTime){
                cacheSpeed = new DownloadSpeed(cacheBytes, cacheDuration);
                cacheDuration = Duration.ZERO;
                cacheBytes = 0;
                DownloadSpeed average = new DownloadSpeed(downloaded, totalDuration);
                TimeRemaining remaining = null;
                if (totalSize > 0){
                    remaining = new TimeRemaining(Duration.ofMillis((long) ((totalSize-downloaded)/(0.92*average.getDouble()+0.08*cacheSpeed.getDouble()+1)*1000)));
                }
                for (OnAdvancedUpdate toRun : onAdvancedUpdates){
                    toRun.run(downloaded, totalSize, cacheSpeed, average, remaining);
                }
            }
        });
        addOnAdvancedUpdate(onAdvancedUpdate);
    }
    private int updateTime;
    private long latestDownloaded = 0;
    private Duration cacheDuration = Duration.ZERO;
    private long cacheBytes = 0;
    private DownloadSpeed cacheSpeed = new DownloadSpeed(0);
    private List<OnAdvancedUpdate> onAdvancedUpdates = new ArrayList<>();


    public static class DownloadSpeed {

        double speed = 0;
        public DownloadSpeed(long downloaded, Duration duration){
            long millis = duration.toMillis();
            if (downloaded == 0 || millis < 10){
                speed = 0;
            }
            speed = (downloaded*1000/(double) millis);
            Log.debug("SPEED: "+speed+"; DOW: "+downloaded+"; MILLIS: "+millis);
        }
        public DownloadSpeed(double speed){
            this.speed = speed;
        }
        public double getDouble(){
            return speed;
        }

        @Override
        public String toString() {
            return StrUtils.bytesToString(this.speed)+"/s";
        }
    }

    public static class TimeRemaining {
        Duration duration;
        TemporalUnit unit;
        long quantity;
        public TimeRemaining(Duration duration){
            this.duration = duration;
            TemporalUnit[] units = new TemporalUnit[]{ChronoUnit.SECONDS, ChronoUnit.MINUTES, ChronoUnit.HOURS};
            int i = 0;
            long q = duration.getSeconds();
            while (q >= 60 && i < units.length-1){
                q /= 60;
                ++i;
            }
            if (i >= units.length){
                quantity = duration.getSeconds();
                unit = ChronoUnit.SECONDS;
            } else {
                unit = units[i];
                quantity = q;
            }
        }

        public TemporalUnit getUnit(){
            return unit;
        }

        public long getQuantity() {
            return quantity;
        }

        public Duration getDuration() {
            return duration;
        }
    }
    public static interface OnAdvancedUpdate {
        public void run(long downloaded, long totalSize, DownloadSpeed currentSpeed, DownloadSpeed averageSpeed, TimeRemaining missingTime);
    }
    public void addOnAdvancedUpdate(OnAdvancedUpdate onAdvancedUpdate){
        if (onAdvancedUpdate == null){
            return;
        }
        this.onAdvancedUpdates.add(onAdvancedUpdate);
    }




}
