package com.xiaomitool.v2.adb.device;

import com.xiaomitool.v2.inet.CustomHttpException;
import com.xiaomitool.v2.xiaomi.XiaomiProcedureException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.xiaomitool.v2.xiaomi.romota.MiuiRomOta.deviceNames_request;

public class DeviceGroups {
    private static final HashSet<String> ALWAYS_UNLOCKED_DEVICES = getAlwaysUnlockedSet();
    private static final HashSet<String> EEA_REGION_DEVICES = getEeaRegionDevices();
    private static final HashSet<String> ANDROID_ONE_DEVICES = getAndroidOneDevices();
    private static final HashSet<String> RECOVERY_SAFE = getRecoverySafeDevices();
    private static final HashSet<String> BIREGION_DEVICES = getBiRegionDevices();
    private static final HashSet<String> ADD_EEA_REGION_DEVICES = new HashSet<>();
    private static final Pattern CODENAME_STRIPPER = Pattern.compile("^([a-z]+(_xhdpi|_sprout)?)((_[a-z]+)*_global)*$", Pattern.CASE_INSENSITIVE);

    public static void init() throws XiaomiProcedureException, CustomHttpException {
        JSONObject obj = deviceNames_request();
        for (String name : obj.keySet()) {
            if (name.contains("_eea")) {
                ADD_EEA_REGION_DEVICES.add(DeviceGroups.stripCodename(name));
            }
        }
    }

    public static boolean isMultiRegionDevice(String codename) {
        codename = stripCodename(codename);
        return !BIREGION_DEVICES.contains(codename);
    }

    public static boolean isSafeToChangeRecoveryLocked(String codename) {
        codename = stripCodename(codename);
        for (String c : RECOVERY_SAFE) {
            if (codename.startsWith(c)) {
                return true;
            }
        }
        return false;
    }

    private static HashSet<String> getBiRegionDevices() {
        return hashSet(
                "perseus",
                "prada",
                "dior",
                "nikel",
                "mocha",
                "lotus",
                "aries",
                "hydrogen",
                "vince",
                "aqua",
                "lithium",
                "mido",
                "ugg",
                "taurus",
                "leo",
                "riva",
                "ugglite",
                "beryllium",
                "hermes",
                "kenzo",
                "tiffany",
                "oxygen",
                "ursa",
                "clover",
                "ferrari",
                "scorpio",
                "omega",
                "gemini",
                "capricorn",
                "ido",
                "rolex",
                "gucci",
                "natrium",
                "sagit",
                "latte",
                "sirius",
                "hennessy",
                "tissot",
                "libra",
                "land",
                "hammerhead",
                "wayne",
                "santoni",
                "equuleus",
                "nitrogen",
                "markw",
                "cancro",
                "pisces",
                "helium",
                "virgo",
                "cappu",
                "chiron",
                "jason",
                "mione",
                "meri",
                "armani"
        );
    }

    private static HashSet<String> getRecoverySafeDevices() {
        return hashSet(
                "prada",
                "dior",
                "nikel",
                "mocha",
                "lotus",
                "aries",
                "hydrogen",
                "aqua",
                "lithium",
                "mido",
                "taurus",
                "leo",
                "hermes",
                "kenzo",
                "tiffany",
                "oxygen",
                "ferrari",
                "scorpio",
                "omega",
                "gemini",
                "capricorn",
                "ido",
                "rolex",
                "gucci",
                "natrium",
                "sagit",
                "latte",
                "hennessy",
                "tissot",
                "libra",
                "land",
                "hammerhead",
                "santoni",
                "markw",
                "cancro",
                "pisces",
                "helium",
                "virgo",
                "cappu",
                "chiron",
                "jason",
                "mione",
                "meri",
                "armani"
        );
    }

    private static HashSet<String> getAndroidOneDevices() {
        HashSet<String> set = new HashSet<>();
        set.add("tiare");
        set.add("tissot_sprout");
        set.add("jasmine_sprout");
        return set;
    }

    private static HashSet<String> getEeaRegionDevices() {
        HashSet<String> set = new HashSet<>();
        set.add("cepheus");
        return set;
    }

    private static HashSet<String> hashSet(String... entries) {
        return new HashSet<>(Arrays.asList(entries));
    }

    private static HashSet<String> getAlwaysUnlockedSet() {
        return hashSet(
                "mione_plus",
                "aries",
                "taurus",
                "pisces",
                "HM2013022",
                "HM2013023",
                "cancro",
                "armani",
                "lcsh92_wet_tdd",
                "lcsh92_wet_jb9",
                "HM2014011",
                "mocha",
                "hammerhead",
                "dior",
                "HM2014501",
                "lte26007",
                "virgo",
                "wt86047",
                "wt88047",
                "gucci",
                "ferrari",
                "hermes",
                "latte"
        );
    }

    private static String stripCodenamePro(String codename) {
        if (codename == null) {
            return null;
        }
        codename = codename.replace("_alpha", "");
        Matcher matcher = CODENAME_STRIPPER.matcher(codename);
        if (!matcher.matches()) {
            return null;
        }
        return matcher.group(1);
    }

    public static String stripCodename(String codename) {
        if (codename == null) {
            return null;
        }
        String cn = stripCodenamePro(codename);
        if (cn != null) {
            return cn;
        }
        return codename.replace("_eea_global", "").replace("_ru_global", "").replace("_india_global", "").replace("_global", "").replace("_alpha", "");
    }

    public static boolean hasUnlockedBootloader(String codename) {
        if (codename == null) {
            return false;
        }
        return ALWAYS_UNLOCKED_DEVICES.contains(stripCodename(codename));
    }

    public static boolean hasEEARegion(String codename) {
        if (codename == null) {
            return false;
        }
        codename = stripCodename(codename);
        return EEA_REGION_DEVICES.contains(codename) || ADD_EEA_REGION_DEVICES.contains(codename);
    }

    public static boolean isAndroidOneDevice(String codename) {
        if (codename == null) {
            return false;
        }
        codename = stripCodename(codename);
        return codename.contains("_sprout") || ANDROID_ONE_DEVICES.contains(codename);
    }
}
