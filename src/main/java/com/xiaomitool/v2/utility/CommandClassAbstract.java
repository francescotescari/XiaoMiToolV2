package com.xiaomitool.v2.utility;

public interface CommandClassAbstract {
    public void sendCommand(CommandClass.Command cmd);

    public CommandClass.Command waitCommand() throws InterruptedException;

    public boolean isWaitingCommand();
}
