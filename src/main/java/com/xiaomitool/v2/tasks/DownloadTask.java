package com.xiaomitool.v2.tasks;

import com.xiaomitool.v2.inet.CustomHttpException;
import com.xiaomitool.v2.inet.CustomHttpRequest;
import com.xiaomitool.v2.inet.EasyHttp;
import com.xiaomitool.v2.utility.utils.SettingsUtils;
import org.apache.http.HttpEntity;

import java.io.*;
import java.util.Map;

public class DownloadTask extends Task {
    private static final int DOWNLOAD_CHUNCK = 1024 * 16;
    private static final int WRITE_CHUNCK = 1024 * 256;
    private String url;
    private File destination;
    private Map<String, String> headers;
    private long totalSize = 0, downloaded;

    public DownloadTask(UpdateListener listener, String url, String fileOutput) {
        this(listener, url, fileOutput == null ? null : new File(fileOutput));
    }

    public DownloadTask(UpdateListener listener, String url, File fileOutput) {
        super(listener);
        this.url = url;
        this.destination = fileOutput;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    protected void startInternal() {
        EasyHttp easyRequest = new EasyHttp().url(url);
        if (headers != null) {
            easyRequest.headers(headers);
        }
        CustomHttpRequest request = easyRequest.getHttpRequestObj();
        try {
            request.execute();
        } catch (CustomHttpException e) {
            error(e);
            return;
        }
        HttpEntity entity;
        try {
            entity = request.getResponseEntity();
        } catch (Exception e) {
            error(e);
            return;
        }
        int responseCode = 200;
        try {
            responseCode = request.getResponseCode();
        } catch (CustomHttpException e) {
            error(e);
        }
        if (responseCode == 302) {
            String location;
            try {
                location = request.getResponseHeaders().get("location").get(0);
            } catch (Exception e) {
                error(e);
                return;
            }
            url = location;
            startInternal();
            return;
        } else if (responseCode != 200) {
            error(new CustomHttpException("Response code is not valid: " + responseCode));
            return;
        }
        if (this.destination == null) {
            this.destination = SettingsUtils.getDownloadFile(url);
        }
        totalSize = entity.getContentLength();
        setTotalSize(totalSize);
        if (this.destination.exists() && this.destination.length() == totalSize && totalSize != 0) {
            request.abort();
            finished(this.destination);
            return;
        }
        InputStream downloadStream;
        try {
            downloadStream = entity.getContent();
        } catch (IOException e) {
            error(e);
            return;
        }
        BufferedOutputStream outputStream;
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(destination));
        } catch (FileNotFoundException e) {
            error(e);
            return;
        }
        byte[] buffer = new byte[DOWNLOAD_CHUNCK];
        while (!STATUS.ABORTED.equals(status)) {
            if (STATUS.PAUSED.equals(status)) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            try {
                int read = downloadStream.read(buffer);
                if (read < 0) {
                    break;
                }
                outputStream.write(buffer, 0, read);
                downloaded += read;
                update(downloaded);
            } catch (IOException e) {
                error(e);
                return;
            }
        }
        try {
            downloadStream.close();
            outputStream.close();
        } catch (IOException e) {
            error(e);
            return;
        }
        if (!STATUS.ABORTED.equals(status)) {
            finished(destination);
        }
    }

    @Override
    protected boolean canPause() {
        return true;
    }

    @Override
    protected boolean canStop() {
        return true;
    }

    @Override
    protected boolean pauseInternal() {
        return true;
    }

    @Override
    protected boolean stopInternal() {
        return true;
    }
}
