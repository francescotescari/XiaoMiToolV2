package com.xiaomitool.v2.procedure.retrive;

import com.xiaomitool.v2.inet.CustomHttpException;
import com.xiaomitool.v2.inet.EasyHttp;
import com.xiaomitool.v2.inet.EasyResponse;
import com.xiaomitool.v2.rom.Installable;
import com.xiaomitool.v2.rom.RomException;
import com.xiaomitool.v2.rom.TwrpFile;
import com.xiaomitool.v2.xiaomi.miuithings.RequestParams;
import org.json.JSONArray;

import java.util.Comparator;
import java.util.List;

public class Twrp {
    private static final String TWRP_DL_HOST = "https://dl.twrp.me";
    private static final String TWRP_AFH_FID = "50678";

    public static TwrpFile latestTwrpMe(String codename) throws CustomHttpException, RomException {
        String dlUrl = TWRP_DL_HOST+"/"+codename+"/dl.json";
        EasyResponse response = EasyHttp.get(dlUrl);
        if (!response.isAllRight()){
            throw new RomException("Failed to retrive twrp json data");
        }
        String latestFile;
        try {
            JSONArray object = new JSONArray(response.getBody());
            latestFile = object.getJSONObject(0).getString("filename");
        } catch (Throwable t){
            throw new RomException("Failed to parse twrp json data");
        }
        String url = TWRP_DL_HOST+"/"+codename+"/"+latestFile;
        response = new EasyHttp().referer(url+".html").setHeadOnly().url(url).exec();
        if (!response.isCodeRight()){
            throw new RomException("Found download url is not valid");
        }
        return new TwrpFile(url, codename);
    }

    public static TwrpFile latestTwrpAfh(String codename) throws CustomHttpException, RomException {
        List<AfhRequest.AfhEntry> entries = AfhRequest.listDirFiles(TWRP_AFH_FID);
        entries.sort(new Comparator<AfhRequest.AfhEntry>() {
            @Override
            public int compare(AfhRequest.AfhEntry o1, AfhRequest.AfhEntry o2) {
                return Long.compare(o2.getUploadDate(),o1.getUploadDate());
            }
        });
        String toFind = "twrp_"+codename;
        String betterFid = null;
        for (AfhRequest.AfhEntry entry : entries){
            String name = entry.getFilename().toLowerCase();
            if (name.contains(toFind)){
                betterFid = entry.getFid();
                break;
            } else if (betterFid == null && name.contains(codename)){
                betterFid = entry.getFid();
            }
        }
        if (betterFid == null){
            throw new RomException("Twrp file not found in file list");
        }
        String downloadUrl = AfhRequest.getDownloadLink(betterFid);
        return new TwrpFile(downloadUrl, codename);
    }
}
