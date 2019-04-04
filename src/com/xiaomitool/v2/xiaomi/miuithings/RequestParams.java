package com.xiaomitool.v2.xiaomi.miuithings;

import com.xiaomitool.v2.rom.MiuiRom;

public abstract class RequestParams implements Cloneable {
    protected String device;
    protected Codebase codebase;
    protected MiuiVersion version;
    protected String androidHash;
    protected String language;
    protected String board;
    protected String imeiHash;
    protected int f;
    protected String region;
    protected String carrier;
    protected int is_cts;
    protected int a;
    protected int isR;
    protected String serialNumber;
    protected int sys;
    protected UnlockStatus unlockStatus;
    protected String packageHash;
    protected int zone;
    protected String hashId;
    protected int abOption;
    protected int express;
    protected String id;
    protected MiuiRom.Specie specie;
    protected Branch branch;

    @Override
    public RequestParams clone() throws CloneNotSupportedException {
        RequestParams clone = (RequestParams) super.clone();
        clone.device = this.device;
        clone.codebase = this.codebase;
        clone.version = this.version;
        clone.language = this.language;
        clone.androidHash = this.androidHash;
        clone.board = this.board;
        clone.imeiHash = this.imeiHash;
        clone.f = this.f;
        clone.region = this.region;
        clone.carrier = this.carrier;
        clone.is_cts = this.is_cts;
        clone.a = this.a;
        clone.isR = this.isR;
        clone.serialNumber= this.serialNumber;
        clone.sys = this.sys;
        clone.unlockStatus = this.unlockStatus;
        clone.packageHash = this.packageHash;
        clone.zone = this.zone;
        clone.hashId = this.hashId;
        clone.abOption = this.abOption;
        clone.express = this.express;
        clone.id = this.id;
        clone.specie = this.specie;
        clone.branch = this.branch;
        return clone;
    }

    public MiuiRom.Specie getSpecie() {
        return specie;
    }
    public void setVersion(MiuiVersion version){
        this.version = version;
    }

    public abstract String buildJson() throws Exception;
    public void setId(String id){
        this.id = id;
    }

    public void setSpecie(MiuiRom.Specie specie){
        this.specie = specie;
        this.branch = specie.getBranch();

    }

    public Codebase getCodebase() {
        return codebase;
    }
    public String getDevice(){
        return device;
    }
    public String getRegion(){
        return region;
    }

    public String getCarrier() {
        return carrier;
    }
    public String getLanguage(){
        return language;
    }
    public Branch getBranch(){
        return specie.getBranch();
    }
    public void setPkg(String pkgMd5){
        this.packageHash = pkgMd5;
    }
    public String getModDevice(){
        return this.specie.buildModDevice(this.device);
    }

    public boolean isInternational(){
        if (this.specie == null){
            return isInternationalBadMethod();
        }
        return !specie.isChinese();
    }

    public String getRequestRegion(){
        return specie == null ? (isInternationalBadMethod() ? "global" : "cn") : specie.getRequestRegion();
    }



    private boolean isInternationalBadMethod(){
        return MiuiRom.Specie.getZone(this.device) != 1;
    }

    public void setZone(int zone) {
        this.zone = zone;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getPkg() {
        return this.packageHash;
    }
}
