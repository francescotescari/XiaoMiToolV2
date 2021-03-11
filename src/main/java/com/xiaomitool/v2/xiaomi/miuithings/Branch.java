package com.xiaomitool.v2.xiaomi.miuithings;

public enum Branch {
    STABLE("F"),
    DEVELOPER("X"),
    INTERNAL("I"),
    DEBUG("D"),
    TESTING("T"),
    UNKNOWN("X"),
    ALPHA("A"),
    FAKE("F");
    private String code;

    Branch(String code) {
        this.code = code;
    }

    public static boolean isDev(Branch branch) {
        return !Branch.STABLE.equals(branch);
    }

    public static Branch fromCode(String code) {
        switch (code) {
            case "F":
                return Branch.STABLE;
            case "X":
                return Branch.DEVELOPER;
            case "I":
                return Branch.INTERNAL;
            case "D":
                return Branch.DEBUG;
            case "T":
                return Branch.TESTING;
            case "A":
                return Branch.ALPHA;
            default:
                return Branch.UNKNOWN;
        }
    }

    public static Branch fromMiuiVersion(String version) {
        String[] parts = version.split("\\.");
        if (parts.length <= 3) {
            return Branch.DEVELOPER;
        } else {
            return Branch.STABLE;
        }
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Branch getDual() {
        if (STABLE.equals(this)) {
            return this;
        }
        return DEVELOPER;
    }
}
