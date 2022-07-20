package com.amazonaws.samples.reidentification;

import com.amazonaws.samples.aesgcmsiv.EncryptionAESGCMSIV;
import com.amazonaws.samples.utils.ReidentifyRequest;
import com.google.common.cache.Cache;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Reidentification {

    /**
     * Creates a new
     * param request request event
     * param cache the cache
     * param aesgcmsiv encryption class object
     *
     */
    private static final Logger log = Logger.getLogger("Reidentification.class");

    public String[] reidentify(ReidentifyRequest request, Cache<String, String> cache, EncryptionAESGCMSIV aesgcmsiv) {
        String[] identifiers = new String[request.pseudonyms.length];

        for (int i = 0; i < request.pseudonyms.length; i++) {
            String pseudonym = request.pseudonyms[i];
            try {
                String identifier = cache.getIfPresent(pseudonym);
                Optional<byte[]> id;
                if (identifier == null) {
                    try{
                        id = aesgcmsiv.decrypt(Base64.getDecoder().decode(pseudonym), "".getBytes());
                    }catch(IllegalArgumentException e){
                        id = Optional.empty();
                    }
                    if (id.isPresent()) {
                        identifier = new String(id.get(), StandardCharsets.UTF_8);
                        cache.put(pseudonym, identifier);
                    }
                }
                identifiers[i] = identifier;

            } catch (Exception e) {
                log.log(Level.parse("ERROR"),"Error during Reidentification, setting null as result: " + pseudonym + e);
                identifiers[i] = null;
            }

        }
        return identifiers;
    }
}




