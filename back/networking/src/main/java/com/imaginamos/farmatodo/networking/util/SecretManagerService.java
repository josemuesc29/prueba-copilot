package com.imaginamos.farmatodo.networking.util;

import com.google.api.gax.rpc.ApiException;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;

import java.io.IOException;

public class SecretManagerService {

    private static final String VERSION_LATEST = "latest";
    private static final String FORMAT_SECRET_NAME = "projects/%s/secrets/%s/versions/%s";

    private final String projectId;

    public SecretManagerService(String projectId) {
        this.projectId = projectId;
    }

    public String getSecret(String secretId) {
        return getSecret(secretId, VERSION_LATEST);
    }

    public String getSecret(String secretId, String versionId) {
        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {

            String secretName = String.format(FORMAT_SECRET_NAME, projectId, secretId, versionId);
            AccessSecretVersionRequest request = AccessSecretVersionRequest.newBuilder()
                    .setName(secretName)
                    .build();

            AccessSecretVersionResponse response = client.accessSecretVersion(request);
            return response.getPayload().getData().toStringUtf8();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load secret due to network issues", e);
        } catch (ApiException e) {
            throw new RuntimeException("Failed to load secret due to API error", e);
        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred while loading secret", e);
        }
    }
}
