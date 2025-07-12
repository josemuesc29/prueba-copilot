package com.imaginamos.farmatodo.networking.models.addresses;

public class SendWhatsappCloudFunctionRes {
    private String data;

    public SendWhatsappCloudFunctionRes() {

    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "SendWhatsappCloudFunctionRes{" +
                "data='" + data + '\'' +
                '}';
    }
}
