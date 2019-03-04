package com.xiaomitool.v2.xiaomi.miuithings;

import com.xiaomitool.v2.utility.KeepOriginClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Codebase extends KeepOriginClass {
    public boolean isValid = true;
    private Integer main;
    private Integer sub;
    private Integer revision;

    public Codebase(String version){
        super(version);
        if (version == null){
            return;
        }
        Pattern p = Pattern.compile("(\\d)\\.(\\d)\\.{0,1}(\\d{0,1})");
        Matcher m = p.matcher(version);
        if (!m.matches()){
            isValid = false;
            return;
        }
        main = Integer.parseInt(m.group(1));
        sub = Integer.parseInt(m.group(2));
        String third = m.group(3);
        revision =  !third.equals("") ? Integer.parseInt(m.group(3)) : null;
    }

    public int getMain(){
        return main;
    }
    public int getSub(){
        return sub;
    }

    int compare(Codebase v2){
        if (!isValid || !v2.isValid){
            return -2;
        }
        return main > v2.getMain() ? 1 : (main < v2.getMain() ? -1 : (sub > v2.getSub() ? 1 : (sub < v2.getSub() ? -1 : 0)));
    }

    @Override
    public String toString() {
        if (!isValid){
            return super.toString();
        }
        return main+"."+sub+(revision != null ? "."+revision : "");
    }
}
