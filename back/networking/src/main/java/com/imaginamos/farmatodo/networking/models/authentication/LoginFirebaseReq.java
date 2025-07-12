package com.imaginamos.farmatodo.networking.models.authentication;

public class LoginFirebaseReq {
    private String uidFirebase;

    public LoginFirebaseReq(String uidFirebase) {
        this.uidFirebase = uidFirebase;
    }

    public String getUidFirebase() {
        return uidFirebase;
    }

    public void setUidFirebase(String uidFirebase) {
        this.uidFirebase = uidFirebase;
    }
}
