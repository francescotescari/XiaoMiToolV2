package com.xiaomitool.v2.xiaomi.miuithings;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Mirrors {
    public static final String DEFAULT_MIRROR_ENTRY = "MirrorList";
    public static final String DEFAULT_MIRROR = "http://bigota.d.miui.com";

    private class StrLenCompare implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
           return o1.length() - o2.length();
        }

    }

    List<String> mirrors = new ArrayList<>();
    public void addFromJson(JSONArray json){
        for (int i = 0; i<json.length(); ++i){
            mirrors.add(json.getString(i));
        }
    }

    public void setFromJson(JSONArray json){
        mirrors = new ArrayList<>();
        addFromJson(json);
    }

    public int length(){
        return mirrors.size();
    }

    public String get(int index){
        return mirrors.get(index);
    }
    public void sort(){
        mirrors.sort(new StrLenCompare());
    }

    public String getFirst(){
        if (mirrors.size() < 1) {
            return DEFAULT_MIRROR;
        }
        sort();
        return mirrors.get(0);
    }
    public String resolve(String filename){
        String first = getFirst();

            return (first.endsWith("/") ? first : first+"/") + (filename.startsWith("/") ? filename.substring(1) : filename);

    }
}
