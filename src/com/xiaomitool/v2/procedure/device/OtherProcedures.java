package com.xiaomitool.v2.procedure.device;

import com.xiaomitool.v2.procedure.ProcedureRunner;
import com.xiaomitool.v2.procedure.RInstall;
import com.xiaomitool.v2.procedure.RMessage;
import com.xiaomitool.v2.procedure.install.InstallException;

public class OtherProcedures {
    public static RInstall sleep(long millis){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                Thread.sleep(millis);
            }
        };
    }
}
