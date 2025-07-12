package com.imaginamos.farmatodo.model.customer;

/**
 * Created by JPuentes on 27/09/2018.
 */
public class RequestGetUserOrigin {

    private String email;
    private String uidFirebase;
    private String provider;
    private String phone;
    private String uid;

    public RequestGetUserOrigin() {}

    public RequestGetUserOrigin(String email) {
        this.email = email;
    }

    public RequestGetUserOrigin(String email, String uidFirebase) {
        this.email = email;
        this.uidFirebase = uidFirebase;
    }

    public RequestGetUserOrigin(String email, String uidFirebase, String provider) {
        this.email = email;
        this.uidFirebase = uidFirebase;
        this.provider = provider;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) { this.email = email; }

    public String getUidFirebase() { return uidFirebase; }

    public void setUidFirebase(String uidFirebase) { this.uidFirebase = uidFirebase; }

    public String getProvider() { return provider; }

    public void setProvider(String provider) { this.provider = provider;}

    public String getPhone() { return phone; }

    public void setPhone(String phone) { this.phone = phone; }

    public String getUid() { return uid; }

    public void setUid(String uid) { this.uid = uid; }
}
