package com.xiaomitool.v2.procedure;

import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.logging.feedback.LiveFeedbackEasy;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.utility.CommandClass;

public abstract class GuiListener extends CommandClass {


    public abstract void toast(String message);
    public  void toast(LRes msg){
        toast(msg.toString());
    }
    public abstract void text(String message);
    public void text(LRes msg){
        text(msg.toString());
    }

    public Command exception(InstallException exception, Runnable beforeWaitCommand) throws InterruptedException {
        onException(exception);
        if (beforeWaitCommand != null){
            beforeWaitCommand.run();
        }
        return this.waitCommand();
    }


    protected abstract void onException(InstallException exception) ;

    public static class Debug extends GuiListener {

        @Override
        public void toast(String message) {
            /*Log.debug("[TOAST] "+message);*/
        }

        @Override
        public void text(String message) {
            /*Log.debug("[MESSAGE] "+message);*/
        }






        @Override
        protected void onException(InstallException exception) {
            /*Log.debug("[EXCEPTION] "+exception.getMessage());*/
            exception.printStackTrace();
        }
    }

}

