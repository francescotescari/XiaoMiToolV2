package com.xiaomitool.v2.utility;

import java.io.IOException;
import java.io.InputStream;

public class FeedbackInputStream extends InputStream {
  private InputStream stream;
  private RunnableWithArg feedback;

  public FeedbackInputStream(InputStream stream, RunnableWithArg feedbackFunc) {
    this.stream = stream;
    this.feedback = feedbackFunc;
  }

  @Override
  public int read() throws IOException {
    feedback.run(1);
    return stream.read();
  }

  @Override
  public int read(byte[] b) throws IOException {
    int size = stream.read(b);
    feedback.run(size);
    return size;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    int size = stream.read(b, off, len);
    feedback.run(size);
    return size;
  }
}
