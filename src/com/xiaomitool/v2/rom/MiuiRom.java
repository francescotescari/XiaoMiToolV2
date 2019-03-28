package com.xiaomitool.v2.rom;

import com.xiaomitool.v2.adb.device.DeviceGroups;
import com.xiaomitool.v2.gui.drawable.DrawableManager;
import com.xiaomitool.v2.gui.visual.ChooserPane;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.resources.ResourceImages;
import com.xiaomitool.v2.resources.ResourcesManager;
import com.xiaomitool.v2.utility.utils.SettingsUtils.Region;
import com.xiaomitool.v2.xiaomi.XiaomiUtilities;
import com.xiaomitool.v2.xiaomi.miuithings.Branch;
import com.xiaomitool.v2.xiaomi.miuithings.Mirrors;
import javafx.scene.image.Image;


import java.util.*;
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
        CHINA_STABLE(0, Region.CN, LRes.CHINA_STABLE),
        CHINA_DEVELOPER(1, Region.CN, LRes.CHINA_DEVELOPER),
        GLOBAL_STABLE(2, Region.GLOBAL, LRes.GLOBAL_STABLE),
        GLOBAL_DEVELOPER(3, Region.GLOBAL, LRes.GLOBAL_DEVELOPER),
        EUROPEAN_STABLE(4, Region.EU, LRes.EUROPE_STABLE),
        EUROPEAN_DEVELOPER(5, Region.EU, LRes.EUROPE_DEVELOPER),
        INDIA_STABLE(6, Region.INDIA, LRes.INDIA_STABLE),
        INDIA_DEVELOPER(7, Region.INDIA, LRes.INDIA_DEVELOPER),
        RUSSIA_STABLE(8, Region.RUSSIA, LRes.RUSSIA_STABLE),
        RUSSIA_DEVELOPER(9, Region.RUSSIA, LRes.RUSSIA_DEVELOPER);
        private final int code;
        private Branch branch;
        private Region parentRegion;
        private LRes lRes;
        Specie(int code, Region parentRegion, LRes lRes){
            this.code = code; this.parentRegion = parentRegion;
            this.lRes = lRes;
        }

        public String getDrawable(){
            return parentRegion.getDrawable();
        }

        public Region getParentRegion(){
            return parentRegion;
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
        public String buildModDevice(String codename){
            String cleanCodename = XiaomiUtilities.stripCodename(codename);
            switch (this.parentRegion){
                case CN:
                    return cleanCodename;
                case GLOBAL:
                    return cleanCodename+"_global";
                case INDIA:
                    return cleanCodename+"_india_global";
                case RUSSIA:
                    return cleanCodename+"_ru_global";
                case EU:
                    return cleanCodename+"_eea_global";
            }
            return codename;
        }


        public Specie toChina(){
            return toRegion(Region.CN);
        }
        public Specie toGlobal(){
            return toRegion(Region.GLOBAL);
        }
        public Specie toRegion(Region region){
            for (Specie sp : Specie.values()){
                if (sp.parentRegion.equals(region)){
                    int rCode = sp.code;
                    int bCode = this.code%2;
                    return fromCode(rCode+bCode);
                }
            }
            return null;
        }

        public static LinkedHashSet<Specie> listToSearchSpecies(Region region, String device){
            LinkedHashSet<Specie> species = new LinkedHashSet<>();
            species.add(CHINA_STABLE);
            species.add(CHINA_DEVELOPER);
            species.add(GLOBAL_STABLE);
            species.add(GLOBAL_DEVELOPER);
            if (!DeviceGroups.isMultiRegionDevice(device)){
                return species;
            }
            for (Specie sp : Specie.values()){
                if (sp.parentRegion.equals(region)){
                    species.add(sp);
                }
            }
            if (DeviceGroups.hasEEARegion(device)){
                species.add(EUROPEAN_STABLE);
                species.add(EUROPEAN_DEVELOPER);
            }
            return species;
        }

        public String getRequestRegion(){
            if (this.isChinese()){
                return "cn";
            } else {
                return "global";
            }
        }

        public boolean isChinese(){
            return Region.CN.equals(this.parentRegion);
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
        public Specie toBranch(Branch branch){
            if (branch == null){
                return this;
            }
            return branch.equals(Branch.STABLE) ? toStable() : toDeveloper();
        }
        public static Specie fromCode(int code){
            for (Specie sp : Specie.values()){
                if (sp.code == code){
                    return sp;
                }
            }
            return null;
        }
        public static Specie fromStringBranch(String codename, Branch branch){
            if (branch == null || codename == null){
                return null;
            }
            int rCode = baseSpecieFromCodename(codename).code;
            int bCode = Branch.STABLE.equals(branch.getDual()) ? 0 : 1;
            Specie p = fromCode(bCode+rCode);
            Log.debug("Specie from device and branch: "+p.toString());
            p.setBranch(branch);
            return p;
        }

        private static Specie baseSpecieFromCodename(String codename){
            if (codename == null){
                return null;
            } else if (codename.contains("_eea_global")){
                return EUROPEAN_STABLE;
            } else if (codename.contains("_india_global")){
                return INDIA_STABLE;
            } else if (codename.contains("_ru_global")){
                return RUSSIA_STABLE;
            } else  if (codename.contains("_global")){
                return GLOBAL_STABLE;
            } else {
                return CHINA_STABLE;
            }
        }

        public void setBranch(Branch branch) {
            this.branch = branch;
        }

        public String toHuman(){
            return lRes.toString();
        }



        public int getZone() {
            return isChinese() ?  1 : 2;
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
        Image image = null;
        if (this.isFake()){
            return new ChooserPane.Choice(this.isOfficial() ? LRes.ROM_LOCAL_OFFICIAL.toString() : LRes.ROM_LOCAL.toString(), this.isOfficial() ? LRes.ROM_LOCAL_OFFICIAL_SUB.toString() : LRes.ROM_LOCAL_SUB.toString());

        } else if (this.specie == null){
            title = "Error: unknown specie";
            sub = this.getMiuiVersion().toString();
        } else {
            image = DrawableManager.getResourceImage(this.specie.getDrawable());
            title = this.specie.toHuman();
            List<String> subList = new ArrayList<>();
            if (miuiVersion != null){
                subList.add(LRes.MIUI_VERSION + ": " + miuiVersion.toString());
            }
            if (codebase != null){
                subList.add(LRes.ANDROID_VERSION + ": " + codebase.toString());
            }
            sub = String.join(" - ",subList);
        }

        return new ChooserPane.Choice(title, sub, image);
    }
}
