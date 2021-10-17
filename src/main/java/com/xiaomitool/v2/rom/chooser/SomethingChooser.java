package com.xiaomitool.v2.rom.chooser;

import static com.xiaomitool.v2.rom.chooser.InstallableChooser.idBySpecie;

import com.xiaomitool.v2.rom.MiuiRom;
import java.util.*;

public abstract class SomethingChooser<T> {
  public static final String ID_FAKE_UNOFFICIAL_ZIP = "unoffzip";
  public static final String ID_FAKE_MOD_ZIP = "fmzip";
  public static final String ID_FAKE_OFFICIAL = "official";
  public static final String ID_UNLOCK_DEVICE = "unlock";
  public static final String ID_INSTALL_TWRP = "itwrp";
  public static final String ID_INSTALL_RECOVERY_IMAGE = "irecimage";
  public static final String ID_INSTALL_MAGISK = "imagisk";
  public static final String ID_XIAOMIEU_STABLE = "eusta";
  public static final String ID_XIAOMIEU_DEV = "eudev";
  public static final String ID_BACK = "back";
  protected LinkedHashMap<String, T> hashMap = new LinkedHashMap<>();

  public void add(String id, T obj) {
    this.hashMap.put(id, obj);
  }

  public T get(String id) {
    return this.hashMap.get(id);
  }

  public Set<Map.Entry<String, T>> entrySet() {
    return this.hashMap.entrySet();
  }

  public static class IdGroup extends LinkedHashSet<String> {
    public static final IdGroup officialRom = new IdGroup("Official roms");
    public static final IdGroup unofficialRoms =
        new IdGroup(
            "Unofficial roms",
            ID_XIAOMIEU_STABLE,
            ID_XIAOMIEU_DEV,
            ID_FAKE_UNOFFICIAL_ZIP,
            ID_BACK);
    public static final IdGroup xiaomiProcedures =
        new IdGroup("Xiaomi Procedures", ID_UNLOCK_DEVICE, ID_BACK);
    public static final IdGroup modsAndStuff =
        new IdGroup(
            "Mods and stuff",
            ID_INSTALL_TWRP,
            ID_INSTALL_MAGISK,
            ID_FAKE_MOD_ZIP,
            ID_INSTALL_RECOVERY_IMAGE,
            ID_BACK);

    static {
      for (MiuiRom.Specie specie : MiuiRom.Specie.values()) {
        officialRom.add(idBySpecie(specie));
      }
      officialRom.add(ID_FAKE_OFFICIAL);
      officialRom.add(ID_BACK);
    }

    private String name;

    public IdGroup(String name, String... ids) {
      this.name = name;
      this.addAll(Arrays.asList(ids));
    }

    public String getName() {
      return name;
    }

    public boolean hasId(String id) {
      return this.contains(id);
    }
  }
}
