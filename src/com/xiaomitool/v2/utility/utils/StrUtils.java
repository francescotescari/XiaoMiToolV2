package com.xiaomitool.v2.utility.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrUtils {
    static final String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final Pattern PROGRESS_REGEX = Pattern.compile("\\[\\s*(\\d+)\\s*\\/\\s*(\\d+)\\s*\\]");
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.00");
    public static String randomWord(int len){
        int clen = chars.length();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i<len; ++i){
            builder.append(chars.charAt(ThreadLocalRandom.current().nextInt(0, clen)));
        }
        return builder.toString();
    }

    public static long[] parseProgress (String line){
        try {
            Matcher m = PROGRESS_REGEX.matcher(line);
            if (m.find()) {
                long[] res = new long[2];
                res[0] = Long.parseLong(m.group(1), 10);
                res[1] = Long.parseLong(m.group(2), 10);
                return res;

            }
            return null;
        } catch (Throwable t){
            return null;
        }
    }
    public static String bytesToString(Number bytes){

            String[] units = new String[]{" ", "k","M","G","T"};
            double sp = bytes.doubleValue();
            int index = 0;
            while (sp > 1000){
                sp = sp / 1000;
                ++index;
                if (Double.isInfinite(sp)){
                    break;
                }
            }
            if (index >= units.length){
                return "Infinite";
            }
            return DECIMAL_FORMAT.format(sp)+" "+units[index]+"B";

    }
    public static String exceptionToString(Throwable t){
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
    public static String firstNLines(String data, int n){
        String[] lines = data.split("\\n");
        return firstNLines(lines, n);
    }
    public static String firstNLines(String[] lines, int n){
        int max = lines.length;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i<n && i<max ; ++i){
            builder.append('\n').append(lines[i]);
        }
        return builder.length() > 0 ? builder.toString().substring(1) : builder.toString();
    }

    public static boolean isNullOrEmpty(String tmp) {
        return tmp == null || tmp.isEmpty();
    }

    public static String str(Object o){
        if (o == null){
            return "null";
        }
        return o.toString();
    }

    public static int lenght(String outputString) {
        return outputString == null ? -1 : outputString.length();
    }
}
