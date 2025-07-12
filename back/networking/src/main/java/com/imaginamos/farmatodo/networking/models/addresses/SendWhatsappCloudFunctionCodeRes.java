package com.imaginamos.farmatodo.networking.models.addresses;

public class SendWhatsappCloudFunctionCodeRes {

    private String ok;
    private String code;
    private String message;

    public SendWhatsappCloudFunctionCodeRes() {
    }

    public SendWhatsappCloudFunctionCodeRes(String ok, String code, String message) {
        this.ok = ok;
        this.code = code;
        this.message = message;
    }

    public String getOk() {
        return ok;
    }

    public void setOk(String ok) {
        this.ok = ok;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "SendWhatsappCloudFunctionCodeRes{" +
                "ok='" + ok + '\'' +
                ", code='" + code + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
