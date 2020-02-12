package com.xiaomitool.v2.xiaomi.miuithings;

import com.xiaomitool.v2.adb.device.Device;
import com.xiaomitool.v2.adb.device.DeviceGroups;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.rom.MiuiRom;
import com.xiaomitool.v2.utility.Nullable;
import com.xiaomitool.v2.xiaomi.XiaomiUtilities;

import org.json.JSONObject;

import java.util.HashMap;

public class DefaultRequestParams extends RequestParams implements Cloneable {

    public DefaultRequestParams(String device, String version, String codebase){
        this(device,version,codebase,null);
    }

    public DefaultRequestParams(String device, String version, String codebase, @Nullable Branch branch){
        super.device = XiaomiUtilities.stripCodename(device);
        super.version = new MiuiVersion(version);
        super.codebase = new Codebase(codebase);
        super.branch =  (branch == null ? super.version.getBranch() : branch);
        super.specie = MiuiRom.Specie.fromStringBranch(device, super.branch);
        super.zone = specie == null ? 0 : specie.getZone();

        androidHash = "00000000000000000000000000000000";
        language = "en_US";
        board ="";
        imeiHash = "0000000000000000000000000000000000000000";
        f=1;
        region="CN";
        carrier="";
        is_cts =0;
        a=a;
        isR=0;
        serialNumber=SerialNumber.SN_ZERO;
        sys=0;
        unlockStatus= UnlockStatus.UNLOCKED;
        packageHash=null;
        hashId="0000000000000000";
        abOption=0;
        express=0;
        id="";
    }

    public String buildJson() throws Exception {
        if (version == null || codebase == null || device == null || serialNumber == null){
            throw new Exception("Failed to build json: missing required parameter");
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("v","MIUI-"+version.toString());
        map.put("bv",version.getBigVersion());
        map.put("c",codebase.toString());
        map.put("d",specie.buildModDevice(device));
        map.put("g",androidHash);
        if (!isInternational()){
            map.put("i",imeiHash);
            map.put("p",board);
        }
        map.put("b",specie.getBranch().getCode());
        map.put("l",language);
        map.put("f",f);
        map.put("r",region);
        map.put("n",carrier);
        map.put("cts",is_cts);
        map.put("id", id);
        map.put("a",a);
        map.put("isR",isR);
        map.put("sn",serialNumber == null ? SerialNumber.SN_ZERO.toHexString() : serialNumber.toHexString());
        map.put("sys",sys);
        map.put("unlock",unlockStatus.getInt());
        if(packageHash != null){
            map.put("pkg",packageHash);
        }
        HashMap<String, Object> options = new HashMap<>();
        /*Log.debug("Devi2 zone: "+zone);*/
        if (zone != 0) {
            options.put("zone", zone);
        }
        if (!isInternational()){
            options.put("hashId",hashId);
        }
        options.put("ab",abOption);
        options.put("express",express);
        JSONObject optJson = new JSONObject(options);
        map.put("options",optJson);
        JSONObject json = new JSONObject(map);
        String ret = json.toString();
        /*Log.debug(ret);*/
        return ret;

    }
    public DefaultRequestParams clone() throws CloneNotSupportedException {
        DefaultRequestParams clone = (DefaultRequestParams) super.clone();
        return clone;

    }

    public void setSerialNumber(SerialNumber sn) {
        this.serialNumber = sn;
    }
}
