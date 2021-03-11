package com.xiaomitool.v2.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiMap<K, V> extends HashMap<K, List<V>> {
    public void putSingle(K key, V value) {
        List<V> entry = super.get(key);
        if (entry == null) {
            entry = new ArrayList<>();
            super.put(key, entry);
        }
        entry.add(value);
    }

    public boolean removeSingle(K key, V value) {
        List<V> entry = super.get(key);
        if (entry == null) {
            return false;
        }
        return entry.remove(value);
    }

    @Override
    public String toString() {
        String res = "{";
        for (Map.Entry<K, List<V>> entry : super.entrySet()) {
            res += entry.getKey() + " = [";
            for (V val : entry.getValue()) {
                res += "(" + val.toString() + "), ";
            }
            res = res.substring(0, res.length() - 2);
            res += "], ";
        }
        res = res.substring(0, res.length() - 2);
        res += "}";
        return res;
    }
}
