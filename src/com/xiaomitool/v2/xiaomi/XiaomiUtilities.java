package com.xiaomitool.v2.xiaomi;


import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.adb.device.DeviceGroups;
import com.xiaomitool.v2.utility.NotNull;
import com.xiaomitool.v2.xiaomi.miuithings.Codebase;

import java.util.ArrayList;
import java.util.HashMap;
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

    public static HashMap<String, String> getDeviceCodenames(){
        HashMap<String, String> map = new HashMap<>();
        map.put("equuleus","MI 8 PRO");
        map.put("armani","Redmi 1S");
        map.put("cactus","Redmi 6A");
        map.put("laurus","MI A3");
        map.put("dior","Redmi Note 1LTE");
        map.put("gucci","Redmi Note 1S");
        map.put("taurus","MI 2A");
        map.put("aries","MI 2S");
        map.put("pisces","MI 3");
        map.put("cancro","MI 3W / MI 4W");
        map.put("libra","MI 4c");
        map.put("ferrari","MI 4I");
        map.put("aqua","MI 4S");
        map.put("gemini","MI 5");
        map.put("meri","MI 5C");
        map.put("capricorn","MI 5s");
        map.put("natrium","MI 5s Plus");
        map.put("tiffany","MI 5X");
        map.put("sagit","MI 6");
        map.put("wayne","MI 6X");
        map.put("dipper","MI 8");
        map.put("ursa","MI 8 Explorer Edition");
        map.put("sirius","MI 8 SE");
        map.put("cepheus","MI 9");
        map.put("crux","MI 9 PRO 5G");
        map.put("grus","MI 9 SE");
        map.put("tissot","MI A1");
        map.put("pyxis","MI CC 9");
        map.put("vela","MI CC9 MEITU");
        map.put("hydrogen","MI MAX");
        map.put("oxygen","MI MAX 2");
        map.put("nitrogen","MI MAX3");
        map.put("helium","MI Max Prime");
        map.put("chiron","MI MIX 2");
        map.put("polaris","MI MIX 2S");
        map.put("perseus","MI MIX 3");
        map.put("scorpio","MI Note 2");
        map.put("jason","MI Note 3");
        map.put("virgo","MI Note");
        map.put("leo","MI Note PRO");
        map.put("mione_plus","MI ONE Plus");
        map.put("latte","MI PAD 2");
        map.put("cappu","MI PAD 3");
        map.put("clover","MI PAD 4");
        map.put("lotus","MI Play");
        map.put("lithium","MI MIX");
        map.put("platina","MI 8 Lite");
        map.put("ido_xhdpi","Redmi 3");
        map.put("land","Redmi 3S");
        map.put("prada","Redmi 4");
        map.put("rolex","Redmi 4A");
        map.put("markw","Redmi 4 Prime");
        map.put("santoni","Redmi 4X");
        map.put("rosy","Redmi 5");
        map.put("riva","Redmi 5A");
        map.put("riva_fs","Redmi 5A FS");
        map.put("vince","Redmi 5 Plus");
        map.put("cereus","Redmi 6");
        map.put("sakura","Redmi 6 Pro");
        map.put("onclite","Redmi 7");
        map.put("pine","Redmi 7A");
        map.put("davinci","Redmi K20");
        map.put("raphael","Redmi K20 Pro");
        map.put("raphaels","Redmi K20 Pro Premium Edition");
        map.put("hermes","Redmi Note 2");
        map.put("kenzo","Redmi Note 3");
        map.put("nikel","Redmi Note 4 MTK");
        map.put("whyred","Redmi Note 5");
        map.put("ugg","Redmi Note 5A");
        map.put("ugglite","Redmi Note 5A Lite");
        map.put("lavender","Redmi Note 7");
        map.put("violet","Redmi Note 7 Pro");
        map.put("ginkgo","Redmi Note 8");
        map.put("begonia","Redmi Note 8 Pro");
        map.put("ysl","Redmi S2");
        map.put("wt98007","Redmi 1");
        map.put("virgo_lte_ct","小米Note 全网通版");
        map.put("mocha","MI Pad");
        map.put("cancro_lte_ct","小米手机4 电信4G版");
        map.put("hermes_pro","Redmi Note 2 PRO");
        map.put("hennessy","Redmi Note 3 MTK");
        map.put("mido","Redmi Note 4X/Redmi Note 4");
        map.put("lcsh92_wet_tdd","红米Note TD版");
        map.put("lcsh92_wet_jb9","红米Note WCDMA版");
        map.put("omega","Redmi PRO");
        map.put("HM2014011","红米手机1S TD 3G版");
        map.put("HM2014501","红米手机1S TD 4G版");
        map.put("lte26007","红米手机2A 标准版/增强版");
        map.put("wt86047_pro","红米手机2 移动增强版 / 红米手机2A 高配版");
        map.put("wt86047","红米手机2 移动版");
        map.put("wt88047_pro","红米手机2 联通版/电信 增强版");
        map.put("wt88047","红米手机2 联通版/电信版");
        map.put("HM2013022","红米手机 TD版");
        map.put("HM2013023","红米手机 WCDMA版");
        return map;
    }

}
