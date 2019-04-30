package com.xiaomitool.v2.logging.feedback;

import com.xiaomitool.v2.crypto.Hash;
import com.xiaomitool.v2.engine.ToolManager;
import com.xiaomitool.v2.gui.visual.CustomButton;
import com.xiaomitool.v2.inet.CustomHttpException;
import com.xiaomitool.v2.inet.CustomHttpRequest;
import com.xiaomitool.v2.inet.EasyHttp;
import com.xiaomitool.v2.inet.EasyResponse;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.logging.Debugger;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.resources.ResourcesConst;
import com.xiaomitool.v2.utility.FeedbackOutputStream;
import com.xiaomitool.v2.utility.utils.SettingsUtils;
import com.xiaomitool.v2.utility.utils.StrUtils;
import com.xiaomitool.v2.utility.utils.ThreadUtils;
import javafx.application.Platform;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class LogSender {
    private static final String SALT_RESPONSE = "response_good";
    private static Instant lastSendSuccessInstant = null;
    private static int WAIT_BEFORE_SENDING = 300;

    private static final String HOST_PATH = ToolManager.getFeedbackUrl();

    private static String getUploadToken() throws CustomHttpException {
        return getUploadToken(ToolManager.getRunningInstanceId());
    }

    private static String getUploadToken(String instanceId) throws CustomHttpException {
        if (StrUtils.isNullOrEmpty(instanceId)){
            return null;
        }
        String url = HOST_PATH+"/utok.php?iid="+instanceId;
        EasyResponse response = EasyHttp.get(url);
        Log.log("LOG","Feedback token request result: "+response.getCode()+" - "+response.getBody(), false);
        if (!response.isAllRight()){
            return null;
        }
        String token = response.getBody();
        if (token.length() < 96){
            return null;
        }
        if (!Hash.md5Hex(instanceId+SALT_RESPONSE).equalsIgnoreCase(token.substring(0,32))){
            return null;
        }
        return token.substring(32);
    }

    private static boolean sendFeedback(String token, String userFeedback, Debugger feedback) throws IOException, CustomHttpException {
        if (token == null || token.length() !=  64){
            Log.log("LOG","Invalid token to send log: "+token, false);
            return false;
        }
        if (getTimeFromLastSend() != -1){
            Log.log("LOG","Not enough time between log sending: "+getTimeFromLastSend(), false);
            return false;
        }
        String instanceId = ToolManager.getRunningInstanceId();

        if (userFeedback != null && !userFeedback.trim().isEmpty()){
            userFeedback+="\n";
        } else {
            userFeedback = null;
        }

        Log.log("LOG","Sending feedback with token: "+token, false);

        InputStream inputStream = null;
        if (feedback == null){
            if (userFeedback != null && !userFeedback.isEmpty()){
                FeedbackOutputStream oStream = new FeedbackOutputStream();
                oStream.setUserMessage(userFeedback);
                inputStream = oStream.getReadInputStream();
            }
        } else {
            inputStream = feedback.getFeedbackData(userFeedback);
        }


        String url = HOST_PATH+"/ufed.php?iid="+instanceId+"&t="+token;
        //Log.debug("LOG CONTENT: "+new String(gzip));
        //Log.log("LOG","Uploading log file of "+gzip.length+" bytes", false);

        CustomHttpRequest httpRequest = new CustomHttpRequest(url);
        httpRequest.setPostData(inputStream);
        if (Log.ADVANCED_LOG) {
            httpRequest.setProxy("127.0.0.1", 8888);
        }
        httpRequest.execute();
        Log.log("LOG","Feedback send result: "+httpRequest.getResponseCode()+" - "+httpRequest.getResponseBody(), false);
        boolean res = httpRequest.getResponseCode() == 200;
        if (res){
            lastSendSuccessInstant = Instant.now();
        }

        return res;
    }
    static boolean sendSingleLog(byte[] data) throws Exception {
        String token = getUploadToken(SettingsUtils.requireHashedPCId());
        return sendSingleLog(token, data);
    }
    private static boolean sendSingleLog(String token, byte[] data) throws CustomHttpException {
        if (token == null || token.length() !=  64){
            Log.log("LOG","Invalid token to send log: "+token, false);
            return false;
        }
        if (data == null || data.length < 4){
            Log.log("LOG","Invalid length data", false);
            return false;
        }
        String instanceId = SettingsUtils.requireHashedPCId();
        String url = HOST_PATH+"/uliv.php?iid="+instanceId+"&t="+token;
        //Log.debug("LOG CONTENT: "+new String(gzip));
        //Log.log("LOG","Uploading log file of "+gzip.length+" bytes", false);

        CustomHttpRequest httpRequest = new CustomHttpRequest(url);
        httpRequest.setPostData(data);
        if (Log.ADVANCED_LOG) {
            httpRequest.setProxy("127.0.0.1", 8888);
        }
        httpRequest.execute();
        Log.log("LOG","Feedback send result: "+httpRequest.getResponseCode()+" - "+httpRequest.getResponseBody(), false);
        return httpRequest.getResponseCode() == 200;
    }

    private static String encodeB64Url(byte[] data) throws UnsupportedEncodingException {
        return URLEncoder.encode(Base64.encodeBase64String(data), ResourcesConst.interalCharset().name());
    }

    private static void test(byte[] data, byte[] ending){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InflaterInputStream inflaterInputStream = new InflaterInputStream(new ByteArrayInputStream(byteMerge(data, ending)), new Inflater(true));
                    IOUtils.copy(inflaterInputStream, NullOutputStream.NULL_OUTPUT_STREAM);
                    Log.debug("TEST SUCCESS! ");
                }catch (IOException e){
                    Log.debug("TEST FAILED: ");
                    Log.printStackTrace(e);
                }

            }
        }).start();
    }

    private static byte[] byteMerge(byte[]... toMerge){
        int size = 0;
        for (byte[] d : toMerge){
            size+=d.length;
        }
        byte[] result = new byte[size];
        size = 0;
        for (byte[] d : toMerge){
            System.arraycopy(d,0,result,size,d.length);
            size+=d.length;
        }
        return result;
    }


    public static boolean uploadFeedback(String userFeedback, Debugger feedback) throws Exception {
        String token = getUploadToken();
        return sendFeedback(token, userFeedback, feedback);
    }
    public static boolean uploadFeedback(String userFeedback, boolean sendLogFile) throws Exception {
        return uploadFeedback(userFeedback, sendLogFile ? Log.getDebugger() : null);
    }

    private static boolean logSendCooldown;
    public static boolean isLogCooldown(){
        return logSendCooldown;
    }

    public static Thread cooldownCounter(CustomButton button) {
        logSendCooldown = false;
        if (getTimeFromLastSend() == -1){
            Platform.runLater(() -> {
                button.setText(LRes.SEND_FEEDBACK.toString());
                button.setDisable(false);
            });
        } else {
            logSendCooldown = true;
            Thread res = new Thread(new Runnable() {
                @Override
                public void run() {
                    int seconds = 0;
                    while (seconds != -1){
                        seconds = getTimeFromLastSend();
                        if (seconds <= 0){
                            break;
                        }
                        final  String text = LRes.PLEASE_WAIT_X_SECONDS.toString(seconds);
                        Platform.runLater(() -> {
                            button.setText(text);
                            button.setDisable(true);
                        });
                        ThreadUtils.sleepSilently(1000);
                    }
                    logSendCooldown = false;
                    Platform.runLater(() -> {
                        button.setText(LRes.SEND_FEEDBACK.toString());
                        button.setDisable(false);
                    });
                }
            });
            res.start();
            return res;
        }
        return null;

    }
    private static int getTimeFromLastSend(){
        try {
            if (lastSendSuccessInstant == null) {
                return -1;
            }
            Duration duration = Duration.between(lastSendSuccessInstant, Instant.now());
            long seconds = duration.getSeconds();
            return seconds < WAIT_BEFORE_SENDING ? (WAIT_BEFORE_SENDING-Math.toIntExact(seconds)) : -1;
        } catch (Exception e){
            return -1;
        }
    }
}
