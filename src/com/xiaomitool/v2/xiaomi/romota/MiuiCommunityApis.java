package com.xiaomitool.v2.xiaomi.romota;

import com.xiaomitool.v2.inet.CustomHttpException;
import com.xiaomitool.v2.inet.EasyHttp;
import com.xiaomitool.v2.inet.EasyResponse;
import com.xiaomitool.v2.xiaomi.XiaomiProcedureException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MiuiCommunityApis {
    private static final String IDS_URL = "http://c.mi.com/oc/rom/getphonelist";

    public static Map<String, Integer> getDevicesCommunityIds() throws CustomHttpException, XiaomiProcedureException {
        EasyResponse response = EasyHttp.get(IDS_URL);
        HashMap<String, Integer> result = new HashMap<>();
        if (!response.isAllRight()){
            throw new XiaomiProcedureException("Invalid response code or body", XiaomiProcedureException.ExceptionCode.EXCEPTION, response.getBody());
        }
        try {
            JSONObject jsonObject = new JSONObject(response.getBody());
            int error = jsonObject.optInt("errno", -1);
            if (error != 0){
                throw new XiaomiProcedureException("Invalid api error number: "+error);
            }
            JSONArray devices = jsonObject.getJSONObject("data").getJSONObject("phone_data").getJSONArray("phone_list");
            for (int i = 0; i<devices.length(); ++i){
                JSONObject data = devices.getJSONObject(i);
                result.put(data.getString("name"), data.getInt("id"));
            }
        } catch (Throwable t){
            throw new XiaomiProcedureException("Failed to parse community api response: "+t.getMessage(), XiaomiProcedureException.ExceptionCode.EXCEPTION, t.toString());
        }
        return result;
    }
}
