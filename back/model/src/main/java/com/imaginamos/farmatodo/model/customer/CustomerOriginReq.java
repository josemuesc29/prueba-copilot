package com.imaginamos.farmatodo.model.customer;

public class CustomerOriginReq {
    private String phone;
    private String uid;
    private String email;

    public CustomerOriginReq() {
    }

    public CustomerOriginReq(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "{ phone:" + phone + ", uid:" + uid +", email:" + email + "}";
    }
}
