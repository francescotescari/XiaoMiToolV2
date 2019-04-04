package com.xiaomitool.v2.process;

import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.resources.ResourcesManager;
import com.xiaomitool.v2.utility.RunnableWithArg;

import java.util.LinkedList;
import java.util.List;


public class AdbRunner extends ProcessRunner {
    private String deviceSerial = null;
    private static long feedbackDisabled = 0;
    public AdbRunner() {
        super(ResourcesManager.getAdbPath());
    }
    public AdbRunner(String... params){
        super(ResourcesManager.getAdbPath(),params);
    }


    public void setDeviceSerial(String deviceSerial) {
        this.deviceSerial = deviceSerial;
    }

    @Override
    protected List<String> buildFinalArgumentsList(){
        LinkedList<String> list = new LinkedList<>();
        list.add(executable.toString());

        if (deviceSerial != null){
            list.add("-s");
            list.add(deviceSerial);
        }
        isFeedback = !arguments.contains("devices");
        if (!isFeedback){
            isFeedback = feedbackDisabled++ % 10 == 0;
        }
        list.addAll(arguments);
        return list;
    }
    /*@Override
    protected void attachStdOutErrReader(){


        RunnableWithArg cacheResult = new RunnableWithArg() {
            @Override
            public void run(Object arg) {
                String args = (String) arg;
                if (!args.startsWith("*")){
                outputBuffer.add(args);
                    Log.debug("Process output: "+args);
                } else {
                    Log.debug("Line ingored: "+args);
                }


            }
        };
        addSyncCallback(cacheResult);
    }*/
}
