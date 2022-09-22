package com.xiaomitool.v2.crypto;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

public class Hash {
    public static String sha1Hex(String data) {
        return DigestUtils.sha1Hex(data);
    }

    public static String sha1Base64(String data) {
        return Base64.encodeBase64String(DigestUtils.sha1(data));
    }

    public static String md5Hex(String data) {
        return DigestUtils.md5Hex(data);
    }
}
