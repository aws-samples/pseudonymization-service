package com.amazonaws.samples.utils;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HmacUtils {
    private static final Logger log = Logger.getLogger("HmacUtils.class");

    public String generateHmac256(byte[] message, byte[] key) {
        try {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key, "HmacSHA256");
        sha256_HMAC.init(secret_key);
        System.out.println(Hex.encodeHexString(sha256_HMAC.doFinal(message)));
        return Hex.encodeHexString(sha256_HMAC.doFinal(message));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.log(Level.parse("SEVERE"),"RuntimeException - generateHmac256 ", e);
        }

        return null;
    }
}