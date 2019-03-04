package com.xiaomitool.v2.inet;

import com.xiaomitool.v2.utility.utils.InetUtils;


import java.util.LinkedHashMap;
import java.util.Map;

public class HttpQuery extends LinkedHashMap<String, Object> {
    @Override
    public Object put(String key, Object value){
        if (value == null){
            value = "null";
        }
        return super.put(key,value);
    }


    public String toEncodedString(boolean encode) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : super.entrySet()){
            builder.append(entry.getKey()).append('=').append(encode ? InetUtils.urlEncode(entry.getValue().toString()) : entry.getValue().toString()).append('&');
        }
        builder.setLength(builder.length()-1);
        return builder.toString();
    }

    @Override
    public String toString(){
        return toEncodedString(false);
    }
}
