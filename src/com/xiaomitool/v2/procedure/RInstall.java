package com.xiaomitool.v2.procedure;

import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.install.InstallException;

public abstract class RInstall {
    protected int flags;
    private StackTraceElement[] creationStack;
    private String name = null;

    public RInstall() {
        creationStack = Thread.currentThread().getStackTrace();
    }

    public abstract void run(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException;

    void runInternal(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
        Log.info("Running now procedure: " + this.toString());
        boolean pushStack = !(this instanceof RNode);
        if (pushStack) {
            runner.pushStackTrace(this, true);
        }
        run(runner);
        if (pushStack) {
            runner.pushStackTrace(this, false);
        }
    }

    public boolean hasFlag(int flag) {
        return (this.flags & flag) != 0;
    }

    public RInstall setFlag(int flag) {
        return setFlag(flag, false);
    }

    public RInstall setFlag(int flag, boolean recursive) {
        this.flags = this.flags | flag;
        return this;
    }

    public RInstall next() {
        return RNode.sequence(this, Procedures.runStackedProcedures());
    }

    @Override
    public String toString() {
        if (name == null) {
            name = toString(1);
        }
        return name;
    }

    public String toString(int stackElements) {
        stackElements += 3;
        stackElements = Integer.min(stackElements, creationStack.length);
        StringBuilder builder = new StringBuilder(creationStack[3].getMethodName());
        for (int i = 4; i < stackElements; ++i) {
            builder.append(" -> ");
            builder.append(creationStack[i].getMethodName());
        }
        return builder.toString();
    }
}
