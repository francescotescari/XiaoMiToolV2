package com.xiaomitool.v2.utility;

public class Thrower<T extends Throwable> {
  private T throwable;

  public void set(T throwable) {
    if (this.throwable == null) this.throwable = throwable;
  }

  public void check() throws T {
    if (this.throwable != null) {
      throw this.throwable;
    }
  }
}
