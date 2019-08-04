package com.xiaomitool.v2.utility.utils;

import com.xiaomitool.v2.resources.ResourcesConst;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrUtils {
    static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final Pattern PROGRESS_REGEX = Pattern.compile("\\[\\s*(\\d+)\\s*\\/\\s*(\\d+)\\s*\\]");
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.00");
    public static String randomWord(int len){
        int clen = CHARS.length();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i<len; ++i){
            builder.append(CHARS.charAt(ThreadLocalRandom.current().nextInt(0, clen)));
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


    public static String map2json(Map<?, ?> map, int indent){
        return map2json(map, indent, indent);
    }

    private static String map2json(Map<?, ?> map, int indent, int sindent){

        if (!(map instanceof LinkedHashMap)){
            return new JSONObject(map).toString(indent);
        }
        final String NL = indent > 0 ? "\n" : "";
        String in = indentToString(indent);
        final String IN = in;
        StringBuilder stringBuilder = new StringBuilder("{");
        for (Map.Entry entry : map.entrySet()){
            String toAdd;
            if (entry.getValue() instanceof Number){
                toAdd = String.valueOf(entry.getValue());
            } else if (entry.getValue() instanceof String){
                toAdd = '"'+entry.getValue().toString().replace("\"","\\\"")+'"';
            } else if (entry.getValue() instanceof Map){
                toAdd = map2json((Map<?, ?>) entry.getValue(), indent+sindent, sindent);
            } else {
                throw new JSONException("Unknown type: "+entry.getValue().getClass().getSimpleName());
            }
            stringBuilder.append(NL).append(IN).append('"').append(entry.getKey().toString()).append("\" : ").append(toAdd).append(",");
        }
        return stringBuilder.substring(0,stringBuilder.length()-1)+NL+indentToString(indent-sindent)+"}"+(indent == sindent ? NL : "");
    }


    private static String indentToString(int indent){
        if (indent <= 0){
            return "";
        }
        char[] ic = new char[indent];
        for (int i = 0; i<indent; ++i){
            ic[i] = ' ';
        }
        return new String(ic);

    }

    public static int compareVersion(String thisVersion, String ofThisVersion){
        String[] parts1 = thisVersion.split("\\.");
        String[] parts2 = ofThisVersion.split("\\.");
        //Log.debug(Integer.min(parts1.length, parts2.length));
        for (int i = 0; i<Integer.min(parts1.length, parts2.length); ++i){
            int i1 = Integer.parseInt(parts1[i]);
            int i2 = Integer.parseInt(parts2[i]);
            //Log.debug("i1: "+i1+", i2: "+i2);
            if (i1 < i2){
                return -1;
            } else if (i1 > i2){
                return 1;
            }
        }
        return parts1.length - parts2.length;
    }


    private static final byte[] SPACE_STRING = "                                                ".getBytes(ResourcesConst.interalCharset());
    private static final byte[] TABS_STIRNG = "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t".getBytes(ResourcesConst.interalCharset());
    private static String chars(byte[] source, int count) {
        if (count < 0){
            return  "";
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(count);
        while (count > 0){
            int len = Integer.min(count, source.length);
            byteArrayOutputStream.write(source, 0, len);
            count-= len;
        }
        return new String(byteArrayOutputStream.toByteArray());
    }
    public static String tabs(int tabs){
        return chars(TABS_STIRNG, tabs);
    }
    public static String spaces(int tabs){
        return chars(SPACE_STRING, tabs);
    }

    public static String after(String fullWord, String start) {
        char[] fullChars = fullWord.toCharArray();
        char[] startChars = start.toCharArray();
        int i;
        for (i = 0; i<fullChars.length && i<startChars.length; ++i){
            if (fullChars[i] != startChars[i]){
                return null;
            }
        }
        return new String(fullChars,i, fullChars.length-i);
    }

    public static String toHexString(byte[] data) {
        return Hex.encodeHexString(data);
    }

    public static String reverse(String str) {
        return new StringBuilder(str).reverse().toString();
    }

    public static String lastLine(String output) {
        if (output == null){
            return null;
        }
        try {
            return output.split("\n")[0];
        } catch (Throwable t){
            return null;
        }
    }
}
