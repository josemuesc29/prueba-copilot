package com.imaginamos.farmatodo.networking.models.addresses;

public class SendSMSCloudFunctionReq {
    private String to;
    private String body;

    public SendSMSCloudFunctionReq() {
    }

    public SendSMSCloudFunctionReq(String to, String body) {
        this.to = to;
        this.body = body;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
