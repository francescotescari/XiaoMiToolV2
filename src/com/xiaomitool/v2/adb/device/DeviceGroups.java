package com.xiaomitool.v2.adb.device;

import java.util.HashSet;

public class DeviceGroups {
    private static final HashSet<String> ALWAYS_UNLOCKED_DEVICES = getAlwaysUnlockedSet();
    private static final HashSet<String> EEA_REGION_DEVICES = getEeaRegionDevices();
    private static final HashSet<String> ANDROID_ONE_DEVICES = getAndroidOneDevices();
    private static final HashSet<String> getAndroidOneDevices(){
        HashSet<String> set = new HashSet<>();
        set.add("tiare");
        return set;
    }
    private static HashSet<String> getEeaRegionDevices(){
        HashSet<String> set = new HashSet<>();
        set.add("cepheus");
        return set;
    }

    private static HashSet<String> getAlwaysUnlockedSet(){
        HashSet<String> set = new HashSet<>();
        set.add("mione_plus");
        set.add("aries");
        set.add("taurus");
        set.add("pisces");
        set.add("HM2013022");
        set.add("HM2013023");
        set.add("cancro");
        set.add("armani");
        set.add("lcsh92_wet_tdd");
        set.add("lcsh92_wet_jb9");
        set.add("HM2014011");
        set.add("mocha");
        set.add("hammerhead");
        set.add("dior");
        set.add("HM2014501");
        set.add("lte26007");
        set.add("virgo");
        set.add("wt86047");
        set.add("wt88047");
        set.add("gucci");
        set.add("ferrari");
        set.add("leo");
        set.add("hermes");
        set.add("latte");
        return set;

    }
    private static String stripCode(String codename){
        if (codename == null){
            return "";
        }
        return codename.replace("_global","").replace("_eea","").replace("_alpha","").replace("_ru","");
    }

    public static boolean hasUnlockedBootloader(String codename){
        if (codename == null){
            return false;
        }
        return ALWAYS_UNLOCKED_DEVICES.contains(stripCode(codename));
    }

    public static boolean hasEEARegion(String codename){
        if (codename == null){
            return false;
        }
        return EEA_REGION_DEVICES.contains(stripCode(codename));
    }

    public static boolean isAndroidOneDevice(String codename){
        if (codename == null){
            return false;
        }
        codename = stripCode(codename);
        return codename.contains("_sprout") || ANDROID_ONE_DEVICES.contains(codename);
    }
}
