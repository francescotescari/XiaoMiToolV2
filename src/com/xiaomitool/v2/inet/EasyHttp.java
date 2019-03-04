package com.xiaomitool.v2.inet;

import com.xiaomitool.v2.logging.Log;

import java.util.LinkedHashMap;
import java.util.Map;

public class EasyHttp {
    private static final boolean DEBUG_PROXY = Log.ADVANCED_LOG;
    public static final String CHROME_USERAGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.139 Safari/537.36";
    private boolean headOnly = false;
    private Map<String, String> cookies = new LinkedHashMap<>();

    protected CustomHttpRequest request = new CustomHttpRequest();
    public EasyHttp url(String url){
        request.setUrl(url);
        return this;
    }
    public EasyHttp setHeadOnly(){
        headOnly = true;
        return this;
    }
    public EasyHttp header(String key, String value){
        request.addHeader(key, value);
        return this;
    }
    public EasyHttp headers(Map<String, String> headers){
        request.addHeaders(headers);
        return this;
    }
    public EasyHttp field(String key, String value){
        request.addPostField(key, value);
        return this;
    }
    public EasyHttp fields(Map<String, ?> fields){
        request.addPostFields(fields);
        return this;
    }
    public EasyHttp proxy(String host, int port){
        request.setProxy(host, port);
        return this;
    }
    public EasyHttp cookie(String key, String value){
        this.cookies.put(key, value);
        return this;
    }
    public EasyHttp cookies(Map<String, String> cookies){
        this.cookies.putAll(cookies);
        return this;
    }
    public CustomHttpRequest getHttpRequestObj(){
        return request;
    }


    public EasyHttp referer(String referer){
        return this.header("Referer",referer);
    }
    public EasyResponse exec() throws CustomHttpException {
        if (DEBUG_PROXY){
            this.proxy("127.0.0.1",8888);
        }
        if (cookies.size() > 0){
            String cookieString = "";
            for (Map.Entry<String, String> entry : cookies.entrySet()){
                cookieString+=entry.getKey()+"="+entry.getValue()+"; ";
            }
            request.addHeader("Cookie", cookieString);
        }
        if (headOnly){
            request.setHeadRequest();
        }
        request.execute();
        EasyResponse response = new EasyResponse(request.getResponseHeaders(), request.getResponseBody(), request.getResponseCode());
        return response;
    }
    public static EasyResponse get(String url) throws CustomHttpException {
        return (new EasyHttp()).url(url).exec();
    }
    public EasyHttp userAgent(String userAgent){
        return this.header("User-Agent", userAgent);
    }

}
