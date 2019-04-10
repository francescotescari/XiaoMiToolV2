package com.xiaomitool.v2.utility.utils;

import java.util.LinkedHashSet;

public class ArrayUtils {
    public static <T> LinkedHashSet<T> createLinkedHashSet(T... objs){
        LinkedHashSet<T> set = new LinkedHashSet<>();
        for (T obj : objs){
            set.add(obj);
        }
        return set;
    }

}
