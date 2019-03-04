package com.xiaomitool.v2.procedure.install;

import com.xiaomitool.v2.procedure.GuiListener;

import com.xiaomitool.v2.tasks.UpdateListener;

public class InstallListener {
    public InstallListener(UpdateListener update, GuiListener listener){
        this.update = update;

        this.guiListener = listener;
    }

    private UpdateListener update;
    private GuiListener guiListener;
    public UpdateListener getUpdateListener(){
        return update;
    }



    public GuiListener getGuiListener() {
        return guiListener;
    }
    public static class Debug extends InstallListener{

        public Debug() {
            super(new UpdateListener.Debug(), new GuiListener.Debug());
        }
    }
}
