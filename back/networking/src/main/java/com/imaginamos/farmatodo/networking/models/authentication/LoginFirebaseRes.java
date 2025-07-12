package com.imaginamos.farmatodo.networking.models.authentication;

public class LoginFirebaseRes {
    private String uidFirebase;
    private String email;
    private boolean emailVerified;
    private String idProvider;
    private String provider;
    private String providerEmail;
    private String operationType;

    public String getUidFirebase() {
        return uidFirebase;
    }

    public void setUidFirebase(String uidFirebase) {
        this.uidFirebase = uidFirebase;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getIdProvider() {
        return idProvider;
    }

    public void setIdProvider(String idProvider) {
        this.idProvider = idProvider;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProviderEmail() {
        return providerEmail;
    }

    public void setProviderEmail(String providerEmail) {
        this.providerEmail = providerEmail;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }
}
