package com.xiaomitool.v2.utility.utils;

import java.util.LinkedHashSet;

public class ArrayUtils {
    public static <T> LinkedHashSet<T> createLinkedHashSet(T... objs) {
        LinkedHashSet<T> set = new LinkedHashSet<>();
        for (T obj : objs) {
            set.add(obj);
        }
        return set;
    }

    public static <T> T[] reverse(T[] array) {
        int len = array.length;
        Object[] res = new Object[len];
        len--;
        for (int i = 0; i <= len; ++i) {
            res[i] = array[len - i];
        }
        return (T[]) res;
    }

    public static byte[] reverse(byte[] array) {
        int len = array.length;
        byte[] res = new byte[len];
        len--;
        for (int i = 0; i <= len; ++i) {
            res[i] = array[len - i];
        }
        return res;
    }

    public static <T> boolean in(T[] array, T element) {
        if (element == null) {
            return false;
        }
        for (T o : array) {
            if (element.equals(o)) {
                return true;
            }
        }
        return false;
    }
}
