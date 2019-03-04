package com.xiaomitool.v2.rom.chooser;

import com.xiaomitool.v2.rom.*;

import java.util.*;

import static com.xiaomitool.v2.rom.chooser.SomethingChooser.ID_FAKE_UNOFFICIAL_ZIP;

public class InstallableChooser extends SomethingChooser<HashMap<Installable.Type, Installable>> {


    public static String idBySpecie(MiuiRom.Specie specie){
        if (specie == null){return "null";}
        switch (specie){
            case CHINA_STABLE:
                return SomethingChooser.ID_CHINA_STABLE;
            case GLOBAL_STABLE:
                return SomethingChooser.ID_GLOBAL_STABLE;
            case CHINA_DEVELOPER:
                return SomethingChooser.ID_CHINA_DEVELOPER;
            case GLOBAL_DEVELOPER:
                return SomethingChooser.ID_GLOBAL_DEVELOPER;
        }
        return specie.toString();
    }






    public void add(String id, Installable installable){
        HashMap<Installable.Type, Installable> map = super.hashMap.get(id);
        if (map == null){
            map = new HashMap<>();
            super.hashMap.put(id, map);
        }
        map.put(installable.getType(), installable);

    }


    public Installable getByType(String id, Installable.Type type){
        HashMap<Installable.Type, Installable> map = super.hashMap.get(id);
        if (map == null || map.size() == 0){
            return null;
        }
        return map.get(type);
    }





}
