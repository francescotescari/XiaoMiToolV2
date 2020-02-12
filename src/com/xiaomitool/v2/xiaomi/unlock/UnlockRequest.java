package com.xiaomitool.v2.xiaomi.unlock;

import com.xiaomitool.v2.inet.CustomHttpException;
import com.xiaomitool.v2.inet.EasyHttp;
import com.xiaomitool.v2.inet.EasyResponse;
import com.xiaomitool.v2.inet.HttpQuery;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.utility.utils.SettingsUtils;
import com.xiaomitool.v2.xiaomi.XiaomiCrypto;
import com.xiaomitool.v2.xiaomi.XiaomiKeystore;
import com.xiaomitool.v2.xiaomi.XiaomiProcedureException;
import org.json.JSONObject;

import java.util.Base64;
import java.util.HashMap;

public class UnlockRequest {
    private static final String SERVICE_NAME = "unlockApi";
    private static final String HOST_INTL = "https://unlock.update.intl.miui.com";
    private static final String HOST = "https://unlock.update.miui.com";
    private final HashMap<String, String> headers = new HashMap<>();
    private String path;
    private HttpQuery params = new HttpQuery();
    private String signHmac, signSha;

    public UnlockRequest(String path) {
        this.path = path;
    }

    public String exec() throws XiaomiProcedureException, CustomHttpException {
        String[] keyToken = XiaomiKeystore.getInstance().requireServiceKeyAndToken(SERVICE_NAME);
        signHmac = XiaomiCrypto.cloudService_signHmac(XiaomiCrypto.UNLOCK_HMAC_KEY, "POST", path, params.toString());
        params.put("sign", signHmac);
        String key = keyToken[0];
        String serviceToken = keyToken[1];
        Log.info("Unlock request (" + this.path + ") params: " + params);
        try {
            XiaomiCrypto.cloudService_encryptRequestParams(params, key);
        } catch (Exception e) {
            throw new XiaomiProcedureException("[UnlockRequest.exec] Cannot encrypt post params: " + e.getMessage());
        }
        signSha = XiaomiCrypto.cloudService_signSha1(key, "POST", path, params.toString());
        params.put("signature", signSha);
        String host = SettingsUtils.isGlobalRegion() ? HOST_INTL : HOST;
        EasyResponse response = new EasyHttp().url(host + path).fields(params).headers(headers).userAgent("XiaomiPCSuite").cookies(XiaomiKeystore.getInstance().requireServiceCookies(SERVICE_NAME)).exec();
        if (!response.isAllRight()) {
            throw new XiaomiProcedureException("[UnlockRequest.exec] Invalid server respose: code: " + response.getCode() + ", lenght: " + response.getBody().length());
        }
        String body = response.getBody();
        try {
            body = XiaomiCrypto.cloudService_decrypt(body, key);
        } catch (Exception e) {
            throw new XiaomiProcedureException("[UnlockRequest.exec] Cannot decrypt response data: " + e.getMessage());
        }
        try {
            body = new String(Base64.getDecoder().decode(body));
        } catch (Throwable t) {
        }
        Log.info("Unlock request (" + this.path + ") response: " + body);
        return body;
    }

    public void addParam(String key, Object value) {
        params.put(key, value);
    }

    public void addNonce() throws XiaomiProcedureException, CustomHttpException {
        String json = UnlockCommonRequests.nonceV2();
        try {
            JSONObject obj = new JSONObject(json);
            int code = obj.getInt("code");
            if (code != 0) {
                throw new XiaomiProcedureException("[UnlockRequest.addNonce] Response code of nonce request is not zero: " + code);
            }
            String nonce = obj.getString("nonce");
            params.put("nonce", nonce);
        } catch (Exception e) {
            throw new XiaomiProcedureException("[UnlockRequest.addNonce] Exception while parsing nonce response: " + e.getMessage());
        }
    }

    public void setHeader(String name, String value) {
        this.headers.put(name, value);
    }
}
