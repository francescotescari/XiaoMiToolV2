package com.xiaomitool.v2.utility.utils;

import java.util.concurrent.ThreadLocalRandom;

public class NumberUtils {
    public static int getRandom(int min, int max){
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
    public static String intToHex(int number){
        String hex = Integer.toHexString(number);
        while (hex.length() < 8){
            hex = "0"+hex;
        }
        return "0x"+hex;
    }

    public static Long parseLong(String str) {
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e){
            return null;
        }
    }

    public static int double2int(double number) {
        return Double.valueOf(number).intValue();
    }
}
