package com.xiaomitool.v2.procedure.retrive;

import com.xiaomitool.v2.gui.drawable.DrawableManager;
import com.xiaomitool.v2.gui.visual.ChooserPane;
import com.xiaomitool.v2.inet.CustomHttpException;
import com.xiaomitool.v2.inet.EasyHttp;
import com.xiaomitool.v2.inet.EasyResponse;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.rom.RomException;
import com.xiaomitool.v2.rom.ZipRom;
import com.xiaomitool.v2.utility.utils.NumberUtils;
import com.xiaomitool.v2.xiaomi.XiaomiUtilities;
import com.xiaomitool.v2.xiaomi.miuithings.Branch;
import com.xiaomitool.v2.xiaomi.miuithings.Codebase;
import com.xiaomitool.v2.xiaomi.miuithings.MiuiVersion;
import com.xiaomitool.v2.xiaomi.miuithings.RequestParams;
import javafx.scene.image.Image;

public class XiaomiEuRoms {
    public static final String[] SOURCEFORGE_MIRRORS = new String[]{"master"};//"astuteinternet","ayera","cfhcable","cytranet","datapacket","excellmedia","freefr","iweb","jaist","kent","liquidtelecom","netcologne","netix","newcontinuum","phoenixnap","razaoinfo","superb-dca2","superb-sea2","svwh","tenet","ufpr","vorboss"};
    
    private static final String EU_HOST = "https://basketbuild.com/uploads/devs/ZduneX25";
    public static ZipRom latest(RequestParams params) throws CustomHttpException, RomException {
        String device = params.getDevice();
        final Branch branch = params.getBranch().getDual();
        String latest = Branch.STABLE.equals(branch) ? "getversion-stables" : "getversion";
        String url = EU_HOST+"/"+latest+"/"+device+".txt";
        EasyResponse response = EasyHttp.get(url);
        if (!response.isAllRight()){
            throw new RomException("Failed to retrive xiaomi.eu rom data");
        }
        String body = response.getBody();
        String[] parts = body.split("_");
        if (parts.length < 5){
            throw new RomException("Failed to parse request output on xiaomi.eu request");
        }
        String version = parts[0];
        String md5 = parts[1];
        String codebase = parts[4];
        String mirror = SOURCEFORGE_MIRRORS[NumberUtils.getRandom(0,SOURCEFORGE_MIRRORS.length-1)];
        String format, shortVersion = Branch.STABLE.equals(branch) ? version.substring(0,4).toLowerCase() : version;
        String name = XiaomiUtilities.deviceToXiaomiEuName(device);
        MiuiVersion miuiVersion = new MiuiVersion(version);
        int bigVersion = miuiVersion.getBigVersionNumber();
        bigVersion = bigVersion > 0 ? bigVersion : 10;
        if (!device.matches("aries|cancro|virgo|hermes|mocha|latte|hennessy")){
            if (Branch.STABLE.equals(branch)){
                format = "https://%1$s.dl.sourceforge.net/project/xiaomi-eu-multilang-miui-roms/xiaomi.eu/MIUI-STABLE-RELEASES/MIUI%2$s/xiaomi.eu_multi_%3$s_%4$s_v%5$d-%6$s.zip";
            } else {
                format = "https://%1$s.dl.sourceforge.net/project/xiaomi-eu-multilang-miui-roms/xiaomi.eu/MIUI-WEEKLY-RELEASES/%2$s/xiaomi.eu_multi_%3$s_%4$s_v%5$d-%6$s.zip";
            }
        } else {
            if (Branch.STABLE.equals(branch)){
                format = "https://basketbuild.com/uploads/devs/ZduneX25/roms/MIUI%2$s/xiaomi.eu_multi_%3$s_%4$s_v%5$d-%6$s.zip";
            } else {
                format = "https://basketbuild.com/uploads/devs/ZduneX25/roms/%2$s/xiaomi.eu_multi_%3$s_%4$s_v%5$d-%6$s.zip";
            }
        }
        String downloadUrl = String.format(format,mirror,shortVersion,name,version,bigVersion,codebase);
        return new ZipRom(downloadUrl, miuiVersion, new Codebase(codebase)) {
            @Override
            public ChooserPane.Choice getChoice() {
                String text = LRes.XIAOMIEU_AUTO_DOWNLOAD.toString(), title = LRes.XIAOMIEU_TITLE.toString();

                if (this.getMiuiVersion() != null){
                    text += " - "+this.getMiuiVersion().toString();
                }
                if (branch != null){
                    title+=" - "+ LRes.branchToString(branch);
                }
                return new ChooserPane.Choice(title, text, new Image(DrawableManager.getPng("xiaomieu").toString(), true));
            }
        };
    }
}
