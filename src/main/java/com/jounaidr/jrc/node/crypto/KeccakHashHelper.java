package com.jounaidr.jrc.node.crypto;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;

@Slf4j
public class KeccakHashHelper {
    private String message;

    public KeccakHashHelper(String message) {
        this.message = message;
    }

    public String returnHash(){
        log.debug("Attempting to hash the following message: {} ...", this.message);

        Keccak.Digest256 digest256 = new Keccak.Digest256();
        byte[] hashbytes = digest256.digest(message.getBytes(StandardCharsets.UTF_8));
        String messageDigest = new String(Hex.encode(hashbytes));

        log.debug("...Hash returned: {}", messageDigest);

        return messageDigest;
    }
}
