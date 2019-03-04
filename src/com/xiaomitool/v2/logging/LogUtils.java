package com.xiaomitool.v2.logging;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtils {
    public static String getTimeStamp(){
        return new SimpleDateFormat("[HH:mm:ss]").format(new Date());
    }
    public static String toStringArray(Object[] array){
        String s = "{";
        for (Object o : array){
            s+=o.toString()+", ";
        }
        s=s.substring(0,s.length()-2);
        return s+"}";
    }
}
