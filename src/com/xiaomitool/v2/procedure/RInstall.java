package com.xiaomitool.v2.procedure;

import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.ProcedureRunner;
import com.xiaomitool.v2.procedure.install.InstallException;

public abstract class RInstall {


     public abstract void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException;
     protected int flags;

     public boolean hasFlag(int flag){
          return (this.flags & flag) != 0;
     }

     public RInstall setFlag(int flag){
          return setFlag(flag, false);
     }

     public RInstall setFlag(int flag, boolean recursive) {
          this.flags = this.flags | flag; return this;
     }

     public RInstall next(){
          return RNode.sequence(this, Procedures.runStackedProcedures());
     }

}
