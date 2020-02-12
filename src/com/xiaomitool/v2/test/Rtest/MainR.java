package com.xiaomitool.v2.test.Rtest;

import com.xiaomitool.v2.inet.CustomHttpException;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.*;
import com.xiaomitool.v2.procedure.install.InstallException;

public class MainR {
    public static void main(String... args) throws InstallException, RMessage, InterruptedException {
        RNode.fallback(three(),one(),two(),three()).run(new ProcedureRunner(new GuiListener.Debug()));
    }


    public static RInstall one(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
                /*Log.debug("ONE RUNNING");*/
            }
        };
    }

    public static RInstall two(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage {
                /*Log.debug("TWO RUNNING");*/
            }
        };
    }

    public static RInstall three(){
        return new RInstall() {
            @Override
            public void run(ProcedureRunner runner) throws InstallException, RMessage {
                /*Log.debug("THREE RUNNING");*/
                throw new InstallException(new CustomHttpException("LOL"));
            }
        };
    }



}
