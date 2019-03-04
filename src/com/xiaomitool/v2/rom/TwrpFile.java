package com.xiaomitool.v2.rom;


import com.xiaomitool.v2.gui.drawable.DrawableManager;
import com.xiaomitool.v2.gui.visual.ChooserPane;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.RInstall;
import com.xiaomitool.v2.procedure.install.TwrpInstall;
import com.xiaomitool.v2.tasks.*;
import com.xiaomitool.v2.utility.NotNull;
import javafx.scene.image.Image;
import org.apache.commons.io.FilenameUtils;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class TwrpFile extends Installable {
    private String codename;
    public TwrpFile(String url, String codename) {
        super(Type.IMAGE, false, url, true, true, url);
        this.codename = codename;
    }
    public TwrpFile( File localFile, String codename) {
        super(Type.IMAGE, false, localFile.getAbsolutePath(), false, true, localFile);
        this.codename =codename;
    }

    @Override
    protected File downloadInternal(UpdateListener listener) throws Exception {
        DownloadTask task = new DownloadTask(listener, downloadUrl, (File) null);
        if (downloadUrl.contains("/dl.twrp.me/")){
            HashMap<String, String> header = new HashMap<>();
            header.put("Referer", downloadUrl+".html");
            task.setHeaders(header);
        }
        TaskManager manager = TaskManager.getInstance();
        manager.startSameThread(task);
        if (!task.isFinished()){
            throw new RomException("Failed to download file");
        }
        return  downloadedFile = (File) task.getResult();
    }


    @Override
    @NotNull
    protected File extractInternal(UpdateListener listener) throws Exception {
        Log.debug("EXtracting twrp file");
        File file = getDownloadedFile();
        if (file == null){
            throw new RomException("Missing downloaded file");
        }
        if (!file.getName().toLowerCase().endsWith(".zip")){
            setFinalFile(file);
            return file;
        }
        ExtractionTask task = new ExtractionTask(listener, file, null, ExtractionTask.ExtractionType.ZIP, false);
        TaskManager manager = TaskManager.getInstance();
        manager.startSameThread(task);
        if (!task.isFinished()){
            throw new RomException("Extraction task failed: "+(task.getError() == null ? "null error" : task.getError().getMessage()));
        }
        File extractDir = (File) task.getResult();
        File[] children = extractDir.listFiles(File::isFile);
        if (children == null){
            Exception e = new IOException("Cannot find twrp extraction directory children");
            listener.onError(e);
            throw e;
        }
        setFinalFile(null);
        for (File child : children){
            String name = child.getName().toLowerCase();
            if (name.endsWith(".img") && (name.contains("twrp") || name.contains(codename))){
                setFinalFile(child);
                break;
            }
        }
        if (finalFile == null){
            Exception e = new IOException("Could not find a valid twrp image file in extracted directory");
            listener.onError(e);
            throw e;
        }
        return finalFile;
    }

    @Override
    public RInstall getInstallProcedure() {
        return TwrpInstall.flashTwrp();
    }

    @Override
    public ChooserPane.Choice getChoice() {
        String title = LRes.TWRP_RECOVERY.toString();
        String text = LRes.TWRP_SELECT_MANUAL.toString();
        if (this.getDownloadUrl() != null){
            title +=" - "+FilenameUtils.getName(this.getDownloadUrl());
            text = LRes.TWRP_AUTO_DOWNLOAD.toString();
        }
        return new ChooserPane.Choice(title, text, new Image(DrawableManager.getPng("twrplogo.png").toString()));
    }
}
