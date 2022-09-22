package com.xiaomitool.v2.utility;

import com.xiaomitool.v2.process.ProcessRunner;
import com.xiaomitool.v2.resources.ResourcesConst;
import com.xiaomitool.v2.resources.ResourcesManager;
import com.xiaomitool.v2.tasks.Task;
import com.xiaomitool.v2.utility.utils.StrUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

public class MTPUtils {
    private static String WIN_MTP_EXE = "mtp";

    private static ProcessRunner winMtpRunner() {
        return new ProcessRunner(ResourcesManager.getToolPath(WIN_MTP_EXE, true));
    }

    public static HashMap<String, MTPDevice> list() throws IOException {
        HashMap<String, MTPDevice> map = new HashMap<>();
        OSNotSupportedException.requireWindows();
        ProcessRunner runner = winMtpRunner();
        runner.addArgument("list");
        runner.runWait();
        if (runner.getExitValue() != 0) {
            return map;
        }
        List<String> out = runner.getOutputLines();
        out = stripOutput(out);
        for (String line : out) {
            try {
                String id = new String(Base64.getDecoder().decode(line));
                map.put(id, new MTPDevice(id));
            } catch (Throwable t) {
                continue;
            }
        }
        return map;
    }

    public static String getRoot(MTPDevice device) throws IOException {
        OSNotSupportedException.requireWindows();
        ProcessRunner runner = winMtpRunner();
        runner.addArgument("getroot");
        runner.addArgument(Base64.getEncoder().encodeToString(device.id.getBytes(ResourcesConst.interalCharset())));
        runner.runWait();
        if (runner.getExitValue() != 0) {
            return "";
        }
        return runner.getOutputLines().get(0);
    }

    public static Task getPushTask(MTPDevice device, Path fileToPush, String destination) {
        OSNotSupportedException.requireWindows();
        ProcessRunner runner = winMtpRunner();
        runner.addArgument("push");
        runner.addArgument(Base64.getEncoder().encodeToString(device.id.getBytes(ResourcesConst.interalCharset())));
        runner.addArgument(fileToPush.toString());
        runner.addArgument(destination);
        return new Task() {
            @Override
            protected void startInternal() {
                setTotalSize(fileToPush.toFile().length());
                runner.addSyncCallback(new RunnableWithArg() {
                    @Override
                    public void run(Object arg) {
                        if (((String) arg).startsWith("*")) {
                            return;
                        }
                        long[] data = StrUtils.parseProgress((String) arg);
                        if (data == null) {
                            update(-1);
                            return;
                        }
                        update(data[0]);
                    }
                });
                try {
                    runner.runWait();
                } catch (IOException e) {
                    error(e);
                }
                if (runner.getExitValue() != 0) {
                    error(new Exception("Return code of file push not zero: " + runner.getExitValue()));
                    return;
                }
                finished(fileToPush);
            }

            @Override
            protected boolean canPause() {
                return false;
            }

            @Override
            protected boolean canStop() {
                return true;
            }

            @Override
            protected boolean pauseInternal() {
                return false;
            }

            @Override
            protected boolean stopInternal() {
                return runner.kill();
            }
        };
    }

    private static List<String> stripOutput(List<String> output) {
        ArrayList<String> list = new ArrayList<>();
        for (String line : output) {
            if (line.startsWith("*")) {
                continue;
            }
            list.add(line);
        }
        return list;
    }

    public static class MTPDevice {
        public String id;
        public String root;

        public MTPDevice(String id) {
            this.id = id;
        }
    }
}
