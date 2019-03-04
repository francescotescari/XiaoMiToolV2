package com.xiaomitool.v2.utility;

public class KeepOriginClass {
    private String origin;
    public KeepOriginClass(String origin){
        this.origin = origin;
    }

    public String getOrigin() {
        return origin;
    }
    @Override
    public String toString(){
        return origin;
    }
}
