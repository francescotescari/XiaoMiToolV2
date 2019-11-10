package com.xiaomitool.v2.test;

import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.adb.device.DeviceProperties;
import com.xiaomitool.v2.logging.Log;

import com.xiaomitool.v2.procedure.GuiListener;
import com.xiaomitool.v2.procedure.ProcedureRunner;
import com.xiaomitool.v2.procedure.Procedures;
import com.xiaomitool.v2.procedure.RMessage;
import com.xiaomitool.v2.procedure.fetch.FastbootFetch;
import com.xiaomitool.v2.utility.MTPUtils;
import com.xiaomitool.v2.utility.Pointer;

import java.util.Map;

public class Main3 {
    public static void main(String[] argv) throws Exception, RMessage {
        ProcedureRunner runner = new ProcedureRunner(new GuiListener.Debug());
        Device device = new Device("ciao");
        device.getDeviceProperties().userSet(DeviceProperties.CODENAME, "gemini");
        runner.setContext(Procedures.SELECTED_DEVICE, device);
        FastbootFetch.findBestRecoveryFastboot().run(runner);



       /* HungyThread thread = new HungyThread();
        thread.start();
        Thread.sleep(2000);
        thread.addRunnable(new Runnable() {
            @Override
            public void run() {
                Log.debug("Dopo due secondi");
            }
        });
        Thread.sleep(4000);
        thread.addRunnable(() -> {
            Log.debug("Dopo sei secondi");
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.debug("ADDING RUNNABLE");
            try {
                thread.addRunnable(new Runnable() {
                    @Override
                    public void run() {

                        Log.debug("Dopo 12");
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.debug("Dopo 10 secondi");
        });
*/
    }
}
