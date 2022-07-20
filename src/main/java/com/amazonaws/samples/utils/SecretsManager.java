package com.amazonaws.samples.utils;

import com.amazonaws.secretsmanager.caching.SecretCache;
import com.amazonaws.secretsmanager.caching.SecretCacheConfiguration;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SecretsManager {
    private static final Logger log = Logger.getLogger("SecretsManager.class");

    public static SecretCache createSecretCache(
            String region,
            int cacheSize,
            long cacheTTL
    ) {
        try {
            AWSSecretsManagerClientBuilder clientBuilder = AWSSecretsManagerClientBuilder
                    .standard()
                    .withRegion(region);

            SecretCacheConfiguration cacheConf = new SecretCacheConfiguration()
                    .withMaxCacheSize(cacheSize)
                    .withCacheItemTTL(cacheTTL)
                    .withClient(clientBuilder.build());

            return new SecretCache(cacheConf);
        }
        catch (RuntimeException e){
            log.log(Level.parse("SEVERE"),"RuntimeException while building SecretCache instance", e);
            throw new RuntimeException();
        }
    }

    public JsonObject getSecretKey(SecretCache secretCache, String secretId) {
        try {
            log.log(Level.parse("INFO"), "Get Secret from SM " + secretId );
            String secrets = secretCache.getSecretString(secretId);
            return new Gson().fromJson(secrets, JsonObject.class);
        } catch (ResourceNotFoundException e){
            log.log(Level.parse("SEVERE"),"Secrets Manager Resource Not Found", e);
            throw new RuntimeException();
        } catch (RuntimeException e){
            log.log(Level.parse("SEVERE"),"RuntimeException while retrieving Secrets Manager Secret String ", e);
            throw new RuntimeException();
        }
    }
}

