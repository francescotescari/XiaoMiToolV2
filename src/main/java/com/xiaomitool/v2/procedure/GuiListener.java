package com.xiaomitool.v2.procedure;

import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.utility.CommandClass;

public abstract class GuiListener implements GuiListenerAbstract {

  private final CommandClass msgManager = new CommandClass();

  public abstract void toast(String message);

  public void toast(LRes msg) {
    toast(msg.toString());
  }

  public abstract void text(String message);

  public void text(LRes msg) {
    text(msg.toString());
  }

  public CommandClass.Command exception(InstallException exception, Runnable beforeWaitCommand)
      throws InterruptedException {
    onException(exception);
    if (beforeWaitCommand != null) {
      beforeWaitCommand.run();
    }
    return this.waitCommand();
  }

  @Override
  public void sendCommand(CommandClass.Command cmd) {
    msgManager.sendCommand(cmd);
  }

  @Override
  public CommandClass.Command waitCommand() throws InterruptedException {
    return msgManager.waitCommand();
  }

  @Override
  public boolean isWaitingCommand() {
    return msgManager.isWaitingCommand();
  }

  public abstract void onException(InstallException exception);

  public static class Debug extends GuiListener {
    @Override
    public void toast(String message) {}

    @Override
    public void text(String message) {}

    @Override
    public void onException(InstallException exception) {
      exception.printStackTrace();
    }
  }
}
