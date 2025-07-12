package com.imaginamos.farmatodo.model.provider;

public class ProviderRes extends WebServiceClient {
    private String name;
    private String email;
    private String client_id;
    private String client_secret;

    public ProviderRes() {
    }

    public ProviderRes(String name, String email, String client_id, String client_secret) {
        this.name = name;
        this.email = email;
        this.client_id = client_id;
        this.client_secret = client_secret;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getClient_secret() {
        return client_secret;
    }

    public void setClient_secret(String client_secret) {
        this.client_secret = client_secret;
    }
}
