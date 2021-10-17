package com.xiaomitool.v2.utility.utils;

public class ObjUtils {
  public static void checkNotNull(Object object, String message) {
    if (object == null) {
      throw (message == null ? new NullPointerException() : new NullPointerException(message));
    }
  }

  public static void checkNotNull(Object object) {
    checkNotNull(object, null);
  }
}
