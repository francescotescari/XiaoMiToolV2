package com.xiaomitool.v2.adb;

import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.process.AdbRunner;
import com.xiaomitool.v2.process.ProcessRunner;
import com.xiaomitool.v2.resources.ResourcesManager;
import com.xiaomitool.v2.utility.utils.StrUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdbCommons {
  private static final int DEFAULT_TIMEOUT = 6;
  private static final String CHECK_RETURN_CODE = "return_code_is_not_zero";
  private static final HashMap<String, String> LAST_ERROR_MAP = new HashMap<>();

  private static String command_string(String cmd) {
    return command(cmd, null);
  }

  private static String command_string(String cmd, String device) {
    ProcessRunner runner = adb_command(cmd, device, DEFAULT_TIMEOUT);
    if (runner == null) {
      return null;
    }
    return runner.getOutputString();
  }

  private static List<String> command_list(String cmd) {
    return command_list(cmd, null);
  }

  private static List<String> command_list(String cmd, String device) {
    ProcessRunner runner = adb_command(cmd, device, DEFAULT_TIMEOUT);
    if (runner == null) {
      return null;
    }
    return runner.getOutputLines();
  }

  public static String command(String cmd, String device) {
    return command(cmd, device, DEFAULT_TIMEOUT);
  }

  public static String command(String cmd, String device, int timeout) {
    AdbRunner runner = adb_command(cmd, device, timeout);
    if (runner == null) {
      return null;
    }
    return runner.getOutputString();
  }

  private static AdbRunner adb_command(String cmd, String device, int timeout) {
    AdbRunner runner = new AdbRunner();
    if (device != null) {
      runner.setDeviceSerial(device);
    }
    List<String> list = new ArrayList<String>();
    Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(cmd);
    while (m.find()) {
      list.add(m.group(1));
    }
    for (String arg : list) {
      runner.addArgument(arg);
    }
    try {
      runner.runWait(timeout);
    } catch (IOException e) {
      return null;
    }
    int exitCode = runner.getExitValue();
    if (exitCode == 0) {
      LAST_ERROR_MAP.put(device, null);
    } else {
      List<String> outlines = runner.getOutputLines();
      String output =
          outlines != null && !outlines.isEmpty()
              ? outlines.get(outlines.size() - 1)
              : "unknown error";
      LAST_ERROR_MAP.put(device, output);
    }
    return runner;
  }

  public static List<String> devices() {
    return command_list("devices");
  }

  public static String start_server() {
    return command_string("start-server");
  }

  public static String kill_server() {
    return command_string("kill-server");
  }

  public static List<String> getProps(String device) {
    return command_list("shell -x getprop", device);
  }

  public static boolean fileExists(String path, String device) {
    String output = adb_shellWithOr("ls " + path, device, DEFAULT_TIMEOUT);
    return output != null;
  }

  public static boolean formatBootdevicePartition(String partition, String device, int timeout) {
    return adb_shellWithOr(
            "mke2fs -T ext4 /dev/block/bootdevice/by-name/" + partition, device, timeout)
        != null;
  }

  public static String adb_shell(String cmd, String device, int timeout) {
    AdbRunner runner = new AdbRunner("shell", "-x", cmd);
    runner.setDeviceSerial(device);
    try {
      if (runner.runWait(timeout) != 0) {
        Log.error("Adb shell command returned " + runner.getExitValue());
      }
    } catch (IOException e) {
      runner = null;
    }
    if (runner == null || runner.getExitValue() != 0) {
      return null;
    }
    return runner.getOutputString();
  }

  public static String adb_shellWithOr(String cmd, String device, int timeout) {
    cmd += " || echo " + CHECK_RETURN_CODE;
    String output = adb_shell(cmd, device, timeout);
    if (output == null) {
      return null;
    } else if (output.contains(CHECK_RETURN_CODE)) {
      LAST_ERROR_MAP.put(cmd, StrUtils.lastLine(output));
      return null;
    }
    return output;
  }

  public static File simplePull(String device, String pullPath, String destPath) {
    Path tmpPath = ResourcesManager.getTmpPath();
    if (!tmpPath.toFile().exists()) {
      try {
        Files.createDirectories(tmpPath);
      } catch (IOException e) {
        Log.warn("Cannot create tmp dir: " + tmpPath.toString() + " : " + e.getMessage());
        return null;
      }
    }
    Path dest = tmpPath.resolve(destPath);
    AdbRunner runner =
        adb_command("pull " + pullPath + " \"" + dest.toString() + "\"", device, 3600);
    if (runner == null) {
      return null;
    }
    if (runner.getExitValue() != 0) {
      return null;
    }
    return dest.toFile();
  }

  public static String raw(String device, String command) {
    return raw(device, command, 30);
  }

  public static String raw(String device, String command, int timeout) {
    AdbRunner runner = adb_command("raw " + command, device, timeout);
    if (runner == null || runner.getExitValue() != 0) {
      return null;
    }
    return runner.getOutputString();
  }

  public static String cat(String device, String file) {
    return adb_shellWithOr("cat " + file, device, DEFAULT_TIMEOUT);
  }

  public static String reboot(String device, String status) {
    return command_string("reboot " + status, device);
  }

  public static AdbRunner runner(String cmd, String device, int timeout) {
    return adb_command(cmd, device, timeout);
  }

  public static boolean mkdir(String path, String serial) {
    return adb_shellWithOr("mkdir \"" + path + "\"", serial, 5) != null;
  }

  public static String getLastError(String serial) {
    return String.valueOf(LAST_ERROR_MAP.get(serial));
  }
}
