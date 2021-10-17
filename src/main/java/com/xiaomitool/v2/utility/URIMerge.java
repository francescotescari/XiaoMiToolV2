package com.xiaomitool.v2.utility;

public class URIMerge {
  String uri;
  String delimiter = "/";

  public URIMerge() {
    uri = "";
  }

  public URIMerge(String uri) {
    this.uri = uri;
  }

  public URIMerge(String uri, String delimiter) {
    this.uri = uri;
    this.delimiter = delimiter;
  }

  public URIMerge resolve(String path) {
    if (!uri.endsWith(delimiter)) {
      uri += delimiter;
    }
    while (path.startsWith(delimiter)) {
      path = path.substring(1);
    }
    uri += path;
    return this;
  }

  @Override
  public String toString() {
    return uri;
  }
}
