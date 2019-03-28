package com.xiaomitool.v2.xiaomi;


import com.xiaomitool.v2.adb.device.DeviceGroups;
import com.xiaomitool.v2.utility.NotNull;

public class XiaomiUtilities {
    public static String findJsonStart(String data){
        byte[] d = data.getBytes();
        for (int i = 0; i<d.length; ++i){
            if (d[i] == '{'){
                return data.substring(i);
            }
        }
        return null;
    }

    public static boolean isFastbootFile(String fn){
        if (fn == null){
            return false;
        }
        fn = fn.toLowerCase();
        return fn.endsWith(".tgz") || fn.endsWith(".tar.gz");
    }
    public static String snToString(Integer serialNumber){
        return String.format("0x%08x",serialNumber);
    }
    public static String stripCodename(String codename){
        return DeviceGroups.stripCodename(codename);
    }
    
    public static  String deviceToXiaomiEuName(@NotNull String codename) {
        codename = DeviceGroups.stripCodename(codename);
        if (codename.equalsIgnoreCase("aries")) {
            codename = "MI2";
        } else if (codename.equalsIgnoreCase("aqua")) {
            codename = "MI4s";
        } else if (codename.equalsIgnoreCase("armani")) {
            codename = "HM1SWC";
        } else if (codename.equalsIgnoreCase("cancro")) {
            codename = "MI3WMI4W";
        } else if (codename.equalsIgnoreCase("capricorn")) {
            codename = "MI5S";
        } else if (codename.equalsIgnoreCase("dior")) {
            codename = "HMNoteLTE";
        } else if (codename.equalsIgnoreCase("ferrari")) {
            codename = "MI4i";
        } else if (codename.equalsIgnoreCase("gemini")) {
            codename = "MI5";
        } else if (codename.equalsIgnoreCase("gucci")) {
            codename = "HMNote1S";
        } else if (codename.equalsIgnoreCase("helium")) {
            codename = "MIMAXPro";
        } else if (codename.equalsIgnoreCase("hennessy")) {
            codename = "HMNote3";
        } else if (codename.equalsIgnoreCase("hydrogen")) {
            codename = "MIMAX";
        } else if (codename.equalsIgnoreCase("hermes")) {
            codename = "HMNote2";
        } else if (codename.equalsIgnoreCase("ido")) {
            codename = "HM3";
        } else if (codename.equalsIgnoreCase("kate")) {
            codename = "HMNote3SE";
        } else if (codename.equalsIgnoreCase("kenzo")) {
            codename = "HMNote3Pro";
        } else if (codename.equalsIgnoreCase("lcsh92_wet_jb9")) {
            codename = "HMNoteW";
        } else if (codename.equalsIgnoreCase("latte")) {
            codename = "MIPAD2";
        } else if (codename.equalsIgnoreCase("libra")) {
            codename = "MI4c";
        } else if (codename.equalsIgnoreCase("land")) {
            codename = "HM3SHM3X";
        } else if (codename.equalsIgnoreCase("leo")) {
            codename = "MINotePro";
        } else if (codename.equalsIgnoreCase("mido")) {
            codename = "HMNote4X";
        } else if (codename.equalsIgnoreCase("mocha")) {
            codename = "MIPAD";
        } else if (codename.equalsIgnoreCase("natrium")) {
            codename = "MI5SPlus";
        } else if (codename.equalsIgnoreCase("nikel")) {
            codename = "HMNote4";
        } else if (codename.equalsIgnoreCase("omega")) {
            codename = "HMPro";
        } else if (codename.equalsIgnoreCase("scorpio")) {
            codename = "MINote2";
        } else if (codename.equalsIgnoreCase("lithium")) {
            codename = "MIMix";
        } else if (codename.equalsIgnoreCase("virgo")) {
            codename = "MINote";
        } else if (codename.equalsIgnoreCase("wt88047")) {
            codename = "HM2XWCPro";
        } else if (codename.equalsIgnoreCase("wt86047")) {
            codename = "HM2XTDPro";
        } else if (codename.equalsIgnoreCase("HM2014811")) {
            codename = "HM2XWC";
        } else if (codename.equalsIgnoreCase("HM2014813")) {
            codename = "HM2XTD";
        } else if (codename.equalsIgnoreCase("prada")) {
            codename = "HM4";
        } else if (codename.equalsIgnoreCase("rolex")) {
            codename = "HM4A";
        } else if (codename.equalsIgnoreCase("markw")) {
            codename = "HM4Pro";
        } else if (codename.equalsIgnoreCase("santoni")) {
            codename = "HM4X";
        } else if (codename.equalsIgnoreCase("meri")) {
            codename = "MI5c";
        } else if (codename.equalsIgnoreCase("song")) {
            codename = "MI5c";
        } else if (codename.equalsIgnoreCase("sagit")) {
            codename = "MI6";
        } else if (codename.equalsIgnoreCase("cappu")) {
            codename = "MIPAD3";
        } else if (codename.equalsIgnoreCase("jason")) {
            codename = "MINote3";
        } else if (codename.equalsIgnoreCase("oxygen")) {
            codename = "MIMAX2";
        } else if (codename.equalsIgnoreCase("tiffany")) {
            codename = "MI5X";
        } else if (codename.equalsIgnoreCase("ugg")) {
            codename = "HMNote5A";
        } else if (codename.equalsIgnoreCase("ugglite")) {
            codename = "HMNote5ALITE";
        } else if (codename.equalsIgnoreCase("chiron")) {
            codename = "MIMix2";
        } else if (codename.equalsIgnoreCase("riva")) {
            codename = "HM5A";
        } else if (codename.equalsIgnoreCase("rosy")) {
            codename = "HM5";
        } else if (codename.equalsIgnoreCase("vince")) {
            codename = "HM5Plus";
        } else if (codename.equalsIgnoreCase("whyred")) {
            codename = "HMNote5Pro";
        } else if (codename.equalsIgnoreCase("polaris")) {
            codename = "MIMix2S";
        } else if (codename.equalsIgnoreCase("wayne")) {
            codename = "MI6X";
        } else if (codename.equalsIgnoreCase("sirius")) {
            codename = "MI8SE";
        } else if (codename.equalsIgnoreCase("dipper")) {
            codename = "MI8";
        } else if (codename.equalsIgnoreCase("cactus")) {
            codename = "HM6A";
        } else if (codename.equalsIgnoreCase("cereus")) {
            codename = "HM6";
        } else if (codename.equalsIgnoreCase("sakura")) {
            codename = "HM6Pro";
        } else if (codename.equalsIgnoreCase("ursa")) {
            codename = "MI8Explorer";
        } else if (codename.equalsIgnoreCase("beryllium")) {
            codename = "POCOF1";
        } else if (codename.equalsIgnoreCase("clover")) {
            codename = "MIPAD4";
        } else if (codename.equalsIgnoreCase("perseus")) {
            codename = "MIMix3";
        } else if (codename.equalsIgnoreCase("platina")) {
            codename = "MI8LITE";
        } else if (codename.equalsIgnoreCase("tulip")) {
            codename = "HMNote6Pro";
        } else if (codename.equalsIgnoreCase("equuleus")) {
            codename = "MI8Pro";
        } else if (codename.equalsIgnoreCase("lavender")) {
            codename = "HMNote7";
        } else if (codename.equalsIgnoreCase("cepheus")) {
            codename = "MI9";
        } else if (codename.equalsIgnoreCase("grus")) {
            codename = "MI9SE";
        } else if (codename.equalsIgnoreCase("onclite")) {
            codename = "HM7";
        } else if (codename.equalsIgnoreCase("violet")) {
            codename = "HMNote7Pro";
        }
        return codename;
    }

}
