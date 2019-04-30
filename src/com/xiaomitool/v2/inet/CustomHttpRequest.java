package com.xiaomitool.v2.inet;


import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.resources.ResourcesConst;
import com.xiaomitool.v2.utility.MultiMap;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class CustomHttpRequest {


    public enum Type {
        GET("GET"),
        POST("POST"),
        HEAD("HEAD");
        private String type;
        Type(String t){
            this.type = t;
        }
        public String getType(){
            return this.getType();
        }
    }
    private Type requestType = Type.GET;
    private LinkedHashMap<String, String> headers;
    private HttpQuery postParams;
    private String proxyHost;
    private int proxyPort = -1;
    private String url;
    private HttpResponse response;
    private HttpRequest request;
    private byte[] postData;
    private InputStream postInputStream;
    public CustomHttpRequest(){
        this(null);
    }
    public CustomHttpRequest(String url){
        this.url = url;
        headers = new LinkedHashMap<>();
    }

    public static final SSLContext TLS12;

    static {
        SSLContext c;
        try {
            c = SSLContextBuilder.create().setProtocol("TLSv1.2").build();
        } catch (Exception e) {
            c = null;

        }
        TLS12 = c;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    public void setProxy(String host, int port){
        this.proxyPort = port;
        this.proxyHost = host;
    }
    public void addHeader(String key, String value){

        this.headers.put(key,value);
    }
    public void addHeaders(Map<String,String> params){
        this.headers.putAll(params);
    }
    public void addPostField(String key, String value){
        makeItPost();
        this.postParams.put(key, value);
    }
    public void addPostFields(Map<String,?> params){
        makeItPost();
        this.postParams.putAll(params);
    }
    public void setPostData(byte[] data){
        makeItPost();
        this.postData = data;
    }
    public void setPostData(InputStream inputStream) {
        makeItPost();
        this.postInputStream = inputStream;
    }

    private void makeItPost(){
        if (this.requestType.equals(Type.POST)){
            return;
        }
        this.postParams = new HttpQuery();
        this.requestType = Type.POST;
    }

    public void execute() throws CustomHttpException {
        if (this.url == null){
            throw new CustomHttpException("Missing request url");
        }
        Log.debug("Http request on url: "+url);
        HttpClientBuilder client = HttpClientBuilder.create();
        RequestConfig.Builder configBuilder = RequestConfig.custom();
        if (proxyPort > -1){
            HttpHost proxy = new HttpHost(proxyHost, proxyPort);
          configBuilder = configBuilder.setProxy(proxy);
        }
        configBuilder.setRedirectsEnabled(false);
        RequestConfig config = configBuilder.build();

        HttpRequestBase request;
        if (requestType.equals(Type.POST)){
            request = new HttpPost(url);
            if (this.postData != null) {
                ((HttpPost) request).setEntity(new ByteArrayEntity(postData));
            } else if (this.postInputStream != null) {
                ((HttpPost) request).setEntity(new InputStreamEntity(this.postInputStream));
            } else {
                List<BasicNameValuePair> basicNameValuePairs = new LinkedList<>();
                for (Map.Entry<String, Object> entry : postParams.entrySet()) {
                    basicNameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
                }
                ((HttpPost) request).setEntity(new UrlEncodedFormEntity(basicNameValuePairs, ResourcesConst.interalCharset()));

            }

        } else if (requestType.equals(Type.GET)){
            request = new HttpGet(url);

        } else if (requestType.equals(Type.HEAD)){
            request = new HttpHead(url);
        } else {
            throw new CustomHttpException("Unknown request type: "+requestType);
        }
        this.request = request;
        request.setConfig(config);
        makeHeaders(request);
        response = execRequest(client, request);
    }

    private static HttpResponse execRequest(HttpClientBuilder client, HttpUriRequest request) throws CustomHttpException {
        return execRequest(client, request, false);
    }
    private static HttpResponse execRequest(HttpClientBuilder client, HttpUriRequest request, boolean useTLS12) throws CustomHttpException {
        if (useTLS12 && TLS12 != null){
            client.setSSLContext(TLS12);
        }
        try {
            return client.build().execute(request);
        } catch (ClientProtocolException e) {
            throw new CustomHttpException("ClientProtocolException: "+e.getMessage(), e);
        } catch (IOException e) {
            if (useTLS12 || !(e instanceof SSLHandshakeException)) {
                throw new CustomHttpException("IOException: " + e.getMessage(), e);
            } else {
                Log.debug("HandshakeException, retrying with new ssl context");
                return execRequest(client, request, true);
            }
        }
    }

    public void setHeadRequest(){
        this.requestType = Type.HEAD;
    }

    public MultiMap<String, String> getResponseHeaders() throws CustomHttpException {
        checkResponse();
        MultiMap<String, String> map = new MultiMap<>();
        for (Header h : response.getAllHeaders()){
            map.putSingle(h.getName().toLowerCase(), h.getValue());
            Log.debug("Response header: "+h.getName()+" - "+h.getValue());
        }
        return map;
    }
    public HttpEntity getResponseEntity() throws CustomHttpException, IOException {
        checkResponse();
        return response.getEntity();
    }
    public String getResponseBody() throws CustomHttpException {
        checkResponse();
        HttpEntity entity = response.getEntity();
        if (entity == null){
            return "";
        }
        try {
            return EntityUtils.toString(entity);
        } catch (IOException e) {
            throw new CustomHttpException("Cannot read response body: "+e.getMessage());
        }
    }
    public int getResponseCode() throws CustomHttpException {
        checkResponse();
        return response.getStatusLine().getStatusCode();
    }
    private void checkResponse() throws CustomHttpException {
        if (response == null){
            throw new CustomHttpException("Null HttpResponse object");
        }
    }

    private void makeHeaders(HttpRequest request){
        for (Map.Entry<String, String> entry : this.headers.entrySet()){
            request.setHeader(entry.getKey(), entry.getValue());
        }
    }

    public void abort(){
        if (request == null){
            return;
        }
        if (request instanceof HttpGet){
            ((HttpGet) request).abort();
        } else if (request instanceof  HttpPost){
            ((HttpPost) request).abort();
        } else if (request instanceof HttpHead){
            ((HttpHead) request).abort();
        }
        if (response != null){
            EntityUtils.consumeQuietly(response.getEntity());
        }
    }


}
