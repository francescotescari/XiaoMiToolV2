package com.xiaomitool.v2.procedure.retrive;

import com.xiaomitool.v2.inet.CustomHttpException;
import com.xiaomitool.v2.inet.EasyHttp;
import com.xiaomitool.v2.inet.EasyResponse;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.rom.RomException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AfhRequest {
    private static final String AFH_HOST = "https://androidfilehost.com";
    private static final String AFH_MIRRORS = AFH_HOST+"/libs/otf/mirrors.otf.php";
    private static final String AFH_CHECK = AFH_HOST+"/libs/otf/checks.otf.php";
    private static final String AFH_API = AFH_HOST+"/api/";
    public static String getDownloadLink(String fid) throws CustomHttpException, RomException {
        String url = AFH_HOST+"/?fid="+fid;
        EasyResponse response = new EasyHttp().url(url).referer(AFH_HOST).userAgent(EasyHttp.CHROME_USERAGENT).exec();
        if (!response.isAllRight()){
            throw new RomException("AfhRequest fid failed: code: "+response.getCode());
        }
        HashMap<String, String> cookies = response.getCookies();
        HashMap<String, String> fields = new HashMap<>();
        /*fields.put("w","waitingtime");
        response = new EasyHttp().url(AFH_CHECK).referer(url).cookies(cookies).fields(fields).userAgent(EasyHttp.CHROME_USERAGENT).header("X-MOD-SBB-CTYPE","xhr").header("X-REQUESTED-WITH","XMLHttpRequest").exec();
        if (!response.isAllRight()){
            throw new RomException("AfhRequest mirrors fetch failed: code: "+response.getCode());
        }
        fields = new HashMap<>();*/
        fields.put("submit","submit");
        fields.put("action","getdownloadmirrors");
        fields.put("fid",fid);
        response = new EasyHttp().url(AFH_MIRRORS).referer(url).cookies(cookies).fields(fields).userAgent(EasyHttp.CHROME_USERAGENT).header("X-MOD-SBB-CTYPE","xhr").header("X-REQUESTED-WITH","XMLHttpRequest").exec();
        if (!response.isAllRight()){
            throw new RomException("AfhRequest mirrors fetch failed: code: "+response.getCode());
        }
        try {
            String body = response.getBody();
            JSONObject json = new JSONObject(body);
            JSONArray array = json.getJSONArray("MIRRORS");
            return array.getJSONObject(0).getString("url").trim();
        } catch (Throwable t){
            throw new RomException("Failed to parse AfhRequest fid: "+t.getMessage());
        }
    }

    public static List<AfhEntry> listDirFiles(String dirId) throws CustomHttpException, RomException {
        String url = AFH_API+"?action=folder&flid="+dirId;
        EasyResponse response = new EasyHttp().url(url).userAgent(EasyHttp.CHROME_USERAGENT).referer(AFH_HOST).exec();
        if (!response.isAllRight()){
            throw new RomException("AfhRequest listDir failed: code: "+response.getCode());
        }
        List<AfhEntry> list = new ArrayList<>();
        try {
            JSONObject object = new JSONObject(response.getBody());
            JSONArray array = object.getJSONObject("DATA").getJSONArray("files");
            for (int i = 0; i < array.length(); ++i) {
                try {
                    JSONObject obj = array.getJSONObject(i);
                    String filename = obj.getString("name"), fid = obj.getString("fid"), upload = obj.getString("upload_date");
                    Long up = Long.parseLong(upload);
                    list.add(new AfhEntry(filename, fid, up));

                } catch (Throwable t) {
                    Log.debug("Failed to parse Afh listDir entry: " + t.getMessage());
                }
            }
        } catch (Throwable t){
            throw new RomException("Failed to parse AfhRequest listDir: "+t.getMessage());
        }
        return list;
    }

    public static class AfhEntry {
        private String filename, fid;
        private Long uploadDate;
        AfhEntry(String filename, String fid, Long uploadDate){
            this.filename = filename;
            this.fid = fid;
            this.uploadDate = uploadDate;
        }

        public Long getUploadDate() {
            return uploadDate;
        }

        public String getFid() {
            return fid;
        }

        public String getFilename() {
            return filename;
        }
    }

}
