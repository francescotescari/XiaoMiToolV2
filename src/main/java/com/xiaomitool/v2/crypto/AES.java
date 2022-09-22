package com.xiaomitool.v2.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {
    public static byte[] aes128cbc_encrypt(final byte[] key, final byte[] IV, final byte[] message) throws Exception {
        return aes128cbc_encryptDecrypt(Cipher.ENCRYPT_MODE, key, IV, message);
    }

    public static byte[] aes128cbc_decrypt(final byte[] key, final byte[] IV, final byte[] message) throws Exception {
        return aes128cbc_encryptDecrypt(Cipher.DECRYPT_MODE, key, IV, message);
    }

    private static byte[] aes128cbc_encryptDecrypt(final int mode, final byte[] key, final byte[] IV, final byte[] message) throws Exception {
        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        final SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        final IvParameterSpec ivSpec = new IvParameterSpec(IV);
        cipher.init(mode, keySpec, ivSpec);
        return cipher.doFinal(message);
    }
}
