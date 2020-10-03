package com.jounaidr.jrc.node.crypto;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;

@Slf4j
public class KeccakHashHelper {
    private String message;

    /**
     * Instantiates a new Keccak hash helper.
     *
     * @param message String to be hashed
     */
    public KeccakHashHelper(String message) {
        this.message = message;
    }

    /**
     * Implementation of Keccak-256 hashing algorithm
     * provided by the Bouncy Castle Library, based on
     * https://www.baeldung.com/sha-256-hashing-java
     * section 6.3
     *
     * @return the hashed message string in lowercase hex format
     */
    public String returnHash(){
        log.debug("Attempting to hash the following message: {} ...", this.message);

        Keccak.Digest256 digest256 = new Keccak.Digest256();
        byte[] hashbytes = digest256.digest(message.getBytes(StandardCharsets.UTF_8));
        String messageDigest = new String(Hex.encode(hashbytes));

        log.debug("...Hash returned: {}", messageDigest);

        return messageDigest;
    }
}
