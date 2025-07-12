package com.imaginamos.farmatodo.model.order;

public class OrderEditRes {
    private String code;
    private String message;
    private Object content;
    private String uuid;
    private float newInvoiceValue;

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

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public String getUuid() { return uuid; }

    public void setUuid(String uuid) { this.uuid = uuid; }

    public float getNewInvoiceValue() { return newInvoiceValue; }

    public void setNewInvoiceValue(float newInvoiceValue) { this.newInvoiceValue = newInvoiceValue; }
}
