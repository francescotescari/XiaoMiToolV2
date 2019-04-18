package com.xiaomitool.v2.xiaomi.miuithings;

import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.utility.KeepOriginClass;
import com.xiaomitool.v2.utility.utils.ArrayUtils;
import com.xiaomitool.v2.utility.utils.StrUtils;

import java.util.Base64;
import java.util.regex.Pattern;

public class SerialNumber {
    public static final SerialNumber SN_ZERO = new SerialNumber("00000000", true);

    private String hexRapresentation = null;
    private boolean hasZeroX;

    private SerialNumber(String rapr, boolean hasZeroX) {
        this.hexRapresentation = rapr;
        this.hasZeroX = hasZeroX;
    }

    public static SerialNumber fromHexString(String hexString){
        if (StrUtils.isNullOrEmpty(hexString)){
            return null;
        }
        boolean hasZeroX = false;
        if (hexString.startsWith("0x") || hexString.startsWith("0X")){
            hexString = hexString.substring(2);
            hasZeroX = true;
        }
        return new SerialNumber(hexString, hasZeroX);
    }

    public static SerialNumber fromFastbootToken(String token){
        if (StrUtils.isNullOrEmpty(token)){
            return null;
        }
        try {
            byte[] data = Base64.getDecoder().decode(token);
            if (data.length < 4){
                return null;
            }
            String parsed = null;
            if (data[0] == 0x55){
                int len = data[3];
                //Log.debug(len+" - "+data.length);
                if (len+4 == data.length || len+2==data.length){
                    int pos = 4;
                    while (pos < data.length&& pos<len+4){
                        byte type = data[pos];
                        int fLen = data[pos+1];
                        fLen = Integer.min(fLen, data.length-pos-2);
                        //Log.debug(type+ "-- "+fLen);
                        byte[] fData = new byte[fLen];
                        //Log.debug("SS");
                        System.arraycopy(data, pos+2, fData, 0, fLen);

                        if (type == 2){
                            parsed = StrUtils.toHexString(fData);

                        }
                        pos += fLen+2;
                    }
                }
            }
            if (parsed == null){
                String hex = StrUtils.toHexString(ArrayUtils.reverse(data));
                if (hex.length() < 8){
                    return null;
                }
                parsed = hex.substring(0,8);

            }
            return new SerialNumber(parsed, true);

        } catch (Throwable t){
            return null;
        }
    }

    public String toHexString(){
        if (StrUtils.isNullOrEmpty(this.hexRapresentation)){
            return "0x00000000";
        }
        return "0x"+this.hexRapresentation;
    }

    public boolean isZero(){
        if (StrUtils.isNullOrEmpty(this.hexRapresentation)){
            return true;
        }
        for (char c : this.hexRapresentation.toCharArray()){
            if (c != '0'){
                return false;
            }
        }
        return true;
    }

    private static final Pattern INVALID_HEX_RAPR = Pattern.compile("[^0-9a-f]",Pattern.CASE_INSENSITIVE);

    public boolean isValid(){
        if (this.isZero()){
            return false;
        }
        return !INVALID_HEX_RAPR.matcher(this.hexRapresentation).find();
    }

    @Override
    public String toString(){
        if (isValid()){
            return toHexString();
        }
        return "0xINVALIDSN";
    }


}
