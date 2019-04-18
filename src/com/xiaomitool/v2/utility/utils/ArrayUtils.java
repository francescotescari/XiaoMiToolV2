package com.xiaomitool.v2.utility.utils;

import com.xiaomitool.v2.logging.Log;

import java.util.LinkedHashSet;
import java.util.Objects;

public class ArrayUtils {
    public static <T> LinkedHashSet<T> createLinkedHashSet(T... objs){
        LinkedHashSet<T> set = new LinkedHashSet<>();
        for (T obj : objs){
            set.add(obj);
        }
        return set;
    }

    public static <T> T[] reverse(T[] array){
        int len = array.length;
        Object[] res = new Object[len];
        len--;
        for (int i = 0; i<=len; ++i){
            res[i] = array[len-i];
        }
        return (T[]) res;
    }

    public static byte[] reverse(byte[] array){
        int len = array.length;
        byte[] res = new byte[len];
        len--;
        for (int i = 0; i<=len; ++i){
            res[i] = array[len-i];
        }
        return res;
    }

}
