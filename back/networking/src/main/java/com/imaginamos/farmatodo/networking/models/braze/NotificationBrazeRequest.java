package com.imaginamos.farmatodo.networking.models.braze;

public class NotificationBrazeRequest {

    private boolean email;
    private boolean push;
    private boolean sms;

    public NotificationBrazeRequest() {
    }

    public NotificationBrazeRequest(boolean email, boolean push, boolean sms) {
        this.email = email;
        this.push = push;
        this.sms = sms;
    }

    public boolean isEmail() {
        return email;
    }

    public void setEmail(boolean email) {
        this.email = email;
    }

    public boolean isPush() {
        return push;
    }

    public void setPush(boolean push) {
        this.push = push;
    }

    public boolean isSms() {
        return sms;
    }

    public void setSms(boolean sms) {
        this.sms = sms;
    }

}
