package com.xiaomitool.v2.xiaomi.miuithings;

public enum UnlockStatus {
    LOCKED(0),
    UNLOCKED(1),
    UNKNOWN(2);
    private int status;
    private UnlockStatus(int i){
        this.status = i;
    }

    public int getInt() {
        return status;
    }
    public static UnlockStatus fromString(String lockstate){
        if (lockstate == null){
            return UNKNOWN;
        }
        lockstate = lockstate.toLowerCase();
        switch (lockstate){
            case "locked":
                return LOCKED;
            case "unlocked":
                return UNLOCKED;
                default:
                    return UNKNOWN;
        }
    }
}
