package com.imaginamos.farmatodo.model.customer;

import java.io.Serializable;

public class CustomerLoginReq implements Serializable {
    private String email;
    private String password;
    private String facebookId;
    private String googleId;

    public CustomerLoginReq(){}

    public CustomerLoginReq(String email, String password){
        this.email = email;
        this.password = password;
    }

    public CustomerLoginReq(String facebookId){
        this.facebookId = facebookId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }
}
