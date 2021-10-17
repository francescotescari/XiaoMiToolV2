package com.xiaomitool.v2.utility;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class MultipleInputStream extends InputStream {
  private final List<InputStream> streams = new LinkedList<>();
  private InputStream currentInputStream;
  private int currentIndex = -1;

  public MultipleInputStream(Collection<InputStream> streams) {
    if (streams.size() == 0) {
      throw new IllegalArgumentException("At least one stream is required");
    }
    this.streams.addAll(streams);
    incrementCurrent();
  }

  public MultipleInputStream(InputStream... streams) {
    if (streams.length == 0) {
      throw new IllegalArgumentException("At least one stream is required");
    }
    this.streams.addAll(Arrays.asList(streams));
    incrementCurrent();
  }

  @Override
  public int read() throws IOException {
    int by = currentInputStream.read();
    if (by < 0) {
      if (incrementCurrent()) {
        return read();
      }
    }
    return by;
  }

  @Override
  public int read(byte[] data) throws IOException {
    int read = currentInputStream.read(data);
    if (read <= 0) {
      if (incrementCurrent()) {
        return read(data);
      }
    }
    return read;
  }

  @Override
  public int read(byte[] data, int off, int len) throws IOException {
    int read = currentInputStream.read(data, off, len);
    if (read <= 0) {
      if (incrementCurrent()) {
        return read(data, off, len);
      }
    }
    return read;
  }

  private boolean incrementCurrent() {
    if (++currentIndex >= streams.size()) {
      return false;
    }
    currentInputStream = streams.get(currentIndex);
    return true;
  }
}
