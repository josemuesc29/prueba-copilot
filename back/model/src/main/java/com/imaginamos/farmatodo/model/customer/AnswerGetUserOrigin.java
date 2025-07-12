package com.imaginamos.farmatodo.model.customer;

/**
 * Created by JPuentes on 26/09/2018.
 */
public class AnswerGetUserOrigin {

    private int statusCode;
    private String status;
    private String origin;
    private String message;

    public AnswerGetUserOrigin(){}

    public AnswerGetUserOrigin(int statusCode, String status, String origin) {
        this.statusCode = statusCode;
        this.status = status;
        this.origin = origin;
    }

    public AnswerGetUserOrigin(int statusCode, String status, String origin,String message) {
        this.statusCode = statusCode;
        this.status = status;
        this.origin = origin;
        this.message = message;
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

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
