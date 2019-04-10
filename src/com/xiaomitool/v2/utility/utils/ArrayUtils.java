package com.xiaomitool.v2.utility.utils;

import java.util.HashSet;
import java.util.Set;

public class ArrayUtils {
    public static <T> HashSet<T> createHashSet(T... objs){
        HashSet<T> set = new HashSet<>();
        for (T obj : objs){
            set.add(obj);
        }
        return set;
    }

}
