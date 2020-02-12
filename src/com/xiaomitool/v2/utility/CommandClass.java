package com.xiaomitool.v2.utility;

public class CommandClass {
    private WaitSemaphore semaphore = new WaitSemaphore();
    private Command command = null;
    private boolean isWaitingCommand = false;

    public void sendCommand(Command cmd) {
        this.command = cmd;
        semaphore.setPermits(1);
    }

    public Command waitCommand() throws InterruptedException {
        isWaitingCommand = true;
        semaphore.decrease();
        isWaitingCommand = false;
        Command out = this.command;
        this.command = null;
        return out;
    }

    protected boolean isWaitingCommand() {
        return isWaitingCommand;
    }

    public enum Command {
        RETRY,
        ABORT,
        NOCMD,
        UPLEVEL,
        EXCEPTION,
        SINKED
    }
}
