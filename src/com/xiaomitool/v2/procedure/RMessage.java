package com.xiaomitool.v2.procedure;

import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.utility.CommandClass;

public class RMessage extends Throwable {
    private CommandClass.Command cmd;
    private InstallException installException;
    public RMessage(CommandClass.Command cmd){
        this.cmd = cmd;
    }
    public RMessage(InstallException e){
        this.installException = e;
        this.cmd = CommandClass.Command.EXCEPTION;
    }

    public CommandClass.Command getCmd() {
        return cmd;
    }

    public InstallException getInstallException() {
        return installException;
    }
}
