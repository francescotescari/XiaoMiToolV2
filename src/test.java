import com.xiaomitool.v2.adb.device.DeviceGroups;
import com.xiaomitool.v2.inet.CustomHttpException;
import com.xiaomitool.v2.utility.utils.StrUtils;
import com.xiaomitool.v2.xiaomi.XiaomiProcedureException;
import org.json.JSONObject;

import java.util.*;

import static com.xiaomitool.v2.adb.device.DeviceGroups.stripCodename;
import static com.xiaomitool.v2.xiaomi.romota.MiuiRomOta.deviceNames_request;

public class test {
    public static void main(String... args) throws Exception {
        JSONObject obj = deviceNames_request();
        HashSet<String> devices = new HashSet<>();
        Map<String, String> res = new HashMap<>();
        Map<String, Integer> len = new HashMap<>();
        for (String name : obj.keySet()){
            String c = stripCodename(name);
            String n = obj.getJSONObject(name).getString("display_name_en");
            boolean chin = false;
            if (StrUtils.isNullOrEmpty(n)){
                n = obj.getJSONObject(name).getString("display_name");
                chin = true;
            }
            int a = n.length();
            if (chin){
                a+=100;
            }
            Integer l = len.get(c);
            if (l != null){
                if (l < a){
                    continue;
                }
            }
            len.put(c, a);
            res.put(c, n);
        }
        ArrayList<Map.Entry<String, String>> so = new ArrayList<Map.Entry<String, String>>(res.entrySet());
        so.sort(new Comparator<Map.Entry<String, String>>() {
            @Override
            public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                return o1.getValue().compareToIgnoreCase(o2.getValue());
            }
        });
        for (Map.Entry<String, String> e : so){
            System.out.println("map.put(\""+e.getKey()+"\",\""+e.getValue()+"\");");
        }

    }
}
