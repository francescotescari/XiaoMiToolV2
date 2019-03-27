package com.xiaomitool.v2.rom;

import com.xiaomitool.v2.gui.visual.ChooserPane;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.resources.ResourceImages;
import com.xiaomitool.v2.resources.ResourcesManager;
import com.xiaomitool.v2.utility.NotNull;
import com.xiaomitool.v2.xiaomi.miuithings.Branch;
import com.xiaomitool.v2.xiaomi.miuithings.Mirrors;

import java.util.ArrayList;
import java.util.List;


public abstract class MiuiRom extends Installable {
    protected Kind kind;
    protected Mirrors mirrors = new Mirrors();
    protected Branch branch;
    protected  String filename, descriptionUrl;

    public MiuiRom(Type type, boolean isOfficial, String unique, boolean needDownload, boolean needExtraction) {
        super(type, isOfficial, unique, needDownload, needExtraction);
    }

    public Branch getBranch() {
        return this.branch;
    }

    public enum Kind {
        LATEST("LatestRom"),
        CURRENT("CurrentRom"),
        PACKAGE("PkgRom"),
        INCREMENTAL("IncrementRom");
        private String toStr;
        Kind(String str){
            toStr = str;
        }
        @Override
        public String toString(){
            return toStr;
        }
    }
    public enum Specie {
        CHINA_STABLE(0),
        CHINA_DEVELOPER(1),
        GLOBAL_STABLE(2),
        GLOBAL_DEVELOPER(3),
        EUROPEAN_STABLE(4),
        EUROPEAN_DEVELOPER(5),
        INDIA_STABLE(6),
        INDIA_DEVELOPER(7),
        RUSSIA_STABLE(8),
        RUSSIA_DEVELOPER(9);
        private final int code;
        private Branch branch;
        Specie(int code){
            this.code = code;
        }

        public int getCode() {
            return code;
        }
        public Branch getBranch(){
            if (this.branch != null){
                return branch;
            }
            return code%2 == 0 ? Branch.STABLE : Branch.DEVELOPER;
        }
        public String getSuffix(){
            return code > 1 ? "_global" : "";
        }
        public Specie toChina(){
            return Specie.fromCode(this.code%2);
        }
        public Specie toGlobal(){
            return Specie.fromCode((this.code%2)+2);
        }
        public Specie toStable(){
            return Specie.fromCode(this.code-(this.code%2));
        }
        public Specie toDeveloper(){
            return Specie.fromCode(1+this.code-(this.code%2));
        }

        public boolean isStable(){
            return this.code%2 == 0;
        }
        public Specie toOppositeBranch(){
            if (this.isStable()){
                return this.toDeveloper();
            } else {
                return this.toStable();
            }
        }
        public static Specie fromCode(int code){
            switch (code){
                case 0:
                    return CHINA_STABLE;
                case 1:
                    return CHINA_DEVELOPER;
                case 2:
                    return GLOBAL_STABLE;
                case 3:
                    return GLOBAL_DEVELOPER;
                    default:
                        return fromCode(code%4);
            }
        }
        public static Specie fromStringBranch(@NotNull String codenameOrRegion, @NotNull Branch branch){
            if (branch == null || codenameOrRegion == null){
                return null;
            }
            int code = codenameOrRegion.toLowerCase().contains("global") ? 2 : 0;
           Specie p = fromCode(code+(Branch.STABLE.equals(branch.getDual()) ? 0 : 1));
            Log.debug("Specie from device and branch: "+p.toString());
           p.setBranch(branch);
           return p;
        }

        public void setBranch(Branch branch) {
            this.branch = branch;
        }

        public  boolean isInternational(){
            return code > 1;
        }

        @Override
        public String toString(){
            switch (this.code){
                case 0:
                    return LRes.CHINA_STABLE.toString();
                case 1:
                    return LRes.CHINA_DEVELOPER.toString();
                case 2:
                    return LRes.GLOBAL_STABLE.toString();
                case 3:
                    return LRes.GLOBAL_DEVELOPER.toString();
            }
            return LRes.UNKNOWN.toString();
        }

        public int getZone() {
            return isInternational() ? 2 : 1;
        }
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    public void setMirrors(Mirrors mirrors) {
        this.mirrors = mirrors;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    protected Specie specie;

    public Specie getSpecie() {
        return specie;
    }

    @Override
    public String getDownloadUrl() {
        if (downloadUrl != null && !downloadUrl.isEmpty()){
            return downloadUrl;
        }
        if (mirrors == null || miuiVersion == null){
            return null;
        }
        downloadUrl = mirrors.resolve(miuiVersion.toString()+"/"+filename);
        return downloadUrl;
    }

    @Override
    public ChooserPane.Choice getChoice() {
        String title, sub;
        String b64image = null;
        if (this.isFake()){
            return new ChooserPane.Choice(this.isOfficial() ? LRes.ROM_LOCAL_OFFICIAL.toString() : LRes.ROM_LOCAL.toString(), this.isOfficial() ? LRes.ROM_LOCAL_OFFICIAL_SUB.toString() : LRes.ROM_LOCAL_SUB.toString());

        } else if (this.specie == null){
            title = "Miui rom";//TODO
            sub = this.getMiuiVersion().toString();
        } else {

            switch (this.specie) {
                case GLOBAL_DEVELOPER:
                    b64image = ResourceImages.IMG_GLOBALICON;
                    title = LRes.ROM_GLOBAL_DEVELOPER.toString();
                    break;
                case CHINA_DEVELOPER:
                    b64image = ResourceImages.IMG_CHINAICON;
                    title = LRes.ROM_CHINA_DEVELOPER.toString();
                    break;
                case GLOBAL_STABLE:
                    b64image = ResourceImages.IMG_GLOBALICON;
                    title = LRes.ROM_GLOBAL_STABLE.toString();
                    break;
                case CHINA_STABLE:
                    b64image = ResourceImages.IMG_CHINAICON;
                    title = LRes.ROM_CHINA_STABLE.toString();
                    break;
                    default:
                        title = this.specie.toString();
            }
            List<String> subList = new ArrayList<>();
            if (miuiVersion != null){
                subList.add(LRes.MIUI_VERSION + ": " + miuiVersion.toString());
            }
            if (codebase != null){
                subList.add(LRes.ANDROID_VERSION + ": " + codebase.toString());
            }
            sub = String.join(" - ",subList);
        }

        return new ChooserPane.Choice(title, sub, b64image == null ? null : ResourcesManager.b64toImage(b64image));
    }
}
