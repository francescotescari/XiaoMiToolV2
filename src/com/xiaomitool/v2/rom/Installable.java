package com.xiaomitool.v2.rom;


import com.xiaomitool.v2.gui.visual.ChooserPane;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.RInstall;
import com.xiaomitool.v2.rom.interfaces.InstallObject;
import com.xiaomitool.v2.tasks.*;
import com.xiaomitool.v2.utility.Choiceable;
import com.xiaomitool.v2.utility.CommandClass;
import com.xiaomitool.v2.utility.WaitSemaphore;
import com.xiaomitool.v2.utility.utils.SettingsUtils;
import com.xiaomitool.v2.utility.utils.StrUtils;
import com.xiaomitool.v2.xiaomi.miuithings.Codebase;
import com.xiaomitool.v2.xiaomi.miuithings.MiuiVersion;

import java.io.File;

public abstract class Installable extends CommandClass implements Choiceable, InstallObject {


    public enum Type {
        FASTBOOT,
        RECOVERY,
        IMAGE,
        PROCEDURE,
        OTHER;
    }


    protected String md5, downloadUrl, installToken, extraParam, unique;
    protected Codebase codebase;
    protected MiuiVersion miuiVersion;
    protected File downloadedFile, finalFile;
    protected Type type;
    protected boolean isOfficial, needDownload =false, needExtraction = false;
    private boolean isFake = false;
    private WaitSemaphore resourceReady = new WaitSemaphore();
    private Thread fetchResourceThread = null;
    private Exception lastException = null;

    public Installable(Type type, boolean isOfficial, String unique, boolean needDownload, boolean needExtraction){
        this.type = type;
        this.isOfficial = isOfficial;
        this.unique = unique;
        this.needDownload = needDownload;
        this.needExtraction = needExtraction;
    }
    public Installable(Type type, boolean isOfficial, String unique, boolean needDownload, boolean needExtraction, String downloadUrl){
        this(type,isOfficial,unique,needDownload,needExtraction);
        this.downloadUrl = downloadUrl;
    }
    public Installable(Type type, boolean isOfficial, String unique, boolean needDownload, boolean needExtraction, File sourceFile){
        this(type,isOfficial,unique,needDownload,needExtraction);
        this.downloadedFile = sourceFile;
    }
    protected void setFake(boolean fake){
        this.isFake = fake;
    }

    public boolean isFake() {
        return isFake;
    }

    public Type getType() {
        return type;
    }

    public Codebase getCodebase() {
        return codebase;
    }

    public boolean isOfficial() {
        return isOfficial;
    }

    public File getDownloadedFile() {
        return downloadedFile;
    }

    public MiuiVersion getMiuiVersion() {
        return miuiVersion;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getInstallToken() {
        return installToken;
    }

    public boolean hasInstallToken(){
        return installToken != null && !installToken.isEmpty();
    }

    public String getMd5() {
        return md5;
    }
    protected void setFinalFile(File file){
        this.finalFile = file;
    }
    public String getExtraParam() {
        return extraParam;
    }
    protected String getUniqueId(){
        return unique;
    }
    public final String getUnique(){
        return this.getClass().getName()+getUniqueId();
    }
    @Override
    public int hashCode(){
        return getUnique().hashCode();
    }

    public boolean isNeedDownload() {
        return needDownload;
    }

    public boolean isNeedExtraction() {
        if (!needExtraction && finalFile == null && downloadedFile != null){
            finalFile = downloadedFile;
        }
        return needExtraction;
    }

    private String downloadFilename = null;
    public void setDownloadFilename(String filename){
        this.downloadFilename = filename;
    }
    protected String getDownloadFilename(){
        return this.downloadFilename;
    }


    protected Object downloadInternal(UpdateListener listener) throws Exception {
        if (getDownloadUrl() == null){
            throw new RomException("Missing download url");
        }
        Task task = new DownloadTask(listener, getDownloadUrl(), downloadFilename == null ?  null : SettingsUtils.getDownloadFile(downloadFilename));
        TaskManager.getInstance().startSameThread(task);

        if (!task.isFinished()){
            throw new RomException("Failed to download file: "+((task.getError() != null) ? task.getError().getMessage() : "null error"));
        }
        return downloadedFile = (File) task.getResult();
    }

    public Object download(UpdateListener listener) throws Exception {
        if (!isNeedDownload()){
            return null;
        }
        if (downloadedFile != null){
            return downloadedFile;
        }
        return downloadInternal(listener);
    }

    public Object extract(UpdateListener listener) throws Exception {
        if (!isNeedExtraction()){
            return null;
        }
        if (finalFile != null){
            return finalFile;
        }
        return extractInternal(listener);
    }


    public String toLogString() {
        return this.getClass().getSimpleName()+" - "+StrUtils.str(this.getType())+"[url:"+StrUtils.str(this.downloadUrl)+",dl:"+StrUtils.str(downloadedFile)+",final:"+StrUtils.str(finalFile)+"]";
    }

    public void fetchResources(UpdateListener downloadListener, UpdateListener extractListener) {
        if (fetchResourceThread != null && fetchResourceThread.isAlive()){
            return;
        }
        Log.debug("Fetching installable resources");
        resourceReady.setPermits(0);
        lastException = null;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                resourceReady.setPermits(0);
                lastException = null;
                Log.debug("Fetching installable resource file");
                try {
                    while (true) {
                        Log.debug("Checking resource status");
                        if (getFinalFile() != null) {
                            Log.debug("Resource file fetched succesfully");
                            break;
                        } else if (getDownloadedFile() != null) {
                            Log.debug("Starting resource file extraction");
                            finalFile = (File) extractInternal(extractListener);
                            Log.debug("Resource file extracted succesfully");
                            downloadedFile = null;
                        } else if (getDownloadUrl() != null) {
                            Log.debug("Starting resource file download");
                            downloadedFile = (File) downloadInternal(downloadListener);
                            Log.debug("Resource file downloaded succesfully");
                            downloadUrl = null;
                        } else {

                            throw new RomException("Cannot fetch installable resources: nothing to do");
                        }
                    }
                } catch (Exception e){
                    lastException = e;
                    Command cmd;
                    try {
                        cmd = waitCommand();
                    } catch (InterruptedException e1) {
                        lastException = e1;
                        return;
                    }
                    if (Command.RETRY.equals(cmd)){
                        run();
                        return;
                    }
                }
                resourceReady.increase();
            }
        };

        fetchResourceThread = new Thread(runnable);
        fetchResourceThread.start();
    }

    public void waitReourcesReady() throws Exception {
        resourceReady.waitOnce();
        if (lastException != null){
            throw lastException;
        }
    }

    protected  Object extractInternal(UpdateListener listener) throws Exception  {
        setFinalFile(downloadedFile);
        return downloadedFile;
    }

    protected void setVars(Type type, boolean isOfficial, String unique, boolean needDownload, boolean needExtraction){
        this.type = type;
        this.isOfficial = isOfficial;
        this.unique = unique;
        this.needDownload = needDownload;
        this.needExtraction = needExtraction;
    }

    public File getFinalFile(){
        return finalFile;
    }

    public abstract RInstall getInstallProcedure();

    @Override
    public boolean isProcedure(){
        return false;
    }

    @Override
    public final ChooserPane.Choice getChoice(){
        return new ChooserPane.Choice(getTitle(), getText(), getIcon());
    }

    @Override
    public final Type getInstallType(){
        return this.type;
    }




}
