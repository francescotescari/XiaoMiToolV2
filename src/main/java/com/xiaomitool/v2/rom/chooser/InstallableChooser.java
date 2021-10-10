package com.xiaomitool.v2.rom.chooser;

import com.xiaomitool.v2.rom.Installable;
import com.xiaomitool.v2.rom.MiuiRom;
import java.util.HashMap;

public class InstallableChooser extends SomethingChooser<HashMap<Installable.Type, Installable>> {
  public static String idBySpecie(MiuiRom.Specie specie) {
    if (specie == null) {
      return "null";
    }
    return "id_specie_" + specie.toString();
  }

  public void add(String id, Installable installable) {
    HashMap<Installable.Type, Installable> map = super.hashMap.get(id);
    if (map == null) {
      map = new HashMap<>();
      super.add(id, map);
    }
    map.put(installable.getType(), installable);
  }

  public Installable getByType(String id, Installable.Type type) {
    HashMap<Installable.Type, Installable> map = super.hashMap.get(id);
    if (map == null || map.size() == 0) {
      return null;
    }
    return map.get(type);
  }
}
