package com.imaginamos.farmatodo.model.customer;

/**
 * Created by JPuentes on 6/11/2018.
 */
public class EmailToLowerCaseResponse {

    private String resultingEmail;
    private Integer statusCode;
    private String status;
    private String message;

    public EmailToLowerCaseResponse(Integer statusCode, String status, String message) {
        this.statusCode = statusCode;
        this.status = status;
        this.message = message;
    }

    public EmailToLowerCaseResponse(String resultingEmail, Integer statusCode, String status, String message) {
        this.resultingEmail = resultingEmail;
        this.statusCode = statusCode;
        this.status = status;
        this.message = message;
    }

    public String getResultingEmail() {
        return resultingEmail;
    }

    public void setResultingEmail(String resultingEmail) {
        this.resultingEmail = resultingEmail;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
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
}
