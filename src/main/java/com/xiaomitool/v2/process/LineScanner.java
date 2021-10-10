package com.xiaomitool.v2.process;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

public class LineScanner {
  private final LinkedList<String> linesBuffer = new LinkedList<>();
  private final byte[] buffer = new byte[1024];
  private InputStream inputStream;
  private boolean isFinished = false;
  private StringBuilder builder;

  public LineScanner(InputStream stream) {
    this.inputStream = stream;
  }

  public boolean hasNextLine() throws IOException {
    if (!linesBuffer.isEmpty()) {
      return true;
    }
    if (builder != null) {
      return true;
    }
    if (isFinished) {
      return false;
    }
    while (!isFinished && linesBuffer.isEmpty()) {
      read();
    }
    return hasNextLine();
  }

  private void read() throws IOException {
    int len = inputStream.read(buffer);
    if (len <= 0) {
      isFinished = true;
      return;
    }
    String data = new String(buffer, 0, len);
    String[] lines = data.split("(\\r|\\n)");
    if (builder != null) {
      builder.append(lines[0]);
      if (lines.length > 1) {
        lines[0] = builder.toString();
      }
    }
    builder = new StringBuilder(lines[lines.length - 1]);
    for (int i = 0; i < lines.length - 1; ++i) {
      linesBuffer.add(lines[i]);
    }
  }

  public String nextLine() throws IOException {
    if (!linesBuffer.isEmpty()) {
      return linesBuffer.pollFirst();
    }
    if (isFinished) {
      if (builder != null) {
        String res = builder.toString();
        builder = null;
        return res;
      } else {
        return null;
      }
    }
    while (!isFinished && linesBuffer.isEmpty()) {
      read();
    }
    return nextLine();
  }
}
