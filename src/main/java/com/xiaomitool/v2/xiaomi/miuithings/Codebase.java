package com.xiaomitool.v2.xiaomi.miuithings;

import com.xiaomitool.v2.utility.KeepOriginClass;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Codebase extends KeepOriginClass {
  public static final Codebase A4_4 = new Codebase("4.4");
  public static final Codebase A5_0 = new Codebase("5.0");
  public static final Codebase A5_1 = new Codebase("5.1");
  public static final Codebase A6_0 = new Codebase("6.0");
  public static final Codebase A7_0 = new Codebase("7.0");
  public static final Codebase A7_1 = new Codebase("7.1");
  public static final Codebase A8_0 = new Codebase("8.0");
  public static final Codebase A8_1 = new Codebase("8.1");
  public static final Codebase A9_0 = new Codebase("9.0");
  public static final Codebase A10_0 = new Codebase("10.0");
  public static final HashMap<Integer, Codebase> KNOWN_CODEBASE = new HashMap<>(10);

  static {
    KNOWN_CODEBASE.put(40, A4_4);
    KNOWN_CODEBASE.put(50, A5_0);
    KNOWN_CODEBASE.put(51, A5_1);
    KNOWN_CODEBASE.put(60, A6_0);
    KNOWN_CODEBASE.put(70, A7_0);
    KNOWN_CODEBASE.put(71, A7_1);
    KNOWN_CODEBASE.put(80, A8_0);
    KNOWN_CODEBASE.put(81, A8_1);
    KNOWN_CODEBASE.put(90, A9_0);
    KNOWN_CODEBASE.put(100, A10_0);
  }

  public boolean isValid = true;
  private Integer main;
  private Integer sub;
  private Integer revision;

  public Codebase(String version) {
    super(version);
    if (version == null) {
      return;
    }
    Pattern p = Pattern.compile("(\\d)\\.(\\d)\\.{0,1}(\\d{0,1})");
    Matcher m = p.matcher(version);
    if (!m.matches()) {
      isValid = false;
      return;
    }
    main = Integer.parseInt(m.group(1));
    sub = Integer.parseInt(m.group(2));
    String third = m.group(3);
    revision = !third.equals("") ? Integer.parseInt(m.group(3)) : null;
  }

  public int getMain() {
    return main;
  }

  public int getSub() {
    return sub;
  }

  public Codebase next() {
    if (!isValid) {
      return null;
    }
    int code = main * 10 + sub;
    Codebase res = KNOWN_CODEBASE.get(code + 1);
    if (res == null) {
      code = code - (code % 10);
      res = KNOWN_CODEBASE.get(code + 10);
    }
    return res;
  }

  public List<Codebase> nexts(int max) {
    ArrayList<Codebase> list = new ArrayList<>(max);
    list.add(this);
    while (list.size() < max) {
      Codebase c = next();
      if (c == null) {
        break;
      }
      list.add(c);
    }
    return list;
  }

  int compare(Codebase v2) {
    if (!isValid || !v2.isValid) {
      return -2;
    }
    return main > v2.getMain()
        ? 1
        : (main < v2.getMain() ? -1 : (sub > v2.getSub() ? 1 : (sub < v2.getSub() ? -1 : 0)));
  }

  @Override
  public String toString() {
    if (!isValid) {
      return super.toString();
    }
    return main + "." + sub + (revision != null ? "." + revision : "");
  }
}
