package com.jounaidr.jrc.node.crypto;

import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;

public class CryptoHashHelper {
    private String message;

    public CryptoHashHelper(String message) {
        this.message = message;
    }

    public String returnHash(){
        Keccak.Digest256 digest256 = new Keccak.Digest256();
        byte[] hashbytes = digest256.digest(message.getBytes(StandardCharsets.UTF_8));
        String messageDigest = new String(Hex.encode(hashbytes));

        return messageDigest;
    }
}
