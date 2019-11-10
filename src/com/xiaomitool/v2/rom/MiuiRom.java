package com.xiaomitool.v2.rom;

import com.xiaomitool.v2.adb.device.DeviceGroups;
import com.xiaomitool.v2.gui.drawable.DrawableManager;
import com.xiaomitool.v2.gui.visual.ChooserPane;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.resources.ResourceImages;
import com.xiaomitool.v2.resources.ResourcesManager;
import com.xiaomitool.v2.utility.utils.SettingsUtils.Region;
import com.xiaomitool.v2.utility.utils.StrUtils;
import com.xiaomitool.v2.xiaomi.XiaomiUtilities;
import com.xiaomitool.v2.xiaomi.miuithings.Branch;
import com.xiaomitool.v2.xiaomi.miuithings.Codebase;
import com.xiaomitool.v2.xiaomi.miuithings.Mirrors;
import com.xiaomitool.v2.xiaomi.miuithings.MiuiVersion;
import javafx.scene.image.Image;


import java.util.*;
import java.util.List;


public abstract class MiuiRom extends Installable {
    protected Kind kind;
    protected Mirrors mirrors = new Mirrors();
    protected Branch branch;
    protected  String filename, descriptionUrl;
    private static final HashSet<Specie> NOT_EXISTING_SPECIES = new HashSet<>();
    static {
        NOT_EXISTING_SPECIES.add(Specie.EUROPEAN_DEVELOPER);
        NOT_EXISTING_SPECIES.add(Specie.RUSSIA_DEVELOPER);
        NOT_EXISTING_SPECIES.add(Specie.INDIA_DEVELOPER);
    }

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
        RUSSIA_DEVELOPER(9, Region.RUSSIA, LRes.RUSSIA_DEVELOPER),
        OTHER(100, Region.OTHER, LRes.REG_OTHER);
        private final int code;
        private Branch branch;
        private Region parentRegion;
        private final LRes lRes;
        private  String suffix;
        Specie(int code, Region parentRegion, LRes lRes){
            this.suffix = parentRegion.getSuffix();
            this.code = code; this.parentRegion = parentRegion;
            this.lRes = lRes;
        }

        public static Specie fromZoneBranch(Region reg, Branch branch) {
            for (Specie s: Specie.values()){
                if (s.getParentRegion().equals(reg)){
                    if (s.getBranch().equals(branch)){
                        return s;
                    }
                }
            }
            Specie s = Specie.OTHER;
            s.parentRegion = reg;
            s.branch = branch;
            return s;
        }

        public String getFastbootRegion(){
            return this.parentRegion.getFastbootValue();
        }



        public String getDrawable(){
            return parentRegion == null ? Region.GLOBAL.getDrawable() : parentRegion.getDrawable();
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
            return cleanCodename+suffix;
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
            Specie currentSpecie = fromStringBranch(device, Branch.STABLE);

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
            if (currentSpecie != null){
                species.add(currentSpecie);
            }
            species.removeAll(NOT_EXISTING_SPECIES);
            return species;
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
            Specie rCode = baseSpecieFromCodename(codename);
            if (OTHER.equals(rCode)){
                rCode.setBranch(branch);
                return rCode;
            }
            int bCode = Branch.STABLE.equals(branch.getDual()) ? 0 : 1;
            Specie p = fromCode(bCode+rCode.code);
            if (p == null){
                return null;
            }
            Log.debug("Specie from device and branch: "+p);
            p.setBranch(branch);
            return p;
        }

        private static Specie baseSpecieFromCodename(String codename){
            if (codename == null){
                return null;
            }
            String stripped = XiaomiUtilities.stripCodename(codename);
            String suffix = StrUtils.after(codename, stripped);
            if (suffix == null){
                return null;
            }
            for (Specie specie : Specie.values()){
                if (suffix.equalsIgnoreCase(specie.suffix) && !specie.equals(OTHER)){
                    return specie.toStable();
                }
            }
            Specie res = OTHER;
            res.suffix = suffix;
            return res;
        }

        public void setBranch(Branch branch) {
            this.branch = branch;
        }

        public String toHuman(){
            return lRes.toString();
        }

        @Override
        public String toString(){
            return super.toString()+"("+suffix+")";
        }



        public int getZone() {
            return isChinese() ?  1 : 2;
        }
        public static int getZone(String codename){
            if (codename == null){
                return 0;
            }
            Specie specie = fromStringBranch(codename, Branch.STABLE);
            return specie.getZone();
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
    public String getTitle() {
        if (this.isFake()){
            return this.isOfficial() ? LRes.ROM_LOCAL_OFFICIAL.toString() : LRes.ROM_LOCAL.toString();
        } else if (this.specie == null){
            return "Error: unknown specie";
        } else {
            return this.specie.toHuman();
        }
    }

    @Override
    public String getText() {
        if (this.isFake()){
            return this.isOfficial() ? LRes.ROM_LOCAL_OFFICIAL_SUB.toString() : LRes.ROM_LOCAL_SUB.toString();
        } else if (this.specie == null){
            return this.getMiuiVersion() == null ? "null" : getMiuiVersion().toString();
        } else {
            List<String> subList = new ArrayList<>();
            MiuiVersion version = getMiuiVersion();
            if (version != null){
                subList.add(LRes.MIUI_VERSION + ": " +version);
            }
            Codebase codebase  = getCodebase();
            if (codebase != null){
                subList.add(LRes.ANDROID_VERSION + ": " + codebase.toString());
            }
            return String.join(" - ",subList);
        }
    }

    @Override
    public Image getIcon(){
        return isFake() ? DrawableManager.getResourceImage(DrawableManager.LOCAL_PC) : DrawableManager.getResourceImage(this.specie.getDrawable());
    }


}
