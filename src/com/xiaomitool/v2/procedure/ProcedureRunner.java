package com.xiaomitool.v2.procedure;

import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.adb.device.DeviceManager;
import com.xiaomitool.v2.gui.visual.InstallPane;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.logging.feedback.LiveFeedbackEasy;
import com.xiaomitool.v2.procedure.install.GenericInstall;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.rom.Installable;
import com.xiaomitool.v2.utility.utils.StrUtils;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcedureRunner extends GuiListener {
    private final HashMap<String, Object> stashedValues = new HashMap<>();
    private final List<String> stackLog = new ArrayList<>();
    private InstallException exception;
    private GuiListener listener;
    private Pane afterExeptionPane;
    private RInstall runnableInstall;
    private boolean sendFeedback = true;
    private RInstall restarter = null;
    private int spaces = 0;
    private HashMap<String, Object> context = new HashMap<>();
    private InstallPane installPane;

    public ProcedureRunner(InstallPane installPane) {
        setInstallPane(installPane);
    }

    public ProcedureRunner(GuiListener listener) {
        setListener(listener);
    }

    public void setAfterExceptionPane(Pane pane) {
        this.afterExeptionPane = pane;
    }

    public RInstall getRestarter() {
        return restarter;
    }

    public void setRestarter(RInstall restarter) {
        this.restarter = restarter;
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
                if (Command.EXCEPTION.equals(cmd)) {
                    cmd = handleException(rMessage.getInstallException(), runnable);
                }
            }
        } catch (InterruptedException e) {
            try {
                cmd = handleException(new InstallException(e), runnable);
            } catch (InterruptedException e1) {
                throw new RuntimeException(e1);
            }
        }
        return Command.NOCMD;
    }

    public Command handleException(InstallException exception, RInstall cause) throws InterruptedException, InstallException, RMessage {
        Log.warn(this.getStackStrace());
        if (InstallException.ABORT_EXCEPTION.equals(exception)) {
            Log.warn("Aborted exception thrown, show message");
            try {
                GenericInstall.restartMain(restarter).run(this);
                return Command.SINKED;
            } catch (InstallException e) {
                exception = e;
            } catch (RMessage rMessage) {
                return rMessage.getCmd();
            }
        } else {
            Log.warn("Not aborted exception thrown, show error");
        }
        final InstallException exceptionFinal = exception;
        if (cause != null && cause.hasFlag(RNode.FLAG_THROWRAWEXCEPTION)) {
            throw exception;
        }
        Log.error(this.getStackStrace());
        Command out;
        if (sendFeedback) {
            out = listener.exception(exception, () -> LiveFeedbackEasy.sendInstallException(exceptionFinal, ProcedureRunner.this));
        } else {
            out = listener.exception(exception, null);
        }
        if (Command.ABORT.equals(out)) {
            throw new RMessage(out);
        }
        return out;
    }

    void pushStackTrace(Object toLog, boolean in) {
        String log = StrUtils.tabs(spaces);
        if (in) {
            log += toLog + " {";
            ++spaces;
        } else {
            log += "} " + toLog;
            --spaces;
        }
        synchronized (this.stackLog) {
            this.stackLog.add(log);
        }
    }

    public String getStackStrace() {
        return getStackStrace(100);
    }

    public String getStackStrace(int maxlen) {
        StringBuilder builder = new StringBuilder();
        synchronized (this.stackLog) {
            int start = Integer.max(this.stackLog.size() - maxlen, 0);
            for (int i = start; i < stackLog.size(); ++i) {
                try {
                    builder.append(this.stackLog.get(i)).append("\n");
                } catch (Throwable ignored) {
                }
            }
        }
        return builder.toString();
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

    public void setContext(String key, Object value) {
        context.put(key, value);
    }

    public void stashContext(String key, Object newValue) {
        this.stashedValues.put(key, getContext(key));
        this.setContext(key, newValue);
    }

    public Object unstashContext(String key) {
        Object res = stashedValues.get(key);
        this.setContext(key, res);
        return res;
    }

    public Object getContext(String key) {
        Object res = context.get(key);
        return res;
    }

    public Object requireContext(String key) throws InstallException {
        Object res = getContext(key);
        if (res == null) {
            throw new InstallException("Failed to get context parameter: " + key, InstallException.Code.INTERNAL_ERROR);
        }
        return res;
    }

    public Object consumeContext(String key) {
        Object entry = context.get(key);
        if (entry != null) {
            context.remove(key);
        }
        return entry;
    }

    public void init(Installable romToInstall, Device device) {
        if (romToInstall != null) {
            setContext(Procedures.INSTALLABLE, romToInstall);
        }
        if (device != null) {
            setContext(Procedures.SELECTED_DEVICE, device);
        }
    }

    public void init() {
        init(null);
    }

    public void startProcedure(RInstall procedure) throws InterruptedException, InstallException, RMessage {
        this.runnableInstall = procedure;
        run(procedure);
    }

    public void init(Installable romToInstall) {
        init(romToInstall, DeviceManager.getSelectedDevice());
    }

    public InstallPane getInstallPane() {
        return this.installPane;
    }

    public void setInstallPane(InstallPane installPane) {
        this.installPane = installPane;
        if (installPane != null) {
            setListener(installPane.getListener());
        } else {
            setListener(null);
        }
        setAfterExceptionPane(installPane);
    }

    private void setListener(GuiListener listener) {
        if (listener == null) {
            this.listener = new GuiListener.Debug();
        } else {
            this.listener = listener;
        }
    }

    public void stashEntireContext(HashMap<String, Object> dst) {
        for (Map.Entry<String, Object> entry : this.context.entrySet()) {
            dst.put(entry.getKey(), entry.getValue());
        }
    }

    public void reloadContext(HashMap<String, Object> src) {
        this.context = new HashMap<>();
        for (Map.Entry<String, Object> entry : src.entrySet()) {
            context.put(entry.getKey(), entry.getValue());
        }
    }
}
