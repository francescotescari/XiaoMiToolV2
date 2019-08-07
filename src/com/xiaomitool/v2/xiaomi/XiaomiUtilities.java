package com.xiaomitool.v2.xiaomi;


import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.adb.device.DeviceGroups;
import com.xiaomitool.v2.utility.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class XiaomiUtilities {
    public static String findJsonStart(String data){
        char[] d = data.toCharArray();
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

    private static final Pattern P_IS_REDMI = Pattern.compile("^((xiao)?mi)?\\s*redmi(\\b)",Pattern.CASE_INSENSITIVE);
    private static final Pattern P_IS_POCO = Pattern.compile("^((xiao)?mi)?\\s*poco(\\b)",Pattern.CASE_INSENSITIVE);
    private static final Pattern P_IS_MI = Pattern.compile("^((xiao)?mi)?\\s*mi(\\b)",Pattern.CASE_INSENSITIVE);
    private static final Pattern P_IS_NOTE = Pattern.compile("\\s+note\\s+",Pattern.CASE_INSENSITIVE);


    public static class DeviceName {
        private String name;
        private String not_parsed;
        private DeviceName(boolean is_mi, boolean is_redmi, boolean is_poco, boolean is_note, String rest){
            this.is_mi = is_mi;
            this.is_redmi = is_redmi;
            this.is_poco = is_poco;
            this.is_note = is_note;
            this.not_parsed = rest;
        }

        public static List<DeviceName> parse(String name){
            String[] parts = name.split("/");
            Boolean is_redmi = null, is_poco = null, is_note = null, is_mi = null;
            ArrayList<DeviceName> result = new ArrayList<>();
            String lastUnparsed = null;
            for (String part : parts) {
                String tmp = P_IS_REDMI.matcher(part).replaceAll("");
                is_redmi = tmp.length() < part.length() || (is_redmi == null ? false : is_redmi);
                part = tmp;
                tmp = P_IS_POCO.matcher(part).replaceAll("");
                is_poco = tmp.length() < part.length() || (is_poco == null ? false : is_poco);
                part = tmp;
                tmp = P_IS_MI.matcher(part).replaceAll("");
                is_mi = tmp.length() < part.length() || (is_mi == null ? false : is_mi);
                part = tmp;
                tmp = P_IS_NOTE.matcher(part).replaceAll("");
                is_note = tmp.length() < part.length() || (is_note == null ? false : is_note);
                tmp = tmp.trim();
                if (("pro".equalsIgnoreCase(tmp) || "prime".equalsIgnoreCase(tmp)) && lastUnparsed != null){
                    tmp = lastUnparsed+" "+tmp;
                } else {
                    lastUnparsed = tmp;
                }
                if (!tmp.isEmpty()) {
                    DeviceName device = new DeviceName(is_mi, is_redmi, is_poco, is_note, tmp);
                    result.add(device);
                }


            }
            return result;
        }

        public String getNotParsed() {
            return not_parsed;
        }

        @Override
        public String toString(){
            String name;
            if (is_mi){
                name = "Mi";
            } else if (is_redmi){
                name = "Redmi";
            } else if (is_poco){
                name = "Poco";
            } else {
                name = "";
            }
            if (is_note){
                name+=" note";
            }
            return name+" "+not_parsed;
        }
        @Override
        public boolean equals(Object obj){
            if (!(obj instanceof DeviceName)){
                return false;
            }
            DeviceName dev = (DeviceName) obj;
            boolean neq = (this.is_note^dev.is_note) || (this.is_redmi^dev.is_redmi) || (this.is_poco^dev.is_poco) || (this.is_mi^dev.is_mi);
            return !neq && this.not_parsed.equalsIgnoreCase(dev.not_parsed);
        }

        @Override
        public int hashCode(){
            return this.not_parsed.hashCode()+(is_mi ? 1 : 0)+(is_redmi ? 2 : 0)+(is_poco ? 4 : 0)+(is_note ? 8 : 0);
        }

        public boolean isRedmi(){
            return is_redmi;
        }
        public boolean isPoco(){
            return is_poco;
        }
        public boolean isNote(){
            return is_note;
        }

        private boolean is_redmi, is_poco, is_note, is_mi;

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
        } else {
            String str = "MI5c";
            if (codename.equalsIgnoreCase("meri")) {
                codename = str;
            } else if (codename.equalsIgnoreCase("song")) {
                codename = str;
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
            } else if (codename.equalsIgnoreCase("andromeda")) {
                codename = "MIMix35G";
            } else if (codename.equalsIgnoreCase("davinci")) {
                codename = "HMK20MI9T";
            } else if (codename.equalsIgnoreCase("raphael")) {
                codename = "HMK20ProMI9TPro";
            } else if (codename.equalsIgnoreCase("pine")) {
                codename = "HM7A";
            } else if (codename.equalsIgnoreCase("nitrogen")) {
                codename = "MIMAX3";
            } else if (codename.equalsIgnoreCase("pyxis")) {
                codename = "MICC9";
            }
        }
        return codename;
    }

}
