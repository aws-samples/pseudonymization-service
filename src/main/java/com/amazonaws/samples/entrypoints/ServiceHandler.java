package com.amazonaws.samples.entrypoints;

import com.amazonaws.samples.aesgcmsiv.EncryptionAESGCMSIV;
import com.amazonaws.samples.pseudonymization.Pseudonymization;
import com.amazonaws.samples.reidentification.Reidentification;
import com.amazonaws.samples.utils.ReidentifyRequest;
import com.amazonaws.samples.utils.ReidentifyResponse;
import com.amazonaws.samples.utils.PseudonymRequest;
import com.amazonaws.samples.utils.PseudonymResponse;
import com.amazonaws.samples.utils.SecretsManager;
import com.amazonaws.secretsmanager.caching.SecretCache;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.Base64;
import java.util.Optional;

public class ServiceHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger log = Logger.getLogger("ServiceHandler.class");

    Gson gson = new Gson();

    private static final Integer DEFAULT_CACHE_SIZE = 400000;
    private static final Cache<String, String> cache;
    private static final SecretCache secretCache;

    private static final String secretArn = System.getenv("SECRET_ARN");
    private static final String secretId = secretArn.substring(secretArn.lastIndexOf(':') + 1);
    private static final String SECRET_DEFAULT_REGION = "EU-WEST-1";
    private static final Integer SECRET_DEFAULT_CACHE_SIZE = 400000;
    private static final Long SECRET_DEFAULT_CACHE_ITEM_TTL = 3600000L;

    static {
        cache = CacheBuilder.newBuilder()
                .maximumSize(Optional.ofNullable(System.getenv("CACHE_SIZE")).map(Integer::parseInt).orElse(DEFAULT_CACHE_SIZE))
                .softValues()
                .build();

        secretCache  = SecretsManager.createSecretCache(
                Optional.ofNullable(System.getenv("SECRET_REGION")).orElse(SECRET_DEFAULT_REGION),
                Optional.ofNullable(System.getenv("SECRET_CACHE_SIZE")).map(Integer::parseInt).orElse(SECRET_DEFAULT_CACHE_SIZE),
                Optional.ofNullable(System.getenv("SECRET_CACHE_TTL")).map(Long::parseLong).orElse(SECRET_DEFAULT_CACHE_ITEM_TTL));
    }

    SecretsManager sm = new SecretsManager();

    JsonObject cryptoKey = sm.getSecretKey(secretCache, secretId);
    String encodedSecretKey = cryptoKey.get("encodedSecretKey").getAsString();
    String encodedNonceKey = cryptoKey.get("encodedNonceKey").getAsString();
    Integer nonceStrPos = cryptoKey.get("nonceStrPos").getAsInt();
    Integer nonceEndPos = cryptoKey.get("nonceEndPos").getAsInt();

    byte[] decodedSecretKey = Base64.getDecoder().decode(encodedSecretKey);
    byte[] decodedNonceKey = Base64.getDecoder().decode(encodedNonceKey);

    EncryptionAESGCMSIV aesgcmsiv = new EncryptionAESGCMSIV(decodedSecretKey, decodedNonceKey, nonceStrPos, nonceEndPos);

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        try {
            // log execution details
            log.log(Level.parse("INFO"),"CONTEXT: " + gson.toJson(context));
            log.log(Level.parse("INFO"),"EVENT: " + gson.toJson(requestEvent));
            log.log(Level.parse("INFO"),"EVENT TYPE: " + requestEvent.getClass().toString());
            log.log(Level.parse("INFO"),"EVENT Path Parameter: " +  requestEvent.getPath());

            String pathParam;
            boolean deterministic=false;
            try{
                pathParam = requestEvent.getPath();
                if (requestEvent.getQueryStringParameters() != null) {
                if (requestEvent.getQueryStringParameters().containsKey("deterministic")) {
                    deterministic = Boolean.parseBoolean(requestEvent.getQueryStringParameters().get("deterministic"));
                }
                }
            }
            catch(RuntimeException e) {
                log.log(Level.parse("SEVERE"),"RuntimeException while getting Path & Query Parameters ", e);
                throw new RuntimeException();
            }
            if (pathParam.equalsIgnoreCase("/pseudonymization")) {
                PseudonymRequest request = gson.fromJson(requestEvent.getBody(), PseudonymRequest.class);
                Pseudonymization p = new Pseudonymization();
                String[] pseudonyms = p.pseudonymize(request, cache, aesgcmsiv, deterministic);
                return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(gson.toJson(new PseudonymResponse(pseudonyms)));
            } else if (pathParam.equalsIgnoreCase("/reidentification"))  {
                ReidentifyRequest request = gson.fromJson(requestEvent.getBody(), ReidentifyRequest.class);
                Reidentification r = new Reidentification();
                String[] identifiers = r.reidentify(request, cache, aesgcmsiv);
                return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(gson.toJson(new ReidentifyResponse(identifiers)));
            } else
            {
                return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody("Path parameter is not correct");
            }
        }
        catch (Exception e) {
            log.log(Level.parse("SEVERE"),"Error handling request: ", e);
                return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody("Error while handling the Request");
            }
    }
}