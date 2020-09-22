package com.alperez.importimages.util;

import android.util.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by stanislav.perchenko on 22.09.2020 at 22:49.
 */
public class HashCalculator {

    public static HashCalculator createForMD5() throws NoSuchAlgorithmException {
        return new HashCalculator("MD5");
    }

    public static HashCalculator createForSHA1() throws NoSuchAlgorithmException {
        return new HashCalculator("SHA-1");
    }

    public static HashCalculator createForSHA256() throws NoSuchAlgorithmException {
        return new HashCalculator("SHA-256");
    }


    private String algorithm;
    private final MessageDigest digester;
    private boolean used;

    //--- Result section  ---
    byte[] binaryHash;
    String base64Hash;
    String hexHash;

    private HashCalculator(String algorithm) throws NoSuchAlgorithmException {
        this.algorithm = algorithm;
        digester = MessageDigest.getInstance(algorithm);
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public boolean isUsed() {
        return used;
    }

    public HashCalculator process(String text) {
        if (used) throw new IllegalStateException("Already used");
        updateDigester(text.getBytes());
        binaryHash = digester.digest();
        base64Hash = Base64.encodeToString(binaryHash, Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
        hexHash = new String(encodeBytesToHex(binaryHash)).toLowerCase();
        used = true;

        return this;
    }

    private char[] encodeBytesToHex(byte[] data) {
        char[] ret = new char[data.length * 2];
        for (int i=0, j=0; i<data.length; i++, j+=2) {
            int c = (char)((data[i] >> 4) & 0x0F);
            if (c > 9) c += 7;
            ret[j] = (char)(c+0x30);


            c = (char)(data[i] & 0x0F);
            if (c > 9) c += 7;
            ret[j+1] = (char)(c + 0x30);

        }
        return ret;
    }

    public HashCalculator process(InputStream is) throws IOException {
        if (used) throw new IllegalStateException("Already used");
        updateDigester(is);
        binaryHash = digester.digest();
        base64Hash = Base64.encodeToString(binaryHash, Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
        used = true;
        return this;
    }

    public byte[] getBinaryHash() {
        if (binaryHash == null) throw new IllegalStateException("Not processed yet");
        return binaryHash;
    }

    public String getHexHash() {
        if (hexHash == null) throw new IllegalStateException("Not processed yet");
        return hexHash;
    }

    public String getBase64Hash() {
        if (base64Hash == null) throw new IllegalStateException("Not processed yet");
        return base64Hash;
    }



    private void updateDigester(InputStream is) throws IOException {
        byte[] transBuff = new byte[1024];
        int nBytes;
        while ((nBytes = is.read(transBuff)) > 0) {
            digester.update(transBuff, 0, nBytes);
        }
    }

    private void updateDigester(byte[] data) {
        digester.update(data, 0, data.length);
    }
}
