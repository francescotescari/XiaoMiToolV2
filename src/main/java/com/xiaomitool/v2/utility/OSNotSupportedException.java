package com.xiaomitool.v2.utility;

import org.apache.commons.lang3.SystemUtils;

public class OSNotSupportedException extends UnsupportedOperationException {
    public OSNotSupportedException() {
        super();
    }

    public OSNotSupportedException(String msg) {
        super(msg);
    }

    public static void requireWindows() {
        if (!SystemUtils.IS_OS_WINDOWS) {
            throw new OSNotSupportedException("This operation requires a Windows machine");
        }
    }
}
