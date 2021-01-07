package com.jounaidr.jrc.server.blockchain.util;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;

@Slf4j
public class KeccakHashUtil {

    /**
     * Implementation of Keccak-256 hashing algorithm
     * provided by the Bouncy Castle Library, based on
     * https://www.baeldung.com/sha-256-hashing-java
     * section 6.3
     *
     * @return the hashed message string in lowercase hex format
     */
    public static String returnHash(String message){
        Keccak.Digest256 digest256 = new Keccak.Digest256();
        byte[] hashbytes = digest256.digest(message.getBytes(StandardCharsets.UTF_8));

        return new String(Hex.encode(hashbytes));
    }
}
