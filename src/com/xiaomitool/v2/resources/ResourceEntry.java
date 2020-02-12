package com.xiaomitool.v2.resources;

import com.xiaomitool.v2.utility.utils.SettingsUtils;

public class ResourceEntry {
    boolean changeSO = false;
    Type type = Type.FILE;
    private String pathname;

    public ResourceEntry(String pathname, boolean changeSO, Type type) {
        this.pathname = pathname;
        this.changeSO = changeSO;
        this.type = type;
    }

    public String getPathname() {
        String path = changeSO ? ResourcesConst.getOSName() : ResourcesConst.OSNAME_GENERIC;
        path = path + SettingsUtils.fileSeparator + pathname;
        if (Type.EXECUTABLE.equals(type)) {
            path += ResourcesConst.getOSExeExtension();
        }
        return path;
    }

    public enum Type {
        FILE,
        EXECUTABLE
    }
}
