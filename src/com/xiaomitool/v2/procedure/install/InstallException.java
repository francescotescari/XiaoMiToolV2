package com.xiaomitool.v2.procedure.install;

import com.xiaomitool.v2.adb.AdbException;
import com.xiaomitool.v2.inet.CustomHttpException;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.rom.RomException;
import com.xiaomitool.v2.xiaomi.XiaomiProcedureException;

public class InstallException extends Exception {

    public static final InstallException ABORT_EXCEPTION = new InstallException("The installation was aborted by the user", Code.ABORTED, false);


    public InstallException(InterruptedException e) {
        this("InterruptedException: "+e.getMessage(), Code.INTERNAL_ERROR, true);
    }

    public InstallException(AdbException e) {
        this("Adb execution exception: "+e.getMessage(), Code.ADB_EXCEPTION, true);
    }

    public InstallException(XiaomiProcedureException exeption) {
        this("Xiaomi procedure failed: "+exeption.getMessage(), Code.XIAOMI_EXCEPTION, true);
    }

    public enum Code {
        REBOOT_FAILED,
        NOT_IN_VALID_TWRP,
        INTERNAL_ERROR,
        RESOURCE_FETCH_FAILED,
        FASTBOOT_FLASH_FAILED,
        ADB_EXCEPTION,
        MTP_FAILED,
        FILE_NOT_FOUND,
        MTP_INSTALL_FAILED,
        INFO_RETRIVE_FAILED,
        XIAOMI_EXCEPTION,
        CONNECTION_ERROR,
        ROM_OTA_ERROR,
        ABORTED,
        ROM_SELECTION_ERROR,
        MISSING_PROPERTY,
        DOWNLOAD_FAILED,
        EXTRACTION_FAILED,
        IO_ERROR,
        HASH_FAILED,
        UNLOCK_ERROR,
        TWRP_INSTALL_FAILED,
        SIDELOAD_INSTALL_FAILED,
        WIPE_FAILED,
        WAIT_DEVICE_TIMEOUT,
        OS_NOT_SUPPORTED,
        CANNOT_INSTALL;
        private String key;
        Code(String key){
            this.key = key;
        }
        Code(){this.key = this.name().toLowerCase();}
        @Override
        public String toString() {
            return key;
        }
    }




    private Code code;
    private boolean waitCommand;
    private  Object[] params;
    public InstallException(CustomHttpException exception){
        this("Internet connection error: "+exception.getMessage(), Code.CONNECTION_ERROR, true);
    }
    public InstallException(RomException e) {
        this("Rom selection procedure error: "+e.getMessage(), Code.ROM_SELECTION_ERROR, true);
    }

    public InstallException(String message, Code code,boolean waitCommand, Object... otherParams){
        super(message);
        Log.warn("InstallException created: "+code+" - "+message);
        this.code = code;
        this.waitCommand = waitCommand;
        this.params = otherParams;
    }
    @Override
    public String toString(){
        return this.getClass().getSimpleName()+" - "+this.getCode()+" - "+this.getMessage();
    }

    public boolean isWaitCommand() {
        return waitCommand;
    }

    public Object[] getParams() {
        return params;
    }

    public Code getCode() {
        return code;
    }
}
