package com.xiaomitool.v2.procedure;

import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.utility.CommandClass;

public abstract class GuiListener extends CommandClass implements GuiListenerAbstract {

    public static GuiListener implement(GuiListenerAbstract listenerAbstract){
        return new GuiListener() {
            @Override
            public void toast(String message) {
                listenerAbstract.toast(message);
            }

            @Override
            public void text(String message) {
                listenerAbstract.text(message);
            }

            @Override
            public void onException(InstallException exception) {
                listenerAbstract.onException(exception);
            }
        };
    }

    public abstract void toast(String message);

    public void toast(LRes msg) {
        toast(msg.toString());
    }

    public abstract void text(String message);

    public void text(LRes msg) {
        text(msg.toString());
    }

    public Command exception(InstallException exception, Runnable beforeWaitCommand) throws InterruptedException {
        onException(exception);
        if (beforeWaitCommand != null) {
            beforeWaitCommand.run();
        }
        return this.waitCommand();
    }

    public abstract void onException(InstallException exception);

    public static class Debug extends GuiListener {
        @Override
        public void toast(String message) {
        }

        @Override
        public void text(String message) {
        }

        @Override
        public void onException(InstallException exception) {
            exception.printStackTrace();
        }
    }
}

