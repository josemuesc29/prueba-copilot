package com.imaginamos.farmatodo.model.order;




public class PayMicroCharge {

    public final static String SUCCESS="00";
    public final static String INTERNAL="01";
    public final static String PAYMENT="02";

    private String code;
    private String message;
    private boolean status;

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

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
