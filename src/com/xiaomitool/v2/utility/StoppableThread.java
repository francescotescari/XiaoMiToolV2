package com.xiaomitool.v2.utility;

public abstract class StoppableThread extends Thread {
    public abstract void closeBeforeStop();
    @Override
    public void interrupt(){
        closeBeforeStop();
        super.interrupt();
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
