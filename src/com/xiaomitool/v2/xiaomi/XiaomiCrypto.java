package com.xiaomitool.v2.xiaomi;

import com.xiaomitool.v2.crypto.AES;
import com.xiaomitool.v2.crypto.Hash;
import com.xiaomitool.v2.inet.HttpQuery;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.resources.ResourcesConst;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.HmacUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;


public class XiaomiCrypto {
    public static final byte[] UNLOCK_HMAC_KEY = buildHmacKey("327442656f45794a54756e6d57554771376251483241626e306b324e686875724f61714266797843754c56676e3441566a3773776361776535337544556e6f");
    private static final String DEFAULT_IV = "0102030405060708";


    private static  byte[] buildHmacKey(String hex){
            try {
                return Hex.decodeHex(hex.toCharArray());
            } catch (DecoderException e) {
                Log.debug("Failed to build hmacKey: ");
            }
            return null;

    }


    public static String cloudService_encrypt(String data, String key) throws Exception {
        byte[] bkey = Base64.decodeBase64(key);
        return Base64.encodeBase64String(AES.aes128cbc_encrypt(bkey, DEFAULT_IV.getBytes(StandardCharsets.ISO_8859_1), data.getBytes(ResourcesConst.interalCharset())));
    }

    public static String cloudService_decrypt(String data, String key) throws Exception {
        byte[] bkey = Base64.decodeBase64(key);
        byte[] bdata =Base64.decodeBase64(data);
        return new String(AES.aes128cbc_decrypt(bkey, DEFAULT_IV.getBytes(StandardCharsets.ISO_8859_1), bdata), ResourcesConst.interalCharset());
    }





    public static void cloudService_encryptRequestParams(HttpQuery params, String key) throws Exception {
        for (Map.Entry<String, Object> e : params.entrySet()){
            e.setValue(cloudService_encrypt(e.getValue().toString(), key));
        }
    }
    public static String cloudService_signHmac(byte[] hmacKey, String method, String path, String query){
        String hmacData = method+"\n"+path+"\n"+query;
        Log.debug("hmacData: "+hmacData);
        String hmac = HmacUtils.hmacSha1Hex(hmacKey, hmacData.getBytes(ResourcesConst.interalCharset()));
        Log.debug("Hmac: "+hmac);
        return hmac;
    }
    public static String cloudService_signSha1(String key, String method, String path, String query){
        String shaData = method+"&"+path+"&"+query+"&"+key;
        Log.debug("shaData: "+shaData);
        String sha = Hash.sha1Base64(shaData);
        Log.debug("Sha1: "+sha);
        return sha;
    }
}
