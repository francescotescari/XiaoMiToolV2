package com.xiaomitool.v2.rom.chooser;

import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.rom.*;

import java.util.*;

import static com.xiaomitool.v2.rom.chooser.SomethingChooser.ID_FAKE_UNOFFICIAL_ZIP;

public class InstallableChooser extends SomethingChooser<HashMap<Installable.Type, Installable>> {


    public static String idBySpecie(MiuiRom.Specie specie){
        if (specie == null){return "null";}
        return "id_specie_"+specie.toString();
    }






    public void add(String id, Installable installable){
        HashMap<Installable.Type, Installable> map = super.hashMap.get(id);
        if (map == null){
            map = new HashMap<>();
            super.add(id, map);
        }
        /*Log.debug("Add to installable chooser: "+id+", "+installable);*/
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
