package com.xiaomitool.v2.adb;

import com.xiaomitool.v2.procedure.install.InstallException;

public class AdbException extends Exception {
    public AdbException(){
        super();
    }
    public AdbException(String message){
        super(message);
    }
    public InstallException toInstallException(boolean waitCommand){
        return new InstallException(this.getMessage(), InstallException.Code.ADB_EXCEPTION, waitCommand);
    }
}
