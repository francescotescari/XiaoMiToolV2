package com.xiaomitool.v2.procedure;

import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.adb.device.DeviceManager;
import com.xiaomitool.v2.adb.device.DeviceProperties;
import com.xiaomitool.v2.gui.visual.InstallPane;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.logging.feedback.LiveFeedback;
import com.xiaomitool.v2.logging.feedback.LiveFeedbackEasy;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.rom.Installable;
import com.xiaomitool.v2.utility.utils.StrUtils;
import javafx.scene.layout.Pane;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProcedureRunner extends GuiListener {
    private InstallException exception;
    private GuiListener listener;
    private Pane afterExeptionPane;
    private RInstall runnableInstall;
    private boolean sendFeedback = true;



    public ProcedureRunner(InstallPane installPane){
        this(installPane.getListener());
        setAfterExceptionPane(installPane);
    }

    public ProcedureRunner(GuiListener listener){
        this.listener = listener;
        if (listener == null){
            this.listener = new GuiListener.Debug();
        }
    }
    public void setAfterExceptionPane(Pane pane){
        this.afterExeptionPane = pane;
    }



    public Command run(RInstall runnable) throws InstallException, RMessage {

        Command cmd = Command.NOCMD;
        try {
            try {
                runnable.run(this);
            } catch (InstallException e) {

                cmd = handleException(exception, runnable);

            } catch (RMessage rMessage) {
                cmd = rMessage.getCmd();
                if (Command.EXCEPTION.equals(cmd)){
                    cmd = handleException(rMessage.getInstallException(), runnable);
                }
            }
        } catch (InterruptedException e){
            try {
                cmd = handleException(new InstallException(e),runnable);
            } catch (InterruptedException e1) {
                throw new RuntimeException(e1);
            }
        }
        return Command.NOCMD;
    }

    public Command handleException(InstallException exception, RInstall cause) throws InterruptedException, InstallException, RMessage {
        if (cause != null && cause.hasFlag(RNode.FLAG_THROWRAWEXCEPTION)){
            throw exception;
        }
        Command out;
        if (sendFeedback) {
            out = listener.exception(exception, () -> LiveFeedbackEasy.sendInstallException(exception, ProcedureRunner.this));
        } else {
            out = listener.exception(exception, null);
        }
        if (Command.ABORT.equals(out)){
            throw new RMessage(out);
        }
        return out;
    }

    private final List<String> stackLog = new ArrayList<>();
    void pushStackTrace(String stackLog){
        this.stackLog.add(stackLog);
    }

    public  String getStackStrace(){
        return String.join("\n",stackLog);
    }








    @Override
    public void toast(String message) {
        listener.toast(message);
    }

    @Override
    public void text(String message) {
        listener.text(message);
    }

    @Override
    protected void onException(InstallException exception) {
        listener.onException(exception);
    }


    private HashMap<String, Object> context = new HashMap<>();
    public void setContext(String key, Object value){
        context.put(key,value);
        Log.debug("Context var set: "+key+" -> "+StrUtils.str(value));
    }

    public Object getContext(String key){

        Object res = context.get(key);
        Log.debug("Getting context var: "+key+" -> "+StrUtils.str(res));
        return res;
    }
    public Object requireContext(String key) throws InstallException {
        Object res = getContext(key);
        if (res == null){
            throw new InstallException("Failed to get context parameter: "+key, InstallException.Code.INTERNAL_ERROR, false);
        }
        return res;
    }
    public Object consumeContext(String key){
        Object entry = context.get(key);
        if (entry != null){
            context.remove(key);
        }
        return entry;
    }

    public void init(Installable romToInstall, Device device){
        if (romToInstall != null) {
            setContext(Procedures.INSTALLABLE, romToInstall);
        }
        if (device != null) {
            setContext(Procedures.SELECTED_DEVICE, device);
            Object codename = device.getDeviceProperties().get(DeviceProperties.CODENAME);
            setContext(Procedures.DEVICE_CODENAME, codename);
        }
    }
    public void init(){
        init(null);
    }

    public void startProcedure(RInstall procedure) throws InterruptedException, InstallException, RMessage {
        this.runnableInstall = procedure;
        run(procedure);
    }
    public void init(Installable romToInstall){
        init(romToInstall, DeviceManager.getSelectedDevice());
    }


}
