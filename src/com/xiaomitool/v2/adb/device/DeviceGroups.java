package com.xiaomitool.v2.adb.device;

import com.xiaomitool.v2.logging.Log;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeviceGroups {
    private static final HashSet<String> ALWAYS_UNLOCKED_DEVICES = getAlwaysUnlockedSet();
    private static final HashSet<String> EEA_REGION_DEVICES = getEeaRegionDevices();
    private static final HashSet<String> ANDROID_ONE_DEVICES = getAndroidOneDevices();
    private static final HashSet<String> RECOVERY_SAFE = removeUnsafeDevices(getCurrentDevices());
    private static final HashSet<String> BIREGION_DEVICES = removeMultiRegionDevice(getCurrentDevices());

    public static boolean isMultiRegionDevice(String codename){
        codename = stripCodename(codename);
        return !BIREGION_DEVICES.contains(codename);
    }

    public static boolean isSafeToChangeRecoveryLocked(String codename){
        codename = stripCodename(codename);
        for (String c : RECOVERY_SAFE){
            if (codename.startsWith(c)){
                return true;
            }
        }
        return false;
    }

    private static final HashSet<String> removeMultiRegionDevice(HashSet<String> toRemove){
        toRemove.remove("cepheus");
        toRemove.remove("onc");
        toRemove.remove("onclite");
        toRemove.remove("lavender");
        toRemove.remove("grus");
        toRemove.remove("violet");
        toRemove.remove("davinci");
        toRemove.remove("raphael");
        toRemove.remove("dipper");
        toRemove.remove("platina");
        toRemove.remove("polaris");
        toRemove.remove("ysl");
        toRemove.remove("whyred");
        toRemove.remove("rosy");
        toRemove.remove("cactus");
        toRemove.remove("cereus");
        toRemove.remove("sakura");
        return toRemove;
    }

    private static final HashSet<String> removeUnsafeDevices(HashSet<String> toRemove){
        toRemove.remove("ugg");
        toRemove.remove("ugglite");
        toRemove.remove("rosy");
        toRemove.remove("riva");
        toRemove.remove("vince");
        toRemove.remove("whyred");
        toRemove.remove("versace");
        toRemove.remove("wayne");
        toRemove.remove("ysl");
        toRemove.remove("sirius");
        toRemove.remove("polaris");
        toRemove.remove("sakura");
        toRemove.remove("sakura_india");
        toRemove.remove("ursa");
        toRemove.remove("beryllium");
        toRemove.remove("comet");
        toRemove.remove("clover");
        toRemove.remove("cactus");
        toRemove.remove("cereus");
        toRemove.remove("nitrogen");
        toRemove.remove("dipper");
        toRemove.remove("tulip");
        toRemove.remove("platina");
        toRemove.remove("lilium");
        toRemove.remove("equuleus");
        toRemove.remove("perseus");
        toRemove.remove("cepheus");
        toRemove.remove("onc");
        toRemove.remove("onclite");
        toRemove.remove("lavender");
        toRemove.remove("grus");
        toRemove.remove("violet");
        toRemove.remove("davinci");
        toRemove.remove("raphael");
        return toRemove;
    }

    private static final HashSet<String> getCurrentDevices(){
        HashSet<String> set = new HashSet<>();
        set.add("mione");
        set.add("aries");
        set.add("taurus");
        set.add("pisces");
        set.add("cancro");
        set.add("armani");
        set.add("mocha");
        set.add("hammerhead");
        set.add("dior");
        set.add("virgo");
        set.add("gucci");
        set.add("ferrari");
        set.add("leo");
        set.add("hermes");
        set.add("libra");
        set.add("latte");
        set.add("ido");
        set.add("hennessy");
        set.add("aqua");
        set.add("gemini");
        set.add("kenzo");
        set.add("capricorn");
        set.add("scorpio");
        set.add("hydrogen");
        set.add("land");
        set.add("omega");
        set.add("markw");
        set.add("nikel");
        set.add("natrium");
        set.add("lithium");
        set.add("helium");
        set.add("prada");
        set.add("mido");
        set.add("rolex");
        set.add("meri");
        set.add("sagit");
        set.add("santoni");
        set.add("cappu");
        set.add("oxygen");
        set.add("tiffany");
        set.add("jason");
        set.add("ugglite");
        set.add("ugg");
        set.add("tissot");
        set.add("chiron");
        set.add("riva");
        set.add("vince");
        set.add("rosy");
        set.add("dipper");
        set.add("whyred");
        set.add("polaris");
        set.add("wayne");
        set.add("nitrogen");
        set.add("ysl");
        set.add("sirius");
        set.add("sakura");
        set.add("cactus");
        set.add("cereus");
        set.add("beryllium");
        set.add("clover");
        set.add("ursa");
        set.add("platina");
        set.add("perseus");
        set.add("equuleus");
        set.add("cepheus");
        set.add("lotus");
        set.add("lavender");
        set.add("grus");
        return set;
    }

    private static final HashSet<String> getAndroidOneDevices(){
        HashSet<String> set = new HashSet<>();
        set.add("tiare");
        set.add("tissot_sprout");
        set.add("jasmine_sprout");
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
    private static final Pattern CODENAME_STRIPPER = Pattern.compile("^([a-z]+(_xhdpi|_sprout)?)((_[a-z]+)*_global)*$", Pattern.CASE_INSENSITIVE);
    private static String stripCodenamePro(String codename){
        if (codename == null){
            return null;
        }
        codename = codename.replace("_alpha","");
        Matcher matcher =  CODENAME_STRIPPER.matcher(codename);
        if (!matcher.matches()){
            return null;
        }
        return matcher.group(1);
    }

    public static String stripCodename(String codename){
        if (codename == null){
            return null;
        }
        String cn = stripCodenamePro(codename);
        if (cn != null){
            return cn;
        }
        return codename.replace("_eea_global","").replace("_ru_global","").replace("_india_global","").replace("_global","").replace("_alpha","");
    }

    public static boolean hasUnlockedBootloader(String codename){
        if (codename == null){
            return false;
        }
        return ALWAYS_UNLOCKED_DEVICES.contains(stripCodename(codename));
    }

    public static boolean hasEEARegion(String codename){
        if (codename == null){
            return false;
        }
        return EEA_REGION_DEVICES.contains(stripCodename(codename));
    }

    public static boolean isAndroidOneDevice(String codename){
        if (codename == null){
            return false;
        }
        codename = stripCodename(codename);
        return codename.contains("_sprout") || ANDROID_ONE_DEVICES.contains(codename);
    }
}
