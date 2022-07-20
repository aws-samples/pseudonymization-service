package com.amazonaws.samples.pseudonymization;

import com.amazonaws.samples.aesgcmsiv.EncryptionAESGCMSIV;
import com.amazonaws.samples.utils.PseudonymRequest;
import com.google.common.cache.Cache;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Pseudonymization {

    /**
     * Creates a new
     * param request request event
     * param cache the cache
     * param aesgcmsiv encryption class object
     *
     */
    private static final java.util.logging.Logger log = Logger.getLogger("Pseudonymization.class");

    public String[] pseudonymize(PseudonymRequest request, Cache<String, String> cache, EncryptionAESGCMSIV aesgcmsiv, Boolean deterministic) {
        String[] pseudonyms = new String[request.identifiers.length];
        for (int i = 0; i < request.identifiers.length; i++) {
            final String identifier = request.identifiers[i];
            try {
                String pseudonym;
                if (deterministic) {
                    pseudonym = cache.getIfPresent(identifier);
                    if (pseudonym == null) {
                        pseudonym = Base64.getEncoder().encodeToString(aesgcmsiv.encrypt(identifier.getBytes(StandardCharsets.UTF_8), "".getBytes(), true));
                        cache.put(identifier, pseudonym);
                    }
                } else {
                    pseudonym = Base64.getEncoder().encodeToString(aesgcmsiv.encrypt(identifier.getBytes(StandardCharsets.UTF_8), "".getBytes()));
                }
                pseudonyms[i] = pseudonym;
            } catch (Exception e) {
                log.log(Level.parse("SEVERE"),"Error during pseudonymisation, setting null as result: " + identifier + e);
                pseudonyms[i] = null;
            }

        }
        return pseudonyms;
    }
}


