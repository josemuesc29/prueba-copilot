package com.imaginamos.farmatodo.networking.models;

import com.google.appengine.repackaged.com.google.gson.Gson;
import com.imaginamos.farmatodo.model.util.URLConnections;

public class SendSMSCloudFunctionVenReq {
    private String message;
    private String phone;
    private String user;
    private String password;


    public SendSMSCloudFunctionVenReq(String phone, String message) {
        this.phone = phone;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }


}
