package com.xiaomitool.v2.utility;

public enum  YesNoMaybe {
    YES(0),
    NO(1),
    MAYBE(2);
    private int code;
    private YesNoMaybe(int code){
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
