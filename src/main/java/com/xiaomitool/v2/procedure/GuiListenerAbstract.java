package com.xiaomitool.v2.procedure;

import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.utility.CommandClass;
import com.xiaomitool.v2.utility.CommandClassAbstract;

public interface GuiListenerAbstract extends CommandClassAbstract {
    public void text(String message);

    public void toast(String message);

    void onException(InstallException exception);

    public CommandClass.Command exception(InstallException exception, Runnable beforeWaitCommand) throws InterruptedException;
}
