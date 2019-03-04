package com.xiaomitool.v2.procedure;

import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.utility.CommandClass;

public abstract class GuiListener extends CommandClass {
    public enum Event {
        CLEAR,
        WAITING_TASK,
        STOCK_RECOVERY_REBOOTING,
        DEVICE_UNAUTH_OFFLINE,
        NEED_DEBUGGING_ACTIVE;
    }

    public abstract void toast(String message);
    public  void toast(LRes msg){
        toast(msg.toString());
    }
    public abstract void text(String message);
    public void text(LRes msg){
        text(msg.toString());
    }
    public abstract void onEvent(Event event, Object subject);
    public Command exception(InstallException exception) throws InterruptedException {
        onException(exception);
        return this.waitCommand();
    }


    protected abstract void onException(InstallException exception);
    public void clearEvent(){
        onEvent(Event.CLEAR,null);
    }
    public static class Debug extends GuiListener {

        @Override
        public void toast(String message) {
            Log.debug("[TOAST] "+message);
        }

        @Override
        public void text(String message) {
            Log.debug("[MESSAGE] "+message);
        }

        @Override
        public void onEvent(Event event, Object subject) {
            Log.debug("[EVENT]["+event.toString()+"]"+(subject != null ? subject.toString() : "null"));
        }




        @Override
        protected void onException(InstallException exception) {
            Log.debug("[EXCEPTION] "+exception.getMessage());
            exception.printStackTrace();
        }
    }

}

