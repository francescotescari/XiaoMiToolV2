package com.xiaomitool.v2.utility.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;

public class CompressUtils {
  public static byte[] compress(byte[] data) throws IOException {
    return paddedCompress(data, 0);
  }

  public static byte[] paddedCompress(byte[] data, int padding) throws IOException {
    Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION, true);
    deflater.setInput(data);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length + padding);
    deflater.finish();
    byte[] buffer = new byte[1024];
    while (padding > 0) {
      outputStream.write(0);
      padding--;
    }
    while (!deflater.finished()) {
      int count = deflater.deflate(buffer);
      outputStream.write(buffer, 0, count);
    }
    outputStream.close();
    return outputStream.toByteArray();
  }
}
