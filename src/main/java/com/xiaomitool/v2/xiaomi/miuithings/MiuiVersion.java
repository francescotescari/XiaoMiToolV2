package com.xiaomitool.v2.xiaomi.miuithings;

import static com.xiaomitool.v2.xiaomi.miuithings.MiuiVersion.CompareStatus.NEWER;

import com.xiaomitool.v2.utility.KeepOriginClass;
import java.util.ArrayList;
import java.util.List;

public class MiuiVersion extends KeepOriginClass {
  private Branch branch;
  private List<Integer> numbers;
  private int bigversion;
  private String charCode;
  private boolean isValid = true;

  public MiuiVersion(String version) {
    super(version);
    if (version == null) {
      return;
    }
    String[] parts = version.split("\\.");
    int numnum = parts.length;
    if (numnum == 3) {
      branch = Branch.DEVELOPER;
    } else if (numnum == 5) {
      branch = Branch.STABLE;
    } else if (numnum == 6) {
      branch = Branch.FAKE;
    } else if (numnum == 4) {
      branch = parts[0].charAt(0) == 'V' ? Branch.STABLE : Branch.UNKNOWN;
    } else {
      isValid = false;
      branch = Branch.UNKNOWN;
    }
    numbers = parseNums(parts);
  }

  public static MiuiVersion fromObject(Object miuiVersion) {
    if (miuiVersion instanceof String) {
      return new MiuiVersion((String) miuiVersion);
    } else if (miuiVersion instanceof MiuiVersion) {
      return (MiuiVersion) miuiVersion;
    } else {
      return null;
    }
  }

  public List<Integer> getNumbers() {
    return numbers;
  }

  private List<Integer> parseNums(String[] parts) {
    List<Integer> nums = new ArrayList<>();
    for (int i = 0; i < parts.length; ++i) {
      String res = parts[i].replaceAll("[^0-9]", "");
      if (!res.isEmpty()) {
        nums.add(Integer.parseInt(res));
      } else {
        charCode = parts[i];
      }
    }
    return nums;
  }

  public CompareStatus compareTo(MiuiVersion v2) {
    List<Integer> numbers2 = v2.getNumbers();
    if (numbers2.size() != numbers.size()) {
      return CompareStatus.CANNOT_COMPARE;
    }
    if (!v2.isValid || !this.isValid) {
      return CompareStatus.CANNOT_COMPARE;
    }
    for (int i = 0; i < numbers.size(); ++i) {
      int a1 = numbers.get(i);
      int a2 = numbers2.get(i);
      if (a1 > a2) {
        return CompareStatus.NEWER;
      } else if (a2 > a1) {
        return CompareStatus.OLDER;
      }
    }
    return CompareStatus.EQUAL;
  }

  public Branch getBranch() {
    return branch;
  }

  public String getBigVersion() {
    boolean lon = branch == Branch.STABLE || branch == Branch.FAKE || charCode != null;
    return lon && numbers.size() > 0 ? numbers.get(0) + "" : "";
  }

  public int getBigVersionNumber() {
    if (isValid) {
      if (Branch.STABLE.equals(this.branch)
          || Branch.FAKE.equals(this.branch) && numbers.size() > 0) {
        return numbers.get(0);
      } else {
        return this.compareTo(new MiuiVersion("8.5.25")).equals(NEWER)
            ? (this.compareTo(new MiuiVersion("9.8.30")).equals(NEWER) ? 11 : 10)
            : 9;
      }
    } else {
      return -1;
    }
  }

  @Override
  public String toString() {
    if (!isValid) {
      return this.getOrigin();
    }
    boolean lon = branch == Branch.STABLE || branch == Branch.FAKE || charCode != null;
    String r = lon ? "V" : "";
    for (int i = 0; i < numbers.size(); ++i) {
      r += numbers.get(i) + ".";
    }
    if (lon && charCode != null) {
      r += charCode + ".";
    }
    return r.substring(0, r.length() - 1);
  }

  public enum CompareStatus {
    NEWER,
    OLDER,
    CANNOT_COMPARE,
    EQUAL
  }
}
