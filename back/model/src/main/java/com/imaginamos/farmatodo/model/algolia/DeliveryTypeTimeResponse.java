package com.imaginamos.farmatodo.model.algolia;

/**
 * Created by JPuentes on 19/11/2018.
 */
public class DeliveryTypeTimeResponse {
    private int statusCode;
    private String status;
    private String message;
    private Object data;

    public DeliveryTypeTimeResponse(int statusCode, String status, String message, Object data) {
        this.statusCode = statusCode;
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public DeliveryTypeTimeResponse(int statusCode, String status, String message) {
        this.statusCode = statusCode;
        this.status = status;
        this.message = message;
    }

    public DeliveryTypeTimeResponse() {
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
