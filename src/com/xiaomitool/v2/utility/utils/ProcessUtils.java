package com.xiaomitool.v2.utility.utils;

import com.xiaomitool.v2.process.ProcessRunner;

public class ProcessUtils {
    public static String getOutput(ProcessRunner runner, String onNonZeroCode){
        if (runner != null && runner.getExitValue() == 0){
            return runner.getOutputString();
        }
        return onNonZeroCode;
    }
    public static String getOutput(ProcessRunner runner){
        return getOutput(runner,null);
    }
}
