package com.xiaomitool.v2.procedure.retrive;

import com.xiaomitool.v2.gui.drawable.DrawableManager;
import com.xiaomitool.v2.gui.visual.ChooserPane;
import com.xiaomitool.v2.inet.CustomHttpException;
import com.xiaomitool.v2.inet.EasyHttp;
import com.xiaomitool.v2.inet.EasyResponse;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.rom.RomException;
import com.xiaomitool.v2.rom.ZipRom;
import com.xiaomitool.v2.utility.utils.NumberUtils;
import com.xiaomitool.v2.xiaomi.XiaomiUtilities;
import com.xiaomitool.v2.xiaomi.miuithings.Branch;
import com.xiaomitool.v2.xiaomi.miuithings.Codebase;
import com.xiaomitool.v2.xiaomi.miuithings.MiuiVersion;
import com.xiaomitool.v2.xiaomi.miuithings.RequestParams;
import javafx.scene.image.Image;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XiaomiEuRoms {
    public static final String[] SOURCEFORGE_MIRRORS = new String[]{"master"};//"astuteinternet","ayera","cfhcable","cytranet","datapacket","excellmedia","freefr","iweb","jaist","kent","liquidtelecom","netcologne","netix","newcontinuum","phoenixnap","razaoinfo","superb-dca2","superb-sea2","svwh","tenet","ufpr","vorboss"};
    private static final Pattern BIG_VERSION_PATTERN = Pattern.compile("^([vV]\\d{1,2})");
    private static final String EU_HOST = "https://basketbuild.com/uploads/devs/ZduneX25";
    public static ZipRom latest(RequestParams params) throws CustomHttpException, RomException {
        String device = params.getDevice();
        final Branch branch = params.getBranch().getDual();
        Log.info("Searching latest ota xiaomi.eu rom for device: "+device+", branch: "+branch);
        String latest = Branch.STABLE.equals(branch) ? "getversion-stables" : "getversion";
        String url = EU_HOST+"/"+latest+"/"+device+".txt";
        EasyResponse response = EasyHttp.get(url);
        if (!response.isAllRight()){
            throw new RomException("Failed to retrive xiaomi.eu rom data");
        }
        String body = response.getBody();
        Log.info("Xiaomi.eu ota response: "+body);
        String[] parts = body.split("_");
        if (parts.length < 5){
            throw new RomException("Failed to parse request output on xiaomi.eu request");
        }
        String version = parts[0];
        String md5 = parts[1];
        String codebase = parts[4];
        String mirror = SOURCEFORGE_MIRRORS[NumberUtils.getRandom(0,SOURCEFORGE_MIRRORS.length-1)];
        String format, shortVersion;
        if (version.length() < 4){
            throw new RomException("Invalid miui rom: "+version);
        }
        if(Branch.STABLE.equals(branch)){
            Matcher bvM = BIG_VERSION_PATTERN.matcher(version);
            if (!bvM.find()){
                throw new RomException("Cannot find big version of rom");
            }
            String bigVersion = bvM.group(1);
            if (bigVersion.length() > 2){
                shortVersion = bigVersion;
            } else {
                shortVersion = version.substring(0,4);
            }
        } else {
           shortVersion = version;
        }
        shortVersion = shortVersion.toLowerCase();
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
            public String getTitle() {
                return branch == null ? LRes.XIAOMIEU_TITLE.toString() : (LRes.XIAOMIEU_TITLE.toString()+" - "+ LRes.branchToString(branch));
            }

            @Override
            public String getText() {
                return this.getMiuiVersion() == null ? LRes.XIAOMIEU_AUTO_DOWNLOAD.toString() : (LRes.XIAOMIEU_AUTO_DOWNLOAD.toString()+" - "+this.getMiuiVersion().toString());
            }

            @Override
            public Image getIcon() {
                return DrawableManager.getResourceImage("xiaomieu.png");
            }
        };
    }
}
