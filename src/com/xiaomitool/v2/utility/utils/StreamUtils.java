package com.xiaomitool.v2.utility.utils;

import com.xiaomitool.v2.utility.RunnableWithArg;

import java.io.IOException;
import java.io.InputStream;

public class StreamUtils {
    private Exception error = null;

    public Exception getError() {
        return error;
    }

    public void readSync(InputStream stream, RunnableWithArg read) throws IOException {
        error = null;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] data = new byte[1024];
                    while (stream.read(data) > 0) {
                        String str = new String(data);
                        String[] lines = str.split("\n");
                        for (String sa : lines) {
                            read.run(sa);
                        }
                    }
                } catch (Exception e) {
                    error = e;
                }
            }
        }).start();
    }
}
