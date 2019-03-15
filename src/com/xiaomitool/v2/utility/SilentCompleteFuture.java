package com.xiaomitool.v2.utility;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SilentCompleteFuture<T> extends CompletableFuture<T> {
    public T getSilently(){
        try {
            return super.get();
        } catch (Throwable e) {
            return null;
        }
    }
}
