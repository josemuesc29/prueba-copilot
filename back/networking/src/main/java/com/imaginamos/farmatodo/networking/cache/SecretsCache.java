package com.imaginamos.farmatodo.networking.cache;

import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.networking.util.SecretManagerService;

import java.util.HashMap;
import java.util.Map;

public class SecretsCache {

    private static SecretsCache instance;
    private final Map<String, String> secrets = new HashMap<>();
    private final SecretManagerService secretManagerService;

    private SecretsCache(String projectId) {
        this.secretManagerService = new SecretManagerService(projectId);
        loadSecrets();
    }
    public static SecretsCache getInstance(String projectId) {
        synchronized (SecretsCache.class) {
            if (instance == null) {
                instance = new SecretsCache(projectId);
            }
        }
        return instance;
    }

    /**
     * Load all the secrets you need and store them in the map
     */
    private void loadSecrets() {
        secrets.put(Constants.GCP_GROWTHBOOK_SECRET_ID, secretManagerService.getSecret(Constants.GCP_GROWTHBOOK_SECRET_ID));
    }

    public String getSecret(String key) {
        return secrets.get(key);
    }
}