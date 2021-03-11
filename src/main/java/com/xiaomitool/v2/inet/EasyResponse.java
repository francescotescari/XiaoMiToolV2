package com.xiaomitool.v2.inet;

import com.xiaomitool.v2.utility.MultiMap;

import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.http.HttpStatus.SC_OK;

public class EasyResponse {
    MultiMap<String, String> headers;
    String body;
    int code;

    public EasyResponse(MultiMap<String, String> headers, String body, int code) {
        this.headers = headers;
        this.body = body;
        this.code = code;
    }

    public String getBody() {
        return body;
    }

    public int getCode() {
        return code;
    }

    public MultiMap<String, String> getHeaders() {
        return headers;
    }

    public HashMap<String, String> getCookies() {
        HashMap<String, String> toReturn = new HashMap<>();
        List<String> rawCookies = headers.get("set-cookie");
        if (rawCookies == null) {
            return toReturn;
        }
        Pattern cformat = Pattern.compile("\\s*([^=]+)\\s*=\\s*([^;]*)");
        for (String raw : rawCookies) {
            Matcher m = cformat.matcher(raw);
            if (m.find()) {
                String key = m.group(1);
                String value = m.group(2);
                toReturn.merge(key, value, new BiFunction<String, String, String>() {
                    @Override
                    public String apply(String s, String s2) {
                        if (s.length() < s2.length()) {
                            return s2;
                        }
                        return s;
                    }
                });
            }
        }
        return toReturn;
    }

    public boolean isAllRight() {
        return !body.isEmpty() && code == SC_OK;
    }

    public boolean isCodeRight() {
        return code == SC_OK;
    }
}
