package com.imaginamos.farmatodo.model.customer;

public class CustomerToken {
    private String token;
    private String tokenIdWebSafe;

    public CustomerToken() {
    }

    public CustomerToken(String token, String tokenIdWebSafe) {
        this.token = token;
        this.tokenIdWebSafe = tokenIdWebSafe;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenIdWebSafe() {
        return tokenIdWebSafe;
    }

    public void setTokenIdWebSafe(String tokenIdWebSafe) {
        this.tokenIdWebSafe = tokenIdWebSafe;
    }
}
