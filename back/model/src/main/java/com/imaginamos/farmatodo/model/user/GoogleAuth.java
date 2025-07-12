package com.imaginamos.farmatodo.model.user;

public class GoogleAuth {

    private String googleId;
    private String email;

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "GoogleAuth{" +
                "googleId='" + googleId + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
